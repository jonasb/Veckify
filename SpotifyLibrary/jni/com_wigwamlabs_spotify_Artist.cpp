#define LOG_TAG "com_wigwamlabs_spotify_Artist"
//#define LOG_NDEBUG 0
#include "log.h"

#include <jni.h>
#include "utils.h"
#include "ExceptionUtils.h"
#include "wigwamlabs/Artist.h"

using namespace wigwamlabs;

jfieldID sArtistHandleField = 0;

Artist *getNativeArtist(JNIEnv *env, jobject object) {
    const jint handle = env->GetIntField(object, sArtistHandleField);
    return reinterpret_cast<Artist *>(handle);
}

JNI_STATIC_METHOD(void, com_wigwamlabs_spotify_Artist, nativeInitClass) {
    LOGV("nativeInitClass()");

    if (sArtistHandleField == 0) {
        sArtistHandleField = env->GetFieldID(klass, "mHandle", "I");
    }
}

JNI_METHOD(void, com_wigwamlabs_spotify_Artist, nativeDestroy) {
    LOGV("nativeDestroy()");

    Artist *artist = getNativeArtist(env, self);
    sp_error error = artist->destroy();
    delete artist;

    if (error != SP_ERROR_OK) {
        ExceptionUtils::throwException(env, ExceptionUtils::RUNTIME_EXCEPTION, sp_error_message(error));
        return;
    }
}

JNI_METHOD(jstring, com_wigwamlabs_spotify_Artist, nativeGetName) {
    LOGV("nativeGetName()");

    Artist *artist = getNativeArtist(env, self);
    const char *nameStr = artist->getName();

    jstring name = env->NewStringUTF(nameStr);

    return name;
}

