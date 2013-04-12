#define LOG_TAG "com_wigwamlabs_spotify_PlaylistContainer"
//#define LOG_NDEBUG 0
#include "log.h"

#include <jni.h>
#include <pthread.h>
#include "utils.h"
#include "ExceptionUtils.h"
#include "JNIEnvProvider.h"
#include "wigwamlabs/PlaylistContainer.h"

using namespace wigwamlabs;

jfieldID sPlaylistContainerHandleField = 0;
jmethodID sPlaylistContainerOnContainerLoadedMethod = 0;
jmethodID sPlaylistContainerOnPlaylistMovedMethod = 0;

class PlaylistContainerCallbackJNI : public PlaylistContainerCallback {
public:
    PlaylistContainerCallbackJNI(JNIEnv *env, jobject playlistContainer) :
        mProvider(JNIEnvProvider::instance(env)) {
        mPlaylistContainer = env->NewGlobalRef(playlistContainer);
    }

    ~PlaylistContainerCallbackJNI() {
        mProvider->getEnv()->DeleteGlobalRef(mPlaylistContainer);
    }

    void onPlaylistMoved(int oldPosition, int newPosition) {
        LOGV("%s (%d -> %d)", __func__, oldPosition, newPosition);
        mProvider->getEnv()->CallVoidMethod(mPlaylistContainer, sPlaylistContainerOnPlaylistMovedMethod, oldPosition, newPosition);
    }

    void onContainerLoaded() {
        LOGV("%s", __func__);
        mProvider->getEnv()->CallVoidMethod(mPlaylistContainer, sPlaylistContainerOnContainerLoadedMethod);
    }

private:
    JNIEnvProvider *mProvider;
    jobject mPlaylistContainer;
};

PlaylistContainer *getNativePlaylistContainer(JNIEnv *env, jobject object) {
    const jint handle = env->GetIntField(object, sPlaylistContainerHandleField);
    return reinterpret_cast<PlaylistContainer *>(handle);
}

JNI_STATIC_METHOD(void, com_wigwamlabs_spotify_PlaylistContainer, nativeInitClass) {
    LOGV("nativeInitClass()");

    if (sPlaylistContainerHandleField == 0) {
        sPlaylistContainerHandleField = env->GetFieldID(klass, "mHandle", "I");
    }
    if (sPlaylistContainerOnContainerLoadedMethod == 0) {
        sPlaylistContainerOnContainerLoadedMethod = env->GetMethodID(klass, "onContainerLoaded", "()V");
    }
    if (sPlaylistContainerOnPlaylistMovedMethod == 0) {
        sPlaylistContainerOnPlaylistMovedMethod = env->GetMethodID(klass, "onPlaylistMoved", "(II)V");
    }
}

JNI_METHOD(void, com_wigwamlabs_spotify_PlaylistContainer, nativeInitInstance) {
    LOGV("nativeInitInstance()");

    PlaylistContainer *container = getNativePlaylistContainer(env, self);
    PlaylistContainerCallbackJNI *callback = new PlaylistContainerCallbackJNI(env, self);
    container->setCallback(callback);
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

JNI_METHOD_ARGS(jint, com_wigwamlabs_spotify_PlaylistContainer, nativeGetPlaylistType, jint index) {
    LOGV("nativeGetPlaylistType(%d)", index);

    PlaylistContainer *container = getNativePlaylistContainer(env, self);

    return container->getPlaylistType(index);
}

JNI_METHOD_ARGS(jint, com_wigwamlabs_spotify_PlaylistContainer, nativeGetPlaylist, jint index) {
    LOGV("nativeGetPlaylist(%d)", index);

    PlaylistContainer *container = getNativePlaylistContainer(env, self);

    Playlist *item = container->getPlaylist(index);

    return reinterpret_cast<jint>(item);
}

JNI_METHOD_ARGS(jint, com_wigwamlabs_spotify_PlaylistContainer, nativeGetFolderStart, jint index) {
    LOGV("nativeGetFolderStart(%d)", index);

    PlaylistContainer *container = getNativePlaylistContainer(env, self);

    FolderStart *item = container->getFolderStart(index);

    return reinterpret_cast<jint>(item);
}

JNI_METHOD_ARGS(jint, com_wigwamlabs_spotify_PlaylistContainer, nativeGetFolderEnd, jint index) {
    LOGV("nativeGetFolderEnd(%d)", index);

    PlaylistContainer *container = getNativePlaylistContainer(env, self);

    FolderEnd *item = container->getFolderEnd(index);

    return reinterpret_cast<jint>(item);
}

JNI_METHOD_ARGS(jint, com_wigwamlabs_spotify_PlaylistContainer, nativeGetPlaceholder, jint index) {
    LOGV("nativeGetPlaceholder(%d)", index);

    PlaylistContainer *container = getNativePlaylistContainer(env, self);

    Placeholder *item = container->getPlaceholder(index);

    return reinterpret_cast<jint>(item);
}
