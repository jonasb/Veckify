#define LOG_TAG "com_wigwamlabs_spotify_app_SpotifySession"
//#define LOG_NDEBUG 0
#include "log.h"

#include <jni.h>
#include <pthread.h>
#include "ExceptionUtils.h"
#include "utils.h"
#include "wigwamlabs/Session.h"

using namespace wigwamlabs;

jfieldID sSessionHandleField = 0;
jmethodID sSessionOnConnectionStateUpdatedMethod = 0;

class SessionCallbackJNI : public SessionCallback {
public:
    SessionCallbackJNI(JNIEnv *env, jobject session) :
        mEnv(NULL) {
        mSession = env->NewGlobalRef(session);
        env->GetJavaVM(&mVm);
    }

    ~SessionCallbackJNI() {
        JNIEnv *env = getEnv();
        if (env) {
            env->DeleteGlobalRef(mSession);
        }
    }

    void onConnectionStateUpdated(int state) {
        getEnv()->CallVoidMethod(mSession, sSessionOnConnectionStateUpdatedMethod, state);
    }

private:
    JNIEnv *getEnv() {
        // TODO support multiple threads
        if (!mEnv) {
            LOGV("Initializing JNI env for thread: %d", pthread_self());
            JavaVMAttachArgs args;
            args.version = JNI_VERSION_1_6;
            args.name = NULL;
            args.group = NULL;
            mVm->AttachCurrentThread(&mEnv, &args);
        }
        return mEnv;
    }
private:
    jobject mSession;
    JavaVM *mVm;
    JNIEnv *mEnv;
};

Session *getNativeSession(JNIEnv *env, jobject object) {
    const jint handle = env->GetIntField(object, sSessionHandleField);
    return reinterpret_cast<Session *>(handle);
}

extern "C" JNIEXPORT void JNICALL Java_com_wigwamlabs_spotify_app_SpotifySession_nativeInitClass(JNIEnv *env, jclass klass) {
    LOGV("nativeInitClass()");

    if (sSessionHandleField == 0) {
        sSessionHandleField = env->GetFieldID(klass, "mHandle", "I");
    }
    if (sSessionOnConnectionStateUpdatedMethod == 0) {
        sSessionOnConnectionStateUpdatedMethod = env->GetMethodID(klass, "onConnectionStateUpdated", "(I)V");
    }
}

extern "C" JNIEXPORT jint JNICALL Java_com_wigwamlabs_spotify_app_SpotifySession_nativeCreate(JNIEnv *env, jobject self, jobject context, jstring settingsPath, jstring cachePath, jstring deviceId) {
    LOGV("nativeCreate()");

    Context *c = getNativeContext(env, context);
    const char *settingsPathStr = env->GetStringUTFChars(settingsPath, NULL);
    const char *cachePathStr = env->GetStringUTFChars(cachePath, NULL);
    const char *deviceIdStr = env->GetStringUTFChars(deviceId, NULL);

    SessionCallbackJNI *callback = new SessionCallbackJNI(env, self);

    sp_error error;
    Session *session = Session::create(c, callback, settingsPathStr, cachePathStr, deviceIdStr, error);

    env->ReleaseStringUTFChars(deviceId, deviceIdStr);
    env->ReleaseStringUTFChars(cachePath, cachePathStr);
    env->ReleaseStringUTFChars(settingsPath, settingsPathStr);

    if (error != SP_ERROR_OK) {
        ExceptionUtils::throwException(env, ExceptionUtils::RUNTIME_EXCEPTION, sp_error_message(error));
        return 0;
    }

    return reinterpret_cast<jint>(session);
}

extern "C" JNIEXPORT void JNICALL Java_com_wigwamlabs_spotify_app_SpotifySession_nativeDestroy(JNIEnv *env, jobject self) {
    LOGV("nativeDestroy()");

    Session *session = getNativeSession(env, self);
    sp_error error = session->destroy();
    delete session;

    if (error != SP_ERROR_OK) {
        ExceptionUtils::throwException(env, ExceptionUtils::RUNTIME_EXCEPTION, sp_error_message(error));
        return;
    }
}

extern "C" JNIEXPORT jboolean JNICALL Java_com_wigwamlabs_spotify_app_SpotifySession_nativeRelogin(JNIEnv *env, jobject self) {
    LOGV("nativeRelogin()");

    Session *session = getNativeSession(env, self);
    return session->relogin();
}

extern "C" JNIEXPORT void JNICALL Java_com_wigwamlabs_spotify_app_SpotifySession_nativeLogin(JNIEnv *env, jobject self, jstring username, jstring password, jboolean rememberMe) {
    LOGV("nativeLogin()");

    Session *session = getNativeSession(env, self);
    const char *usernameStr = env->GetStringUTFChars(username, NULL);
    const char *passwordStr = env->GetStringUTFChars(password, NULL);

    sp_error error = session->login(usernameStr, passwordStr, rememberMe);

    env->ReleaseStringUTFChars(password, passwordStr);
    env->ReleaseStringUTFChars(username, usernameStr);

    if (error != SP_ERROR_OK) {
        ExceptionUtils::throwException(env, ExceptionUtils::RUNTIME_EXCEPTION, sp_error_message(error));
        return;
    }
}

extern "C" JNIEXPORT jint JNICALL Java_com_wigwamlabs_spotify_app_SpotifySession_nativeGetPlaylistContainer(JNIEnv *env, jobject self) {
    LOGV("nativeLogin()");

    Session *session = getNativeSession(env, self);

    PlaylistContainer *container = session->getPlaylistContainer();

    return reinterpret_cast<int>(container);
}
