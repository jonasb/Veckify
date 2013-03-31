#define LOG_TAG "com_wigwamlabs_spotify_Playlist"
//#define LOG_NDEBUG 0
#include "log.h"

#include <jni.h>
#include "utils.h"
#include "ExceptionUtils.h"
#include "JNIEnvProvider.h"
#include "wigwamlabs/Playlist.h"

using namespace wigwamlabs;

jfieldID sPlaylistHandleField = 0;
jmethodID sPlaylistOnTracksMovedMethod = 0;
jmethodID sPlaylistOnPlaylistUpdateInProgressMethod = 0;

class PlaylistCallbackJNI : public PlaylistCallback {
public:
    PlaylistCallbackJNI(JNIEnv *env, jobject playlist) :
        mProvider(JNIEnvProvider::instance(env)) {
        mPlaylist = env->NewGlobalRef(playlist);
    }

    ~PlaylistCallbackJNI() {
        mProvider->getEnv()->DeleteGlobalRef(mPlaylist);
    }

    void onTracksMoved(const int *oldPositions, int oldPositionCount, int newPosition) {
#if !LOG_NDEBUG
        switch (oldPositionCount) {
        case 0: LOGV("%s ([], %d)", __func__, newPosition); break;
        case 1: LOGV("%s ([%d], %d)", __func__, oldPositions[0], newPosition); break;
        case 2: LOGV("%s ([%d, %d], %d)", __func__, oldPositions[0], oldPositions[1], newPosition); break;
        case 3: LOGV("%s ([%d, %d, %d], %d)", __func__, oldPositions[0], oldPositions[1], oldPositions[2], newPosition); break;
        case 4: LOGV("%s ([%d, %d, %d, %d], %d)", __func__, oldPositions[0], oldPositions[1], oldPositions[2], oldPositions[3], newPosition); break;
        default: LOGV("%s ([%d, %d, %d, %d, %d + %d more], %d)", __func__, oldPositions[0], oldPositions[1], oldPositions[2], oldPositions[3], oldPositions[4], (oldPositionCount - 5), newPosition); break;
        }
#endif
        JNIEnv *env = mProvider->getEnv();
        jintArray array = env->NewIntArray(oldPositionCount);
        if (array) {
            env->SetIntArrayRegion(array, 0, oldPositionCount, oldPositions);
            env->CallVoidMethod(mPlaylist, sPlaylistOnTracksMovedMethod, array, newPosition);

            env->DeleteLocalRef(array);
        }
    }

    void onPlaylistUpdateInProgress(bool done) {
        LOGV("%s (%d)", __func__, done);
        mProvider->getEnv()->CallVoidMethod(mPlaylist, sPlaylistOnPlaylistUpdateInProgressMethod, done);
    }
private:
    JNIEnvProvider *mProvider;
    jobject mPlaylist;
};

Playlist *getNativePlaylist(JNIEnv *env, jobject object) {
    const jint handle = env->GetIntField(object, sPlaylistHandleField);
    return reinterpret_cast<Playlist *>(handle);
}

JNI_STATIC_METHOD(void, com_wigwamlabs_spotify_Playlist, nativeInitClass) {
    LOGV("nativeInitClass()");

    if (sPlaylistHandleField == 0) {
        sPlaylistHandleField = env->GetFieldID(klass, "mHandle", "I");
    }
    if (sPlaylistOnTracksMovedMethod == 0) {
        sPlaylistOnTracksMovedMethod = env->GetMethodID(klass, "onTracksMoved", "([II)V");
    }
    if (sPlaylistOnPlaylistUpdateInProgressMethod == 0) {
        sPlaylistOnPlaylistUpdateInProgressMethod = env->GetMethodID(klass, "onPlaylistUpdateInProgress", "(Z)V");
    }
}

JNI_METHOD(void, com_wigwamlabs_spotify_Playlist, nativeInitInstance) {
    LOGV("nativeInitInstance()");

    Playlist *playlist = getNativePlaylist(env, self);
    PlaylistCallbackJNI *callback = new PlaylistCallbackJNI(env, self);
    playlist->setCallback(callback);
}

JNI_METHOD(void, com_wigwamlabs_spotify_Playlist, nativeDestroy) {
    LOGV("nativeDestroy()");

    Playlist *playlist = getNativePlaylist(env, self);
    sp_error error = playlist->destroy();
    delete playlist;

    if (error != SP_ERROR_OK) {
        ExceptionUtils::throwException(env, ExceptionUtils::RUNTIME_EXCEPTION, sp_error_message(error));
        return;
    }
}

JNI_METHOD(jint, com_wigwamlabs_spotify_Playlist, nativeClone) {
    LOGV("nativeClone()");

    Playlist *playlist = getNativePlaylist(env, self);
    Playlist *clone = playlist->clone();

    return reinterpret_cast<jint>(clone);
}

JNI_METHOD(jstring, com_wigwamlabs_spotify_Playlist, nativeGetName) {
    LOGV("nativeGetName()");

    Playlist *playlist = getNativePlaylist(env, self);

    const char *nameStr = playlist->getName();

    jstring name = env->NewStringUTF(nameStr);

    return name;
}

JNI_METHOD(jint, com_wigwamlabs_spotify_Playlist, nativeGetCount) {
    LOGV("nativeGetCount()");

    Playlist *playlist = getNativePlaylist(env, self);

    int count = playlist->getCount();
    LOGV("count = %d", count);
    return count;
}

JNI_METHOD_ARGS(jint, com_wigwamlabs_spotify_Playlist, nativeGetTrack, jint index) {
    LOGV("nativeGetTrack(%d)", index);

    Playlist *playlist = getNativePlaylist(env, self);

    Track *track = playlist->getTrack(index);

    return reinterpret_cast<jint>(track);
}
