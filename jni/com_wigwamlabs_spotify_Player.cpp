#define LOG_TAG "com_wigwamlabs_spotify_Player"
//#define LOG_NDEBUG 0
#include "log.h"

#include <jni.h>
#include "utils.h"
#include "ExceptionUtils.h"
#include "JNIEnvProvider.h"
#include "wigwamlabs/Player.h"

using namespace wigwamlabs;

jfieldID sPlayerHandleField = 0;
jmethodID sPlayerOnTrackProgressMethod = 0;
jmethodID sPlayerOnCurrentTrackUpdatedMethod = 0;

class PlayerCallbackJNI : public PlayerCallback {
public:
    PlayerCallbackJNI(JNIEnv *env, jobject player) :
        mProvider(JNIEnvProvider::instance(env)) {
        mPlayer = env->NewGlobalRef(player);
    }

    ~PlayerCallbackJNI() {
        mProvider->getEnv()->DeleteGlobalRef(mPlayer);
    }

    void onTrackProgress(int secondsPlayed, int secondsDuration) {
        LOGV("%s %ds (%ds)", secondsPlayed, secondsDuration);
        mProvider->getEnv()->CallVoidMethod(mPlayer, sPlayerOnTrackProgressMethod, secondsPlayed, secondsDuration);
    }

    void onCurrentTrackUpdated(bool playNext) {
        LOGV("%s %d", __func__, playNext);
        mProvider->getEnv()->CallVoidMethod(mPlayer, sPlayerOnCurrentTrackUpdatedMethod, playNext);
    }
private:
    JNIEnvProvider *mProvider;
    jobject mPlayer;
};

Player *getNativePlayer(JNIEnv *env, jobject object) {
    const jint handle = env->GetIntField(object, sPlayerHandleField);
    return reinterpret_cast<Player *>(handle);
}

JNI_STATIC_METHOD(void, com_wigwamlabs_spotify_Player, nativeInitClass) {
    LOGV("nativeInitClass()");

    if (sPlayerHandleField == 0) {
        sPlayerHandleField = env->GetFieldID(klass, "mHandle", "I");
    }
    if (sPlayerOnTrackProgressMethod == 0) {
        sPlayerOnTrackProgressMethod = env->GetMethodID(klass, "onTrackProgress", "(II)V");
    }
    if (sPlayerOnCurrentTrackUpdatedMethod == 0) {
        sPlayerOnCurrentTrackUpdatedMethod = env->GetMethodID(klass, "onCurrentTrackUpdated", "(Z)V");
    }
}

JNI_METHOD(void, com_wigwamlabs_spotify_Player, nativeInitInstance) {
    LOGV("nativeInitInstance()");

    Player *player = getNativePlayer(env, self);
    PlayerCallbackJNI *callback = new PlayerCallbackJNI(env, self);
    player->setCallback(callback);
}

JNI_METHOD_ARGS(void, com_wigwamlabs_spotify_Player, nativePlay, jobject trackHandle) {
    LOGV("nativePlay");

    Player *player = getNativePlayer(env, self);
    Track *track = getNativeTrack(env, trackHandle);

    player->play(track);
}

JNI_METHOD_ARGS(void, com_wigwamlabs_spotify_Player, nativeSetNextTrack, jobject trackHandle) {
    LOGV("nativeSetNextTrack");

    Player *player = getNativePlayer(env, self);
    Track *track = getNativeTrack(env, trackHandle);

    player->setNextTrack(track);
}

JNI_METHOD_ARGS(void, com_wigwamlabs_spotify_Player, nativeSeek, jint progressMs) {
    LOGV("nativeSeek");

    Player *player = getNativePlayer(env, self);
    player->seek(progressMs);
}
