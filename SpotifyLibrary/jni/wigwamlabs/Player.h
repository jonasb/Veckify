#ifndef WIGWAMLABS_PLAYER_H_INCLUDED
#define WIGWAMLABS_PLAYER_H_INCLUDED

#include <libspotify/api.h>
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>
#include <pthread.h>

namespace wigwamlabs {

class Track;

enum PlayerState {
    STATE_STARTED,
    STATE_PLAYING,
    STATE_PAUSED_USER,
    STATE_PAUSED_NOISY,
    STATE_PAUSED_AUDIOFOCUS,
    STATE_STOPPED,
};

class PlayerCallback {
public:
    virtual void onStateChanged(PlayerState state) = 0;
    virtual void onTrackProgress(int secondsPlayed, int secondsDuration) = 0;
    virtual void onCurrentTrackUpdated(bool playNext) = 0;
    virtual void onPlayTokenLost() = 0;
};

static const int SAMPLES_PER_SECOND = 44100;
static const int CHANNEL_COUNT = 2;
static const int BYTES_PER_SAMPLE = CHANNEL_COUNT * sizeof(int16_t);
static const int BYTES_PER_MS = SAMPLES_PER_SECOND * BYTES_PER_SAMPLE / 1000;
static const int BUFFER_COUNT = 2;
static const int BUFFER_SIZE_MS = 250;
static const int BUFFER_SIZE = BYTES_PER_MS * BUFFER_SIZE_MS;

class Player {
public:
    static Player *create(sp_session *session);
    sp_error destroy();
    ~Player();

    void setCallback(PlayerCallback *callback);

    int onMusicDelivery(const sp_audioformat *format, const void *frames, int numFrames);
    void onPlayTokenLost();

    PlayerState getState() const;
    void play(Track *track);
    bool pause(PlayerState reason);
    void resume();
    void seek(int progressMs);
    void setNextTrack(Track *track);
    void playNextTrack();

private:
    struct Buffer {
        int16_t buffer[BUFFER_SIZE];
        int size;
        bool consumed;
    };
    static void bqPlayerCallback(SLAndroidSimpleBufferQueueItf bq, void *context);
    Player(sp_session *session);
    void setState(PlayerState state);
    bool initialize();
    void play(sp_track *track, bool playNext);
    void setTrackProgressMs(int progressMs);

private:
    sp_session *mSession;
    PlayerCallback *mCallback;

    pthread_mutex_t mBufferMutex;
    Buffer mBuffers[BUFFER_COUNT];
    int mConsumeBuffer;
    int mProduceBuffer;

    SLObjectItf mEngineObject;
    SLObjectItf mOutputMixObject;
    SLObjectItf mBqPlayerObject;
    SLPlayItf mBqPlayerPlay;
    SLAndroidSimpleBufferQueueItf mBqPlayerBufferQueue;

    int mTrackDurationMs;
    int mTrackProgressBytes;
    int mTrackProgressReportedSec;

    PlayerState mState;
    sp_track *mTrackNext;
    bool mPrefetchRequested;
};

}

#endif // WIGWAMLABS_PLAYER_H_INCLUDED
