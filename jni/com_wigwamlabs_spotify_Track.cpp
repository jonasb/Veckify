#define LOG_TAG "com_wigwamlabs_spotify_Track"
//#define LOG_NDEBUG 0
#include "log.h"

#include <jni.h>
#include "utils.h"
#include "ExceptionUtils.h"
#include "wigwamlabs/Track.h"

using namespace wigwamlabs;

jfieldID sTrackHandleField = 0;

Track *getNativeTrack(JNIEnv *env, jobject object) {
    const jint handle = env->GetIntField(object, sTrackHandleField);
    return reinterpret_cast<Track *>(handle);
}

JNI_STATIC_METHOD(void, com_wigwamlabs_spotify_Track, nativeInitClass) {
    LOGV("nativeInitClass()");

    if (sTrackHandleField == 0) {
        sTrackHandleField = env->GetFieldID(klass, "mHandle", "I");
    }
}

JNI_METHOD(void, com_wigwamlabs_spotify_Track, nativeDestroy) {
    LOGV("nativeDestroy()");

    Track *track = getNativeTrack(env, self);
    sp_error error = track->destroy();
    delete track;

    if (error != SP_ERROR_OK) {
        ExceptionUtils::throwException(env, ExceptionUtils::RUNTIME_EXCEPTION, sp_error_message(error));
        return;
    }
}

JNI_METHOD(jstring, com_wigwamlabs_spotify_Track, nativeGetName) {
    LOGV("nativeGetName()");

    Track *track = getNativeTrack(env, self);

    const char *nameStr = track->getName();

    jstring name = env->NewStringUTF(nameStr);

    return name;
}
