#define LOG_TAG "com_wigwamlabs_spotify_PlaylistContainer"
//#define LOG_NDEBUG 0
#include "log.h"

#include <jni.h>
#include "utils.h"
#include "ExceptionUtils.h"
#include "wigwamlabs/PlaylistContainer.h"

using namespace wigwamlabs;

jfieldID sPlaylistContainerHandleField = 0;

PlaylistContainer *getNativePlaylistContainer(JNIEnv *env, jobject object) {
    const jint handle = env->GetIntField(object, sPlaylistContainerHandleField);
    return reinterpret_cast<PlaylistContainer *>(handle);
}

JNI_STATIC_METHOD(void, com_wigwamlabs_spotify_PlaylistContainer, nativeInitClass) {
    LOGV("nativeInitClass()");

    if (sPlaylistContainerHandleField == 0) {
        sPlaylistContainerHandleField = env->GetFieldID(klass, "mHandle", "I");
    }
}

JNI_METHOD(void, com_wigwamlabs_spotify_PlaylistContainer, nativeDestroy) {
    LOGV("nativeDestroy()");

    PlaylistContainer *container = getNativePlaylistContainer(env, self);
    sp_error error = container->destroy();
    delete container;

    if (error != SP_ERROR_OK) {
        ExceptionUtils::throwException(env, ExceptionUtils::RUNTIME_EXCEPTION, sp_error_message(error));
        return;
    }
}

JNI_METHOD(jint, com_wigwamlabs_spotify_PlaylistContainer, nativeGetCount) {
    LOGV("nativeGetCount()");

    PlaylistContainer *container = getNativePlaylistContainer(env, self);
    return container->getCount();
}

JNI_METHOD_ARGS(jint, com_wigwamlabs_spotify_PlaylistContainer, nativeGetPlaylist, jint index) {
    LOGV("nativeGetPlaylist(%d)", index);

    PlaylistContainer *container = getNativePlaylistContainer(env, self);

    Playlist *playlist = container->getPlaylist(index);

    return reinterpret_cast<jint>(playlist);
}
