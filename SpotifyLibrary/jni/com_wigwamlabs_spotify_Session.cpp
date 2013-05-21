#define LOG_TAG "com_wigwamlabs_spotify_Session"
//#define LOG_NDEBUG 0
#include "log.h"

#include <jni.h>
#include <pthread.h>
#include "ExceptionUtils.h"
#include "JNIEnvProvider.h"
#include "utils.h"
#include "wigwamlabs/Session.h"

using namespace wigwamlabs;

jfieldID sSessionHandleField = 0;
jmethodID sSessionOnLoggedInMethod = 0;
jmethodID sSessionOnMetadataUpdatedMethod = 0;
jmethodID sSessionOnCredentialsBlobUpdatedMethod = 0;
jmethodID sSessionOnConnectionStateUpdatedMethod = 0;
jmethodID sSessionOnOfflineTracksToSyncChangedMethod = 0;

class SessionCallbackJNI : public SessionCallback {
public:
    SessionCallbackJNI(JNIEnv *env, jobject session) :
        mProvider(JNIEnvProvider::instance(env)) {
        mSession = env->NewGlobalRef(session);
    }

    ~SessionCallbackJNI() {
        mProvider->getEnv()->DeleteGlobalRef(mSession);
    }

    void onLoggedIn(sp_error error) {
        mProvider->getEnv()->CallVoidMethod(mSession, sSessionOnLoggedInMethod, error);
    }

    void onMetadataUpdated() {
        mProvider->getEnv()->CallVoidMethod(mSession, sSessionOnMetadataUpdatedMethod);
    }

    void onCredentialsBlobUpdated(const char *blobStr) {
        JNIEnv *env = mProvider->getEnv();
        jstring blob = env->NewStringUTF(blobStr);

        env->CallVoidMethod(mSession, sSessionOnCredentialsBlobUpdatedMethod, blob);

        env->DeleteLocalRef(blob);
    }

    void onConnectionStateUpdated(int state) {
        mProvider->getEnv()->CallVoidMethod(mSession, sSessionOnConnectionStateUpdatedMethod, state);
    }

    void onOfflineTracksToSyncChanged(bool syncing, int tracks) {
        mProvider->getEnv()->CallVoidMethod(mSession, sSessionOnOfflineTracksToSyncChangedMethod, syncing, tracks);
    }
private:
    jobject mSession;
    JNIEnvProvider *mProvider;
};

Session *getNativeSession(JNIEnv *env, jobject object) {
    const jint handle = env->GetIntField(object, sSessionHandleField);
    return reinterpret_cast<Session *>(handle);
}

JNI_STATIC_METHOD(void, com_wigwamlabs_spotify_Session, nativeInitClass) {
    LOGV("nativeInitClass()");

    if (sSessionHandleField == 0) {
        sSessionHandleField = env->GetFieldID(klass, "mHandle", "I");
    }
    if (sSessionOnLoggedInMethod == 0) {
        sSessionOnLoggedInMethod = env->GetMethodID(klass, "onLoggedIn", "(I)V");
    }
    if (sSessionOnMetadataUpdatedMethod == 0) {
        sSessionOnMetadataUpdatedMethod = env->GetMethodID(klass, "onMetadataUpdated", "()V");
    }
    if (sSessionOnCredentialsBlobUpdatedMethod == 0) {
        sSessionOnCredentialsBlobUpdatedMethod = env->GetMethodID(klass, "onCredentialsBlobUpdated", "(Ljava/lang/String;)V");
    }
    if (sSessionOnConnectionStateUpdatedMethod == 0) {
        sSessionOnConnectionStateUpdatedMethod = env->GetMethodID(klass, "onConnectionStateUpdated", "(I)V");
    }
    if (sSessionOnOfflineTracksToSyncChangedMethod == 0) {
        sSessionOnOfflineTracksToSyncChangedMethod = env->GetMethodID(klass, "onOfflineTracksToSyncChanged", "(ZI)V");
    }
}

JNI_METHOD_ARGS(jint, com_wigwamlabs_spotify_Session, nativeCreate, jstring settingsPath, jstring cachePath, jstring deviceId) {
    LOGV("nativeCreate()");

    const char *settingsPathStr = env->GetStringUTFChars(settingsPath, NULL);
    const char *cachePathStr = env->GetStringUTFChars(cachePath, NULL);
    const char *deviceIdStr = env->GetStringUTFChars(deviceId, NULL);

    SessionCallbackJNI *callback = new SessionCallbackJNI(env, self);

    sp_error error;
    Session *session = Session::create(callback, settingsPathStr, cachePathStr, deviceIdStr, error);

    env->ReleaseStringUTFChars(deviceId, deviceIdStr);
    env->ReleaseStringUTFChars(cachePath, cachePathStr);
    env->ReleaseStringUTFChars(settingsPath, settingsPathStr);

    if (error != SP_ERROR_OK) {
        ExceptionUtils::throwException(env, ExceptionUtils::RUNTIME_EXCEPTION, sp_error_message(error));
        return 0;
    }

    return reinterpret_cast<jint>(session);
}

JNI_METHOD(void, com_wigwamlabs_spotify_Session, nativeDestroy) {
    LOGV("nativeDestroy()");

    Session *session = getNativeSession(env, self);
    sp_error error = session->destroy();
    delete session;

    if (error != SP_ERROR_OK) {
        ExceptionUtils::throwException(env, ExceptionUtils::RUNTIME_EXCEPTION, sp_error_message(error));
        return;
    }
}

JNI_METHOD_ARGS(void, com_wigwamlabs_spotify_Session, nativeSetStreamingBitrate, jint bitrate) {
    LOGV("nativeSetStreamingBitrate(%d)", bitrate);
    Session *session = getNativeSession(env, self);
    sp_error error = session->setStreamingBitrate((sp_bitrate) bitrate);
    if (error != SP_ERROR_OK) {
        LOGW("setStreamingBitrate(%d) => %s", bitrate, sp_error_message(error));
    }
}

JNI_METHOD_ARGS(void, com_wigwamlabs_spotify_Session, nativeSetOfflineBitrate, jint bitrate) {
    LOGV("nativeSetOfflineBitrate(%d)", bitrate);
    Session *session = getNativeSession(env, self);
    sp_error error = session->setOfflineBitrate((sp_bitrate) bitrate);
    if (error != SP_ERROR_OK) {
        LOGW("setOfflineBitrate(%d) => %s", bitrate, sp_error_message(error));
    }
}

JNI_METHOD(jint, com_wigwamlabs_spotify_Session, nativeGetConnectionState) {
    LOGV("nativeGetConnectionState()");

    Session *session = getNativeSession(env, self);
    return session->getConnectionState();
}

JNI_METHOD_ARGS(void, com_wigwamlabs_spotify_Session, nativeLogin, jstring username, jstring password, jstring blob) {
    LOGV("nativeLogin()");

    Session *session = getNativeSession(env, self);
    const char *usernameStr = env->GetStringUTFChars(username, NULL);
    const char *passwordStr = (!password ? NULL : env->GetStringUTFChars(password, NULL));
    const char *blobStr = (!blob ? NULL : env->GetStringUTFChars(blob, NULL));

    sp_error error = session->login(usernameStr, passwordStr, blobStr);

    if (blob && blobStr) {
        env->ReleaseStringUTFChars(blob, blobStr);
    }
    if (password && passwordStr) {
        env->ReleaseStringUTFChars(password, passwordStr);
    }
    env->ReleaseStringUTFChars(username, usernameStr);

    if (error != SP_ERROR_OK) {
        ExceptionUtils::throwException(env, ExceptionUtils::RUNTIME_EXCEPTION, sp_error_message(error));
        return;
    }
}

JNI_METHOD(void, com_wigwamlabs_spotify_Session, nativeLogout) {
    LOGV("nativeLogout()");

    Session *session = getNativeSession(env, self);
    sp_error error = session->logout();

    if (error != SP_ERROR_OK) {
        ExceptionUtils::throwException(env, ExceptionUtils::RUNTIME_EXCEPTION, sp_error_message(error));
        return;
    }
}

JNI_METHOD(jint, com_wigwamlabs_spotify_Session, nativeGetPlaylistContainer) {
    LOGV("nativeGetPlaylistContainer()");

    Session *session = getNativeSession(env, self);

    PlaylistContainer *container = session->getPlaylistContainer();

    return reinterpret_cast<jint>(container);
}

JNI_METHOD(jint, com_wigwamlabs_spotify_Session, nativeGetPlayer) {
    LOGV("nativeGetPlayer()");

    Session *session = getNativeSession(env, self);

    Player *player = session->getPlayer();

    return reinterpret_cast<jint>(player);
}
