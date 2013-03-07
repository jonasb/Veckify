#define LOG_TAG "com_wigwamlabs_spotify_app_SpotifySession"
#define LOG_NDEBUG 0
#include "log.h"

#include <jni.h>
#include "ExceptionUtils.h"
#include "utils.h"
#include "wigwamlabs/Session.h"

using namespace wigwamlabs;

jfieldID sSessionHandleField = 0;

Session *getNativeSession(JNIEnv *env, jobject object) {
    const jint handle = env->GetIntField(object, sSessionHandleField);
    return reinterpret_cast<Session *>(handle);
}

extern "C" JNIEXPORT void JNICALL Java_com_wigwamlabs_spotify_app_SpotifySession_nativeInitClass(JNIEnv *env, jclass klass) {
    LOGV("nativeInitClass()");

    if (sSessionHandleField == 0) {
        sSessionHandleField = env->GetFieldID(klass, "mHandle", "I");
    }
}

extern "C" JNIEXPORT jint JNICALL Java_com_wigwamlabs_spotify_app_SpotifySession_nativeCreate(JNIEnv *env, jobject self, jobject context, jstring settingsPath, jstring cachePath, jstring deviceId) {
    LOGV("nativeCreate()");

    Context *c = getNativeContext(env, context);
    const char *settingsPathStr = env->GetStringUTFChars(settingsPath, NULL);
    const char *cachePathStr = env->GetStringUTFChars(cachePath, NULL);
    const char *deviceIdStr = env->GetStringUTFChars(deviceId, NULL);

    sp_error error;
    Session *session = Session::create(c, settingsPathStr, cachePathStr, deviceIdStr, error);

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
