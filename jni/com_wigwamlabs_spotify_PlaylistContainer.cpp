#define LOG_TAG "com_wigwamlabs_spotify_PlaylistContainer"
//#define LOG_NDEBUG 0
#include "log.h"

#include <jni.h>
#include "ExceptionUtils.h"
#include "wigwamlabs/PlaylistContainer.h"

using namespace wigwamlabs;

jfieldID sPlaylistContainerHandleField = 0;

PlaylistContainer *getNativePlaylistContainer(JNIEnv *env, jobject object) {
    const jint handle = env->GetIntField(object, sPlaylistContainerHandleField);
    return reinterpret_cast<PlaylistContainer *>(handle);
}

extern "C" JNIEXPORT void JNICALL Java_com_wigwamlabs_spotify_PlaylistContainer_nativeInitClass(JNIEnv *env, jclass klass) {
    LOGV("nativeInitClass()");

    if (sPlaylistContainerHandleField == 0) {
        sPlaylistContainerHandleField = env->GetFieldID(klass, "mHandle", "I");
    }
}

extern "C" JNIEXPORT void JNICALL Java_com_wigwamlabs_spotify_PlaylistContainer_nativeDestroy(JNIEnv *env, jobject self) {
    LOGV("nativeDestroy()");

    PlaylistContainer *container = getNativePlaylistContainer(env, self);
    sp_error error = container->destroy();
    delete container;

    if (error != SP_ERROR_OK) {
        ExceptionUtils::throwException(env, ExceptionUtils::RUNTIME_EXCEPTION, sp_error_message(error));
        return;
    }
}

extern "C" JNIEXPORT jint JNICALL Java_com_wigwamlabs_spotify_PlaylistContainer_nativeGetCount(JNIEnv *env, jobject self) {
    LOGV("nativeGetCount()");

    PlaylistContainer *container = getNativePlaylistContainer(env, self);
    return container->getCount();
}
