#define LOG_TAG "Player"
#define LOG_NDEBUG 0
#include "log.h"

#include "Player.h"
#include "Track.h"

namespace wigwamlabs {

#define LOGW_IF_ERROR(result, msg) \
    do { \
        if (result != SL_RESULT_SUCCESS) { \
            LOGW("Error %d: %s", result, msg); \
        } \
    } while (false)

Player *Player::create(sp_session *session) {
    Player *self = new Player(session);
    if (!self->initialize()) {
        LOGE("Failed to initialize player");
        self->destroy();
        delete self;
        self = NULL;
    }
    return self;
}

Player::Player(sp_session *session) :
    mSession(session),
    mCallback(NULL),
    mConsumeBuffer(0),
    mProduceBuffer(0),
    mEngineObject(NULL),
    mOutputMixObject(NULL),
    mBqPlayerObject(NULL),
    mTrackDurationMs(0),
    mTrackProgressBytes(0),
    mTrackProgressReportedSec(0) {
    // init buffers
    pthread_mutex_init(&mBufferMutex, NULL);

    for (int i = 0; i < BUFFER_COUNT; i++) {
        Buffer &buffer = mBuffers[i];
        buffer.size = 0;
        buffer.consumed = true;
    }
}

bool Player::initialize() {
    SLresult result = SL_RESULT_SUCCESS;

    // create/realize engine
    if (result == SL_RESULT_SUCCESS) {
        result = slCreateEngine(&mEngineObject, 0, NULL, 0, NULL, NULL);
    }
    if (result == SL_RESULT_SUCCESS) {
        result = (*mEngineObject)->Realize(mEngineObject, SL_BOOLEAN_FALSE);
    }

    // get the engine interface
    SLEngineItf engineEngine;
    if (result == SL_RESULT_SUCCESS) {
        result = (*mEngineObject)->GetInterface(mEngineObject, SL_IID_ENGINE, &engineEngine);
    }

    // create/realize output mix
    if (result == SL_RESULT_SUCCESS) {
        const SLInterfaceID ids[] = { SL_IID_VOLUME };
        const SLboolean required[] = { SL_BOOLEAN_FALSE };
        result = (*engineEngine)->CreateOutputMix(engineEngine, &mOutputMixObject, sizeof(ids) / sizeof(ids[0]), ids, required);
    }
    if (result == SL_RESULT_SUCCESS) {
        result = (*mOutputMixObject)->Realize(mOutputMixObject, SL_BOOLEAN_FALSE);
    }

    // configure audio source / buffer queue
    SLDataLocator_AndroidSimpleBufferQueue locatorBufferQueue = {
        SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE,
        BUFFER_COUNT
    };
    SLDataFormat_PCM format = {
        SL_DATAFORMAT_PCM,
        CHANNEL_COUNT,
        SL_SAMPLINGRATE_44_1,
        SL_PCMSAMPLEFORMAT_FIXED_16,
        SL_PCMSAMPLEFORMAT_FIXED_16,
        SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT,
        SL_BYTEORDER_LITTLEENDIAN
    };
    SLDataSource audioSource = {
        &locatorBufferQueue,
        &format
    };

    // configure audio sink
    SLDataLocator_OutputMix locatorOutputMix = {
        SL_DATALOCATOR_OUTPUTMIX,
        mOutputMixObject
    };
    SLDataSink sink = {
        &locatorOutputMix,
        NULL
    };

    // create/realize audio player
    if (result == SL_RESULT_SUCCESS) {
        const SLInterfaceID ids[] = { SL_IID_ANDROIDSIMPLEBUFFERQUEUE };
        const SLboolean required[] = { SL_BOOLEAN_TRUE };
        result = (*engineEngine)->CreateAudioPlayer(engineEngine, &mBqPlayerObject, &audioSource, &sink, sizeof(ids) / sizeof(ids[0]), ids, required);
    }
    if (result == SL_RESULT_SUCCESS) {
        result = (*mBqPlayerObject)->Realize(mBqPlayerObject, SL_BOOLEAN_FALSE);
    }

    // get the play interface
    if (result == SL_RESULT_SUCCESS) {
        result = (*mBqPlayerObject)->GetInterface(mBqPlayerObject, SL_IID_PLAY, &mBqPlayerPlay);
    }

    // get the buffer queue interface
    if (result == SL_RESULT_SUCCESS) {
        result = (*mBqPlayerObject)->GetInterface(mBqPlayerObject, SL_IID_BUFFERQUEUE, &mBqPlayerBufferQueue);
    }

    // register callback on the buffer queue
    if (result == SL_RESULT_SUCCESS) {
        result = (*mBqPlayerBufferQueue)->RegisterCallback(mBqPlayerBufferQueue, bqPlayerCallback, this);
    }

    // set the player's state to playing
    if (result == SL_RESULT_SUCCESS) {
        result = (*mBqPlayerPlay)->SetPlayState(mBqPlayerPlay, SL_PLAYSTATE_PLAYING);
    }

    //
    return (result == SL_RESULT_SUCCESS);
}

sp_error Player::destroy() {
    LOGV(__func__);
    sp_error error = SP_ERROR_OK;

    // destroy buffer queue audio player object, and invalidate all associated interfaces
    if (mBqPlayerObject != NULL) {
        (*mBqPlayerObject)->Destroy(mBqPlayerObject);
        mBqPlayerObject = NULL;
        mBqPlayerPlay = NULL;
        mBqPlayerBufferQueue = NULL;
    }

    // destroy output mix object, and invalidate all associated interfaces
    if (mOutputMixObject != NULL) {
        (*mOutputMixObject)->Destroy(mOutputMixObject);
        mOutputMixObject = NULL;
    }

    // destroy engine object, and invalidate all associated interfaces
    if (mEngineObject != NULL) {
        (*mEngineObject)->Destroy(mEngineObject);
        mEngineObject = NULL;
    }

    pthread_mutex_destroy(&mBufferMutex);

    return error;
}

Player::~Player() {
    delete mCallback;
}

void Player::setCallback(PlayerCallback *callback) {
    if (mCallback) {
        delete mCallback;
    }
    mCallback = callback;
}

void Player::play(Track *track) {
    mTrackProgressBytes = 0;
    sp_session_player_load(mSession, track->getTrack());
    sp_session_player_play(mSession, true);
    mTrackDurationMs = track->getDurationMs();
    setTrackProgressMs(0);
}

void Player::seek(int progressMs) {
    sp_error error = sp_session_player_seek(mSession, progressMs);
    if (error == SP_ERROR_OK) {
        setTrackProgressMs(progressMs);
        mTrackProgressBytes = progressMs * BYTES_PER_MS;
    }
}

int Player::onMusicDelivery(const sp_audioformat *format, const void *frames, int numFrames) {
    int bytesConsumed = 0;
    if (numFrames == 0) {
        return bytesConsumed;
    }

    pthread_mutex_lock(&mBufferMutex);

    Buffer &buffer = mBuffers[mProduceBuffer];
    if (buffer.consumed) { // ready to accept more samples
        // calculate how much to fill
        const int bytesAvailable = numFrames * BYTES_PER_SAMPLE;
        if (buffer.size + bytesAvailable <= BUFFER_SIZE) {
            bytesConsumed = bytesAvailable;
        } else {
            bytesConsumed = BUFFER_SIZE - buffer.size;
        }
        bytesConsumed = (bytesConsumed / BYTES_PER_SAMPLE) * BYTES_PER_SAMPLE; // ensure we only use full samples

        // fill buffer
        memcpy(reinterpret_cast<int8_t *>(buffer.buffer) + buffer.size, frames, bytesConsumed);
        buffer.size += bytesConsumed;
        bool bufferFull = (buffer.size + bytesAvailable > BUFFER_SIZE); // can't fit another call with the same amount
        LOGV("Fill buffer %d with %d bytes %s", mProduceBuffer, bytesConsumed, (bufferFull ? "-> enqueue" : ""));

        // enqueue
        if (bufferFull) {
            buffer.consumed = false;

            if (mConsumeBuffer == mProduceBuffer) { // consumer is waiting for buffer
                SLresult result = (*mBqPlayerBufferQueue)->Enqueue(mBqPlayerBufferQueue, buffer.buffer, buffer.size);
                LOGW_IF_ERROR(result, "Enqueuing buffer");
            }

            mProduceBuffer = (mProduceBuffer + 1) % BUFFER_COUNT;
        }
    }

    pthread_mutex_unlock(&mBufferMutex);

    return bytesConsumed / BYTES_PER_SAMPLE;
}

void Player::onEndOfTrack() {
    LOGV(__func__);
    sp_session_player_unload(mSession);

    setTrackProgressMs(mTrackDurationMs);
    //TODO report to user
}

void Player::bqPlayerCallback(SLAndroidSimpleBufferQueueItf bq, void *context) {
    Player *self = static_cast<Player *>(context);
    pthread_mutex_lock(&self->mBufferMutex);

    // mark buffer as consumed
    LOGV("Buffer %d has been consumed", self->mConsumeBuffer);
    Buffer &consumedBuffer = self->mBuffers[self->mConsumeBuffer];
    const int consumedBytes = consumedBuffer.size;
    consumedBuffer.size = 0;
    consumedBuffer.consumed = true;

    //
    self->mConsumeBuffer = (self->mConsumeBuffer + 1) % BUFFER_COUNT;

    // enqueue next buffer
    Buffer &nextBuffer = self->mBuffers[self->mConsumeBuffer];
    if (!nextBuffer.consumed) {
        SLresult result = (*self->mBqPlayerBufferQueue)->Enqueue(self->mBqPlayerBufferQueue, nextBuffer.buffer, nextBuffer.size);
        LOGW_IF_ERROR(result, "Enqueuing buffer");
    } else {
        LOGW("Buffer underflow");
    }

    pthread_mutex_unlock(&self->mBufferMutex);

    // update progress
    self->mTrackProgressBytes += consumedBytes;
    self->setTrackProgressMs(self->mTrackProgressBytes / BYTES_PER_MS);
}

void Player::setTrackProgressMs(int progressMs) {
    const int progressSec = (progressMs + 500) / 1000;
    if (mTrackProgressReportedSec != progressSec) {
        mTrackProgressReportedSec = progressSec;
        const int trackDurationSec = (mTrackDurationMs + 500) / 1000;
        LOGV("Track progress: %ds/%ds", mTrackProgressReportedSec, trackDurationSec);
        if (mCallback) {
            mCallback->onTrackProgress(mTrackProgressReportedSec, trackDurationSec);
        }
    }
}

} // namespace wigwamlabs
