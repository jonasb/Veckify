#define LOG_TAG "Session"
#define LOG_NDEBUG 0
#include "log.h"

#include "Session.h"
#include <stddef.h>
#include <string.h>
#include "key.c"

namespace wigwamlabs {

////////////////////////////////////////////////////////////
// static callbacks that forwards the calls to the right instance
//

Session *getSelf(sp_session *session) {
    return static_cast<Session *>(sp_session_userdata(session));
}

void Session::onNotifyMainThread(sp_session *session) {
    getSelf(session)->onNotifyMainThread();
}

void Session::onLogMessage(sp_session *session, const char *data) {
    getSelf(session)->onLogMessage(data);
}

void Session::onConnectionError(sp_session *session, sp_error error) {
    getSelf(session)->onConnectionError(error);
}

void Session::onLoggedIn(sp_session *session, sp_error error) {
    getSelf(session)->onLoggedIn(error);
}

void Session::onLoggedOut(sp_session *session) {
    getSelf(session)->onLoggedOut();
}

////////////////////////////////////////////////////////////
// factory

Session *Session::create(Context *context, const char *settingsPath, const char *cachePath, const char *deviceId, sp_error &outError) {
    LOGV("create(%s, %s, %s)", settingsPath, cachePath, deviceId);
    Session *self = new Session(context);

    // callbacks
    sp_session_callbacks callbacks;
    memset(&callbacks, 0, sizeof(sp_session_callbacks));
    callbacks.log_message = onLogMessage;
    callbacks.logged_in = onLoggedIn;
    callbacks.logged_out = onLoggedOut;
    callbacks.connection_error = onConnectionError;
    callbacks.notify_main_thread = onNotifyMainThread;

    // config
    sp_session_config config;
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
    config.initially_unload_playlists = true;
    config.device_id = deviceId;
    config.proxy = NULL;
    config.proxy_username = NULL;
    config.proxy_password = NULL;
    config.ca_certs_filename = NULL;
    config.tracefile = NULL;

    // create
    sp_session *session;
    outError = sp_session_create(&config, &session);

    if (outError != SP_ERROR_OK) {
        delete self;
        self = NULL;
    }

    self->mSession = session;

    return self;
}

////////////////////////////////////////////////////////////
//
//

Session::Session(Context *context) :
    mContext(context),
    mSession(NULL) {
}

sp_error Session::destroy() {
    LOGV("destroy()");
    sp_error error = SP_ERROR_OK;
    if (mSession) {
        error = sp_session_release(mSession);
        mSession = NULL;
    }
    return error;
}

Session::~Session() {
    mContext = NULL;
    if (mSession) {
        LOGE("mSession not null, forgot to call destroy()?");
    }
    mSession = NULL;
}

void Session::onNotifyMainThread() {
    LOGV("onNotifyMainThread()");
}

void Session::onLogMessage(const char *data) {
    LOGV("onLogMessage() %s", data);
}

void Session::onConnectionError(sp_error error) {
    LOGV("onConnectionError() %s", sp_error_message(error));
}

void Session::onLoggedIn(sp_error error) {
    LOGV("onLoggedIn() %s", sp_error_message(error));
}

void Session::onLoggedOut() {
    LOGV("onLoggedOut()");
}

} // namespace wigwamlabs
