#define LOG_TAG "com_wigwamlabs_spotify_app_SpotifySession"
#define LOG_NDEBUG 0
#include "log.h"

#include <jni.h>
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

extern "C" JNIEXPORT jint JNICALL Java_com_wigwamlabs_spotify_app_SpotifySession_nativeCreate(JNIEnv *env, jobject self, jobject context) {
    LOGV("nativeCreate()");

    Context *c = getNativeContext(env, context);

    return reinterpret_cast<jint>(new Session(c));
}

extern "C" JNIEXPORT void JNICALL Java_com_wigwamlabs_spotify_app_SpotifySession_nativeDestroy(JNIEnv *env, jobject self) {
    LOGV("nativeDestroy()");

    Session *session = getNativeSession(env, self);
    delete session;
}
