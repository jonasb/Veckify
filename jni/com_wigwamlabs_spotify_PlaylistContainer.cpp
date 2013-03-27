#define LOG_TAG "com_wigwamlabs_spotify_PlaylistContainer"
//#define LOG_NDEBUG 0
#include "log.h"

#include <jni.h>
#include <pthread.h>
#include "utils.h"
#include "ExceptionUtils.h"
#include "wigwamlabs/PlaylistContainer.h"

using namespace wigwamlabs;

jfieldID sPlaylistContainerHandleField = 0;
jmethodID sPlaylistContainerOnContainerLoaded = 0;

class PlaylistContainerCallbackJNI : public PlaylistContainerCallback {
public:
    PlaylistContainerCallbackJNI(JNIEnv *env, jobject playlistContainer) :
        mEnv(NULL) {
        mPlaylistContainer = env->NewGlobalRef(playlistContainer);
        env->GetJavaVM(&mVm);
    }

    ~PlaylistContainerCallbackJNI() {
        JNIEnv *env = getEnv();
        if (env) {
            env->DeleteGlobalRef(mPlaylistContainer);
        }
    }

    void onContainerLoaded() {
        LOGV(__func__);
        getEnv()->CallVoidMethod(mPlaylistContainer, sPlaylistContainerOnContainerLoaded);
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
    jobject mPlaylistContainer;
    JavaVM *mVm;
    JNIEnv *mEnv;
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
    if (sPlaylistContainerOnContainerLoaded == 0) {
        sPlaylistContainerOnContainerLoaded = env->GetMethodID(klass, "onContainerLoaded", "()V");
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
