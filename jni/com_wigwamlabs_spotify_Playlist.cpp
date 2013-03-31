#define LOG_TAG "com_wigwamlabs_spotify_Playlist"
//#define LOG_NDEBUG 0
#include "log.h"

#include <jni.h>
#include "utils.h"
#include "ExceptionUtils.h"
#include "wigwamlabs/Playlist.h"

using namespace wigwamlabs;

jfieldID sPlaylistHandleField = 0;

Playlist *getNativePlaylist(JNIEnv *env, jobject object) {
    const jint handle = env->GetIntField(object, sPlaylistHandleField);
    return reinterpret_cast<Playlist *>(handle);
}

JNI_STATIC_METHOD(void, com_wigwamlabs_spotify_Playlist, nativeInitClass) {
    LOGV("nativeInitClass()");

    if (sPlaylistHandleField == 0) {
        sPlaylistHandleField = env->GetFieldID(klass, "mHandle", "I");
    }
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