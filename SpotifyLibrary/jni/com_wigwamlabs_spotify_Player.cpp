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
jmethodID sPlayerOnStateChangedMethod = 0;
jmethodID sPlayerOnTrackProgressMethod = 0;
jmethodID sPlayerOnPlayTokenLostMethod = 0;

class PlayerCallbackJNI : public PlayerCallback {
public:
    PlayerCallbackJNI(JNIEnv *env, jobject player) :
        mProvider(JNIEnvProvider::instance(env)) {
        mPlayer = env->NewGlobalRef(player);
    }

    ~PlayerCallbackJNI() {
        mProvider->getEnv()->DeleteGlobalRef(mPlayer);
    }

    void onStateChanged(PlayerState state) {
        LOGV("%s %d", __func__, state);
        mProvider->getEnv()->CallVoidMethod(mPlayer, sPlayerOnStateChangedMethod, state);
    }

    void onTrackProgress(int secondsPlayed, int secondsDuration) {
        LOGV("%s %ds (%ds)", __func__, secondsPlayed, secondsDuration);
        mProvider->getEnv()->CallVoidMethod(mPlayer, sPlayerOnTrackProgressMethod, secondsPlayed, secondsDuration);
    }

    void onPlayTokenLost() {
        LOGV(__func__);
        mProvider->getEnv()->CallVoidMethod(mPlayer, sPlayerOnPlayTokenLostMethod);
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
    if (sPlayerOnStateChangedMethod == 0) {
        sPlayerOnStateChangedMethod = env->GetMethodID(klass, "onStateChanged", "(I)V");
    }
    if (sPlayerOnTrackProgressMethod == 0) {
        sPlayerOnTrackProgressMethod = env->GetMethodID(klass, "onTrackProgress", "(II)V");
    }
    if (sPlayerOnPlayTokenLostMethod == 0) {
        sPlayerOnPlayTokenLostMethod = env->GetMethodID(klass, "onPlayTokenLost", "()V");
    }
}

JNI_METHOD(void, com_wigwamlabs_spotify_Player, nativeInitInstance) {
    LOGV("nativeInitInstance()");

    Player *player = getNativePlayer(env, self);
    PlayerCallbackJNI *callback = new PlayerCallbackJNI(env, self);
    player->setCallback(callback);
}

JNI_METHOD(jint, com_wigwamlabs_spotify_Player, nativeGetState) {
    LOGV("nativeGetState()");

    Player *player = getNativePlayer(env, self);

    return player->getState();
}

JNI_METHOD_ARGS(void, com_wigwamlabs_spotify_Player, nativePlay, jobject trackHandle) {
    LOGV("nativePlay()");

    Player *player = getNativePlayer(env, self);
    Track *track = getNativeTrack(env, trackHandle);

    player->play(track);
}

JNI_METHOD_ARGS(void, com_wigwamlabs_spotify_Player, nativePrefetchTrack, jobject trackHandle) {
    LOGV("nativePrefetchTrack()");

    Player *player = getNativePlayer(env, self);
    Track *track = getNativeTrack(env, trackHandle);

    player->prefetchTrack(track);
}

JNI_METHOD_ARGS(void, com_wigwamlabs_spotify_Player, nativePause, jint reasonState) {
    LOGV("nativePause(%d)", reasonState);

    Player *player = getNativePlayer(env, self);

    player->pause((PlayerState) reasonState);
}

JNI_METHOD(void, com_wigwamlabs_spotify_Player, nativeResume) {
    LOGV("nativeResume()");

    Player *player = getNativePlayer(env, self);

    player->resume();
}

JNI_METHOD_ARGS(void, com_wigwamlabs_spotify_Player, nativeSeek, jint progressMs) {
    LOGV("nativeSeek");

    Player *player = getNativePlayer(env, self);
    player->seek(progressMs);
}
