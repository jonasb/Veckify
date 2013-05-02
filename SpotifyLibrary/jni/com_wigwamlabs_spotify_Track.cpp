#define LOG_TAG "com_wigwamlabs_spotify_Track"
//#define LOG_NDEBUG 0
#include "log.h"

#include "utils.h"
#include "ExceptionUtils.h"
#include "wigwamlabs/Track.h"

using namespace wigwamlabs;

jfieldID sTrackHandleField = 0;

Track *getNativeTrack(JNIEnv *env, jobject object) {
    if (object == 0) {
        return NULL;
    }
    const jint handle = env->GetIntField(object, sTrackHandleField);
    return reinterpret_cast<Track *>(handle);
}

JNI_STATIC_METHOD(void, com_wigwamlabs_spotify_Track, nativeInitClass) {
    LOGV("nativeInitClass()");

    if (sTrackHandleField == 0) {
        sTrackHandleField = env->GetFieldID(klass, "mHandle", "I");
    }
}

JNI_METHOD_ARGS(jint, com_wigwamlabs_spotify_Track, nativeCreate, jstring link) {
    LOGV("nativeCreate()");
    const char *linkStr = env->GetStringUTFChars(link, NULL);

    Track *instance = Track::create(linkStr);

    env->ReleaseStringUTFChars(link, linkStr);

    return reinterpret_cast<jint>(instance);
}

JNI_METHOD(jint, com_wigwamlabs_spotify_Track, nativeClone) {
    LOGV("nativeClone()");

    Track *track = getNativeTrack(env, self);
    Track *clone = track->clone();

    return reinterpret_cast<jint>(clone);
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

JNI_METHOD(jint, com_wigwamlabs_spotify_Track, nativeGetArtistCount) {
    LOGV("nativeGetArtistCount()");

    Track *track = getNativeTrack(env, self);
    return track->getArtistCount();
}

JNI_METHOD_ARGS(jint, com_wigwamlabs_spotify_Track, nativeGetArtist, jint index) {
    LOGV("nativeGetArtist(%d)", index);

    Track *track = getNativeTrack(env, self);

    Artist *artist = track->getArtist(index);

    return reinterpret_cast<jint>(artist);
}

JNI_METHOD_ARGS(jstring, com_wigwamlabs_spotify_Track, nativeGetImageLink, jint size) {
    LOGV("nativeGetImageLink(%d)", size);
    Track *track = getNativeTrack(env, self);
    sp_link *link = track->getImageLink((sp_image_size)size);
    return convertLinkToString(env, link);
}

JNI_METHOD_ARGS(jint, com_wigwamlabs_spotify_Track, nativeGetAvailability, jobject sessionHandle) {
    LOGV("nativeGetAvailability()");

    Track *track = getNativeTrack(env, self);
    Session *session = getNativeSession(env, sessionHandle);

    return track->getAvailability(session);
}
