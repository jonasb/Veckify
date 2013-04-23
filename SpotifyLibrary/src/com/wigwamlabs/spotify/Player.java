package com.wigwamlabs.spotify;

import android.content.ComponentName;
import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.widget.Toast;

import proguard.annotation.Keep;

import java.util.ArrayList;

public class Player extends NativeItem implements AudioManager.OnAudioFocusChangeListener {
    static {
        nativeInitClass();
    }

    public static final int STATE_STARTED = 0;
    public static final int STATE_PLAYING = 1;
    public static final int STATE_PAUSED_USER = 2;
    public static final int STATE_PAUSED_NOISY = 3;
    public static final int STATE_PAUSED_AUDIOFOCUS = 4;
    public static final int STATE_STOPPED = 5;
    private static final long DURATION_BEFORE_AUDIO_UNRESPONSIVE_MS = 5 * 1000;
    private final Handler mHandler = new Handler();
    private final ArrayList<Callback> mCallbacks = new ArrayList<Callback>();
    private final Context mContext;
    private final AudioManager mAudioManager;
    private final Runnable mResondToUnresponsiveAudio;
    private boolean mHasAudioFocus;
    private Queue mQueue;
    private int mTrackProgressSec = 0;
    private int mTrackDurationSec = 0;
    private RemoteControlClient mRemoteControlClient;
    private boolean mPrefetchRequested;

    public Player(Context context, int handle) {
        super(handle);
        nativeInitInstance();

        mContext = context;
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mResondToUnresponsiveAudio = new Runnable() {
            @Override
            public void run() {
                onUnresponsiveAudio();
            }
        };
    }

    private static native void nativeInitClass();

    private native void nativeInitInstance();

    @Override
    void nativeDestroy() {
        // do nothing, native instance is deleted by session
    }

    private native int nativeGetState();

    private native int nativePlay(Track track);

    private native void nativePrefetchTrack(Track track);

    private native void nativePause(int reasonState);

    private native void nativeResume();

    private native void nativeSeek(int progressMs);

    @Keep
    private void onStateChanged(final int state) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                for (Callback callback : mCallbacks) {
                    callback.onStateChanged(state);
                }
                if (state != STATE_PLAYING && state != STATE_PAUSED_AUDIOFOCUS && state != STATE_PAUSED_USER && state != STATE_PAUSED_NOISY) {
                    abandonAudioFocus();
                }
                if (mRemoteControlClient != null) {
                    mRemoteControlClient.onStateChanged(state);
                }
                if (state == STATE_PLAYING) {
                    Debug.logAudioResponsivenessVerbose("Start checking since we start playing");
                    mHandler.removeCallbacks(mResondToUnresponsiveAudio);
                    mHandler.postDelayed(mResondToUnresponsiveAudio, DURATION_BEFORE_AUDIO_UNRESPONSIVE_MS);
                } else {
                    Debug.logAudioResponsivenessVerbose("Stop checking, since we're not playing");
                    mHandler.removeCallbacks(mResondToUnresponsiveAudio);
                }
            }
        });
    }

    @Keep
    private void onTrackProgress(final int secondsPlayed, final int secondsDuration) {
        mTrackProgressSec = secondsPlayed;
        mTrackDurationSec = secondsDuration;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                for (Callback callback : mCallbacks) {
                    callback.onTrackProgress(secondsPlayed, secondsDuration);
                }
                if (secondsPlayed >= secondsDuration) {
                    mQueue.next();
                    playTrack();
                } else if (secondsDuration - secondsPlayed <= 20 && !mPrefetchRequested) {
                    final Track next = mQueue.getTrack(1);
                    if (next != null) {
                        nativePrefetchTrack(next);
                    }
                    mPrefetchRequested = true;
                }
                Debug.logAudioResponsivenessVerbose("Got audio, postpone check");
                mHandler.removeCallbacks(mResondToUnresponsiveAudio);
                mHandler.postDelayed(mResondToUnresponsiveAudio, DURATION_BEFORE_AUDIO_UNRESPONSIVE_MS);
            }
        });
    }

    @Keep
    private void onPlayTokenLost() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, R.string.toast_play_token_lost, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void addCallback(Callback callback, boolean callbackNow) {
        if (mCallbacks.contains(callback)) {
            return;
        }
        mCallbacks.add(callback);
        if (callbackNow) {
            callback.onStateChanged(getState());
            callback.onCurrentTrackUpdated(mQueue != null ? mQueue.getTrack(0) : null);
            callback.onTrackProgress(mTrackProgressSec, mTrackDurationSec);
        }
    }

    private int getState() {
        return nativeGetState();
    }

    public void removeCallback(Callback callback) {
        mCallbacks.remove(callback);
    }

    public void play(Queue queue) {
        if (mQueue != null) {
            mQueue.destroy();
        }
        mQueue = queue;

        // TODO if the play call fails we should probably abandon the focus (applies for all uses of focus)
        if (requestAudioFocus()) {
            playTrack();
        }
    }

    private void playTrack() {
        Track track;
        while (true) {
            track = mQueue.getTrack(0);
            final int error = nativePlay(track);
            if (error == SpotifyError.TRACK_NOT_PLAYABLE) {
                Debug.logQueue("Queue: track '" + track.getName() + "' not playable, skip");
                //TODO what if all tracks are non playable
                //TODO keep track of which tracks are playable?
                mQueue.next();
            } else {
                break;
            }
        }
        mPrefetchRequested = false;

        for (Callback callback : mCallbacks) {
            callback.onCurrentTrackUpdated(track);
        }

        if (mRemoteControlClient != null) {
            mRemoteControlClient.updateMediaData(track);
        }
    }

    public void seek(int progressMs) {
        nativeSeek(progressMs);
    }

    public void pause() {
        nativePause(STATE_PAUSED_USER);
    }

    void pauseNoisy() {
        nativePause(STATE_PAUSED_NOISY);
    }

    public void resume() {
        if (requestAudioFocus()) {
            nativeResume();
        }
    }

    public void togglePause() {
        switch (getState()) {
        case STATE_PLAYING:
            pause();
            break;
        case STATE_PAUSED_USER:
        case STATE_PAUSED_AUDIOFOCUS:
        case STATE_PAUSED_NOISY:
            resume();
            break;
        case STATE_STARTED:
        case STATE_STOPPED:
            break;
        }
    }

    public void next() {
        if (requestAudioFocus()) {
            mQueue.next();
            playTrack();
        }
    }

    private boolean requestAudioFocus() {
        if (!mHasAudioFocus) {
            final int result = mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            mHasAudioFocus = (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
            Debug.logAudioFocus("Audio focus request: " + (mHasAudioFocus ? "succeeded" : "failed"));
        }

        if (mHasAudioFocus) {
            final ComponentName receiver = new ComponentName(mContext, BroadcastReceiver.class);
            // if the receiver is already registered, it will be moved to the top of the stack, so it's ok to call it multiple times
            mAudioManager.registerMediaButtonEventReceiver(receiver);

            if (mRemoteControlClient == null) {
                mRemoteControlClient = RemoteControlClient.create(mContext, receiver);
            }
            mAudioManager.registerRemoteControlClient(mRemoteControlClient);
        }

        return mHasAudioFocus;
    }

    private void abandonAudioFocus() {
        final ComponentName receiver = new ComponentName(mContext, BroadcastReceiver.class);
        if (mRemoteControlClient != null) {
            mAudioManager.unregisterRemoteControlClient(mRemoteControlClient);
            mRemoteControlClient = null;
        }
        mAudioManager.unregisterMediaButtonEventReceiver(receiver);

        if (mHasAudioFocus) {
            Debug.logAudioFocus("Abandoning audio focus");
            mHasAudioFocus = false;
            mAudioManager.abandonAudioFocus(this);
        }
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
        case AudioManager.AUDIOFOCUS_GAIN:
            Debug.logAudioFocus("Gained audio focus");
            mHasAudioFocus = true;
            if (getState() == STATE_PAUSED_AUDIOFOCUS) {
                resume();
            }
            break;
        case AudioManager.AUDIOFOCUS_LOSS:
            Debug.logAudioFocus("Lost audio focus");
            nativePause(STATE_PAUSED_AUDIOFOCUS);
            break;
        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
            Debug.logAudioFocus("Lost audio focus (transient)");
            nativePause(STATE_PAUSED_AUDIOFOCUS);
            break;
        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
            //TODO deal with ducking?
            Debug.logAudioFocus("Lost audio focus (transient, can duck)");
            nativePause(STATE_PAUSED_AUDIOFOCUS);
            break;
        }
    }

    private void onUnresponsiveAudio() {
        if (getState() != STATE_PLAYING) {
            Debug.logAudioResponsiveness("Is not playing when reacting to unresponsiveness, ignore.");
            return;
        }
        Debug.logAudioResponsiveness("Audio is unresponsive, skip track");
        Debug.notifyAudioUnresponsive(mContext, "Audio unresponsive", "Skipping current track.");

        mQueue.next();
        playTrack();

        mHandler.removeCallbacks(mResondToUnresponsiveAudio);
        mHandler.postDelayed(mResondToUnresponsiveAudio, DURATION_BEFORE_AUDIO_UNRESPONSIVE_MS);
    }

    public interface Callback {
        void onStateChanged(int state);

        void onCurrentTrackUpdated(Track track);

        void onTrackProgress(int secondsPlayed, int secondsDuration);
    }
}
