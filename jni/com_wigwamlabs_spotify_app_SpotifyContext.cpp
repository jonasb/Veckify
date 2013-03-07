#define LOG_TAG "com_wigwamlabs_spotify_app_SpotifyContext"
#define LOG_NDEBUG 0
#include "log.h"

#include <jni.h>
#include "wigwamlabs/Context.h"

using namespace wigwamlabs;

jfieldID sContextHandleField = 0;

Context *getNativeContext(JNIEnv *env, jobject object) {
    const jint handle = env->GetIntField(object, sContextHandleField);
    return reinterpret_cast<Context *>(handle);
}

extern "C" JNIEXPORT void JNICALL Java_com_wigwamlabs_spotify_app_SpotifyContext_nativeInitClass(JNIEnv *env, jclass klass) {
    LOGV("nativeInitClass()");

    if (sContextHandleField == 0) {
        sContextHandleField = env->GetFieldID(klass, "mHandle", "I");
    }
}

extern "C" JNIEXPORT jint JNICALL Java_com_wigwamlabs_spotify_app_SpotifyContext_nativeCreate(JNIEnv *env, jobject self) {
    LOGV("nativeCreate()");

    return reinterpret_cast<jint>(new Context);
}

extern "C" JNIEXPORT void JNICALL Java_com_wigwamlabs_spotify_app_SpotifyContext_nativeDestroy(JNIEnv *env, jobject self) {
    LOGV("nativeDestroy()");

    Context *context = getNativeContext(env, self);
    delete context;
}
