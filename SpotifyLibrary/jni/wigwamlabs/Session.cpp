#define LOG_TAG "Session"
//#define LOG_NDEBUG 0
#include "log.h"
#if 0
#define LOG_RUN_LOOP(...) LOGV(__VA_ARGS__)
#else
#define LOG_RUN_LOOP(...) ((void)0)
#endif

#include "Session.h"
#include "PlaylistContainer.h"
#include "Player.h"
#include <stddef.h>
#include <string.h>
#include "key.c"

namespace wigwamlabs {

Session *getSelf(sp_session *session) {
    return static_cast<Session *>(sp_session_userdata(session));
}

Session *Session::create(SessionCallback *callback, const char *settingsPath, const char *cachePath, const char *deviceId, sp_error &outError) {
    LOGV("%s (%s, %s, %s)", __func__, settingsPath, cachePath, deviceId);
    // create instance
    Session *self = new Session(callback);

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
    callbacks.play_token_lost = onPlayTokenLost;
    callbacks.log_message = onLogMessage;
    callbacks.end_of_track = onEndOfTrack;
    callbacks.streaming_error = onStreamingError;
    callbacks.start_playback = onStartPlayback;
    callbacks.stop_playback = onStopPlayback;
    callbacks.get_audio_buffer_stats = onGetAudioBufferStats;
    callbacks.offline_status_updated = onOfflineStatusUpdated;
    callbacks.offline_error = onOfflineError;
    callbacks.credentials_blob_updated = onCredentialsBlobUpdated;
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

Session::Session(SessionCallback *callback) :
    mCallback(callback),
    mSession(NULL),
    mMainThread(0),
    mMainNotifyDo(0),
    mMainThreadRunning(false),
    mWaitingForLoggedIn(false) {
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

void *Session::mainThreadLoop(void *self) {
    return static_cast<Session *>(self)->mainThreadLoop();
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

    int nextTimeoutMs = 0;
    while (mMainThreadRunning) {
        if (nextTimeoutMs == 0) {
            while (!mMainNotifyDo) {
                pthread_cond_wait(&mMainNotifyCond, &mMainNotifyMutex);
            }
        } else {
            struct timespec ts;
            clock_gettime(CLOCK_REALTIME, &ts);
            ts.tv_sec += nextTimeoutMs / 1000;
            ts.tv_nsec += (nextTimeoutMs % 1000) * 1000000;

            pthread_cond_timedwait(&mMainNotifyCond, &mMainNotifyMutex, &ts);
        }

        mMainNotifyDo = 0;
        pthread_mutex_unlock(&mMainNotifyMutex);

        do {
            LOG_RUN_LOOP("Calling sp_session_process_events");
            sp_session_process_events(mSession, &nextTimeoutMs);
        } while (nextTimeoutMs == 0);

        // Workaround for bug https://github.com/spotify/cocoalibspotify/issues/140
        // During login the notify_main_thread callback isn't called for some reason.
        // Since the connection state is offline, nextTimeoutMs is 5 mins, so it takes ages
        // before the connectionstate_updated callback is called.
        if (mWaitingForLoggedIn && nextTimeoutMs > 200) {
            nextTimeoutMs = 200;
        }

        pthread_mutex_lock(&mMainNotifyMutex);
    }

    // cleanup
    pthread_mutex_destroy(&mMainNotifyMutex);
    pthread_cond_destroy(&mMainNotifyCond);

    //
    return NULL;
}

sp_error Session::destroy() {
    LOGV(__func__);
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
    delete mCallback;
    if (mSession) {
        LOGE("mSession not null, forgot to call destroy()?");
    }
    mSession = NULL;
    mMainThreadRunning = false;
}

sp_session *Session::getSession() {
    return mSession;
}

int Session::getConnectionState() const {
    LOGV(__func__);
    return sp_session_connectionstate(mSession);
}

sp_error Session::login(const char *username, const char *password, const char *blob) {
    LOGV("%s (%s) credentials:%s%s", __func__, username, password ? " password" : "", blob ? " blob" : "");

    sp_error error = sp_session_login(mSession, username, password, false, blob);
    if (error == SP_ERROR_OK) {
        mWaitingForLoggedIn = true;
        onNotifyMainThread(mSession);
    }
    return error;
}

sp_error Session::logout() {
    LOGV(__func__);
    return sp_session_logout(mSession);
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

void Session::onLoggedIn(sp_session *session, sp_error error) {
    LOGV("%s %s", __func__, sp_error_message(error));
    Session *self = getSelf(session);
    if (error != SP_ERROR_OK) {
        self->mWaitingForLoggedIn = false;
    }
    self->mCallback->onLoggedIn(error);
}

void Session::onLoggedOut(sp_session *session) {
    LOGV(__func__);
}

void Session::onMetadataUpdated(sp_session *session) {
    LOGV(__func__);
    getSelf(session)->mCallback->onMetadataUpdated();
}

void Session::onConnectionError(sp_session *session, sp_error error) {
    LOGV("%s %s", __func__, sp_error_message(error));
}

void Session::onMessageToUser(sp_session *session, const char *message) {
    LOGV("%s %s", __func__, message);
}

void Session::onNotifyMainThread(sp_session *session) {
    LOG_RUN_LOOP(__func__);
    Session *self = getSelf(session);

    if (self->mMainThreadRunning) {
        pthread_mutex_lock(&self->mMainNotifyMutex);
        self->mMainNotifyDo = 1;
        pthread_cond_signal(&self->mMainNotifyCond);
        pthread_mutex_unlock(&self->mMainNotifyMutex);
    }
}

int Session::onMusicDelivery(sp_session *session, const sp_audioformat *format, const void *frames, int numFrames) {
    return getSelf(session)->mPlayer->onMusicDelivery(format, frames, numFrames);
}

void Session::onPlayTokenLost(sp_session *session) {
    LOGV(__func__);
    return getSelf(session)->mPlayer->onPlayTokenLost();
}

void Session::onLogMessage(sp_session *session, const char *data) {
    LOGV("%s %s", __func__, data);
}

void Session::onEndOfTrack(sp_session *session) {
    LOGV(__func__);
}

void Session::onStreamingError(sp_session *session, sp_error error) {
    LOGV("%s, %s", __func__, sp_error_message(error));
}

void Session::onStartPlayback(sp_session *session) {
    getSelf(session)->mPlayer->onStartPlayback();
}

void Session::onStopPlayback(sp_session *session) {
    getSelf(session)->mPlayer->onStopPlayback();
}

void Session::onGetAudioBufferStats(sp_session *session, sp_audio_buffer_stats *stats) {
    getSelf(session)->mPlayer->onGetAudioBufferStats(stats);
}

void Session::onOfflineStatusUpdated(sp_session *session) {
    Session *self = getSelf(session);
    bool syncing = (sp_session_connectionstate(self->mSession) == SP_CONNECTION_STATE_LOGGED_IN);
    int tracks = sp_offline_tracks_to_sync(self->mSession);
    LOGV("%s: syncing: %d, offline tracks to sync: %d", __func__, syncing, tracks);
    self->mCallback->onOfflineTracksToSyncChanged(syncing, tracks);
}

void Session::onOfflineError(sp_session *session, sp_error error) {
    LOGV("%s %s", __func__, sp_error_message(error));
}

void Session::onCredentialsBlobUpdated(sp_session *session, const char *blob) {
    LOGV(__func__);
    getSelf(session)->mCallback->onCredentialsBlobUpdated(blob);
}

void Session::onConnectionStateUpdated(sp_session *session) {
    Session *self = getSelf(session);
    sp_connectionstate state = sp_session_connectionstate(self->mSession);
    LOGV("%s %d", __func__, state);
    self->mCallback->onConnectionStateUpdated(state);

    if (self->mWaitingForLoggedIn && state == SP_CONNECTION_STATE_LOGGED_IN) {
        self->mWaitingForLoggedIn = false;
    }
}

} // namespace wigwamlabs
