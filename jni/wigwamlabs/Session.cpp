#define LOG_TAG "Session"
#define LOG_NDEBUG 0
#include "log.h"

#include "Session.h"
#include "PlaylistContainer.h"
#include "Player.h"
#include <stddef.h>
#include <string.h>
#include "key.c"

namespace wigwamlabs {

////////////////////////////////////////////////////////////
// static callbacks
//

void *Session::mainThreadLoop(void *self) {
    return static_cast<Session *>(self)->mainThreadLoop();
}

Session *getSelf(sp_session *session) {
    return static_cast<Session *>(sp_session_userdata(session));
}

void Session::onLoggedIn(sp_session *session, sp_error error) {
    getSelf(session)->onLoggedIn(error);
}

void Session::onLoggedOut(sp_session *session) {
    getSelf(session)->onLoggedOut();
}

void Session::onMetadataUpdated(sp_session *session) {
    LOGV("%s", __func__);
    Session *self = getSelf(session);
    self->mCallback->onMetadataUpdated();
}

void Session::onConnectionError(sp_session *session, sp_error error) {
    getSelf(session)->onConnectionError(error);
}

void Session::onMessageToUser(sp_session *session, const char *message) {
    getSelf(session)->onMessageToUser(message);
}

void Session::onNotifyMainThread(sp_session *session) {
    getSelf(session)->onNotifyMainThread();
}

void Session::onLogMessage(sp_session *session, const char *data) {
    getSelf(session)->onLogMessage(data);
}

void Session::onConnectionStateUpdated(sp_session *session) {
    getSelf(session)->onConnectionStateUpdated();
}

////////////////////////////////////////////////////////////
// factory

Session *Session::create(Context *context, SessionCallback *callback, const char *settingsPath, const char *cachePath, const char *deviceId, sp_error &outError) {
    LOGV("create(%s, %s, %s)", settingsPath, cachePath, deviceId);
    // create instance
    Session *self = new Session(context, callback);

    // create thread
    outError = self->startThread();
    if (outError != SP_ERROR_OK) {
        delete self;
        return NULL;
    }

    // callbacks
    sp_session_callbacks callbacks;
    memset(&callbacks, 0, sizeof(sp_session_callbacks));
    callbacks.logged_in = onLoggedIn;
    callbacks.logged_out = onLoggedOut;
    callbacks.metadata_updated = onMetadataUpdated;
    callbacks.connection_error = onConnectionError;
    callbacks.message_to_user = onMessageToUser;
    callbacks.notify_main_thread = onNotifyMainThread;
    callbacks.music_delivery = onMusicDelivery;
    callbacks.log_message = onLogMessage;
    callbacks.end_of_track = onEndOfTrack;
    callbacks.connectionstate_updated = onConnectionStateUpdated;

    // config
    sp_session_config config;
    memset(&config, 0, sizeof(sp_session_config));
    config.api_version = SPOTIFY_API_VERSION;
    config.cache_location = cachePath;
    config.settings_location = settingsPath;
    config.application_key = g_appkey;
    config.application_key_size = g_appkey_size;
    config.user_agent = "Veckify";
    config.callbacks = &callbacks;
    config.userdata = self;
    config.compress_playlists = true;
    config.dont_save_metadata_for_playlists = false;
    config.initially_unload_playlists = false;
    config.device_id = deviceId;
    config.proxy = NULL;
    config.proxy_username = NULL;
    config.proxy_password = NULL;
    config.ca_certs_filename = NULL;
    config.tracefile = NULL;

    // create session
    sp_session *session;
    outError = sp_session_create(&config, &session);

    // init player
    if (outError == SP_ERROR_OK) {
        self->mPlayer = Player::create(session);
        if (self->mPlayer == NULL) {
            outError = SP_ERROR_OTHER_TRANSIENT;
        }
    }

    //
    if (outError == SP_ERROR_OK) {
        self->mSession = session;
    } else {
        delete self;
        self = NULL;
    }

    return self;
}

////////////////////////////////////////////////////////////
//
//

Session::Session(Context *context, SessionCallback *callback) :
    mContext(context),
    mCallback(callback),
    mSession(NULL),
    mMainThread(0),
    mMainNotifyDo(0),
    mMainThreadRunning(false) {
}

sp_error Session::startThread() {
    int err = 0;
    if (!err) {
        err = pthread_create(&mMainThread, NULL, mainThreadLoop, this);
    }
    if (!err) {
        /*ignore = */ pthread_setname_np(mMainThread, "Session Worker");
    }
    return (err ? SP_ERROR_SYSTEM_FAILURE : SP_ERROR_OK);
}

void *Session::mainThreadLoop() {
    // init semaphores
    int err = 0;
    err = pthread_mutex_init(&mMainNotifyMutex, NULL);
    if (!err) {
        err = pthread_cond_init(&mMainNotifyCond, NULL);
        if (err) {
            pthread_mutex_destroy(&mMainNotifyMutex);
        }
    }
    if (err) {
        return NULL;
    }

    // run
    mMainThreadRunning = true;
    pthread_mutex_lock(&mMainNotifyMutex);

    int nextTimeout = 0;
    while (mMainThreadRunning) {
        if (nextTimeout == 0) {
            while (!mMainNotifyDo) {
                pthread_cond_wait(&mMainNotifyCond, &mMainNotifyMutex);
            }
        } else {
            struct timespec ts;
            clock_gettime(CLOCK_REALTIME, &ts);
            ts.tv_sec += nextTimeout / 1000;
            ts.tv_nsec += (nextTimeout % 1000) * 1000000;

            pthread_cond_timedwait(&mMainNotifyCond, &mMainNotifyMutex, &ts);
        }

        mMainNotifyDo = 0;
        pthread_mutex_unlock(&mMainNotifyMutex);

        do {
            LOGV("Calling sp_session_process_events");
            sp_session_process_events(mSession, &nextTimeout);
        } while (nextTimeout == 0);

        pthread_mutex_lock(&mMainNotifyMutex);
    }

    // cleanup
    pthread_mutex_destroy(&mMainNotifyMutex);
    pthread_cond_destroy(&mMainNotifyCond);

    //
    return NULL;
}

sp_error Session::destroy() {
    LOGV("destroy()");
    sp_error error = SP_ERROR_OK;
    if (mSession) {
        error = sp_session_logout(mSession);
        if (error == SP_ERROR_OK) {
            error = sp_session_release(mSession);
        }
        mSession = NULL;

        // stop thread
        mMainThreadRunning = false;

        //
        error = mPlayer->destroy();
        delete mPlayer;
        mPlayer = NULL;
    }
    return error;
}

Session::~Session() {
    mContext = NULL;
    delete mCallback;
    if (mSession) {
        LOGE("mSession not null, forgot to call destroy()?");
    }
    mSession = NULL;
    mMainThreadRunning = false;
}

bool Session::relogin() {
    LOGV("relogin()");
    return (sp_session_relogin(mSession) == SP_ERROR_OK);
}

sp_error Session::login(const char *username, const char *password, bool rememberMe) {
    LOGV("login(%s, ***, %d)", username, rememberMe);
    return sp_session_login(mSession, username, password, rememberMe, NULL); //TODO need to deal with blob?
}

PlaylistContainer *Session::getPlaylistContainer() {
    sp_playlistcontainer *c = sp_session_playlistcontainer(mSession);
    if (!c) {
        return NULL;
    }
    return new PlaylistContainer(c);
}

Player *Session::getPlayer() {
    return mPlayer;
}

void Session::onLoggedIn(sp_error error) {
    LOGV("onLoggedIn() %s", sp_error_message(error));
}

void Session::onLoggedOut() {
    LOGV("onLoggedOut()");
}

void Session::onConnectionError(sp_error error) {
    LOGV("onConnectionError() %s", sp_error_message(error));
}

void Session::onMessageToUser(const char *message) {
    LOGV("onMessageToUser(%s)", message);
}

void Session::onNotifyMainThread() {
    LOGV("onNotifyMainThread()");

    if (mMainThreadRunning) {
        pthread_mutex_lock(&mMainNotifyMutex);
        mMainNotifyDo = 1;
        pthread_cond_signal(&mMainNotifyCond);
        pthread_mutex_unlock(&mMainNotifyMutex);
    }
}

int Session::onMusicDelivery(sp_session *session, const sp_audioformat *format, const void *frames, int numFrames) {
    return getSelf(session)->mPlayer->onMusicDelivery(format, frames, numFrames);
}

void Session::onLogMessage(const char *data) {
    LOGV("onLogMessage() %s", data);
}

void Session::onConnectionStateUpdated() {
    LOGV("onConnectionStateUpdated()");

    mCallback->onConnectionStateUpdated(sp_session_connectionstate(mSession));
}

void Session::onEndOfTrack(sp_session *session) {
    return getSelf(session)->mPlayer->onEndOfTrack();
}

} // namespace wigwamlabs
