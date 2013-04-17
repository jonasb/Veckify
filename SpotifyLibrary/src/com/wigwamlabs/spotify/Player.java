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
    public static final int STATE_PAUSED_AUDIOFOCUS = 3;
    public static final int STATE_PAUSED_NOISY = 4;
    public static final int STATE_STOPPED = 5;
    private final Handler mHandler = new Handler();
    private final ArrayList<Callback> mCallbacks = new ArrayList<Callback>();
    private final Context mContext;
    private final AudioManager mAudioManager;
    private boolean mHasAudioFocus;
    private Queue mQueue;
    private int mTrackProgressSec = 0;
    private int mTrackDurationSec = 0;
    private RemoteControlClient mRemoteControlClient;

    public Player(Context context, int handle) {
        super(handle);
        nativeInitInstance();

        mContext = context;
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    private static native void nativeInitClass();

    private native void nativeInitInstance();

    @Override
    void nativeDestroy() {
        // do nothing, native instance is deleted by session
    }

    private native int nativeGetState();

    private native void nativePlay(Track track);

    private native void nativePause(int reasonState);

    private native void nativeResume();

    private native void nativeNext();

    private native void nativeSetNextTrack(Track track);

    private native void nativeSeek(int progressMs);

    public void play(Queue queue) {
        if (mQueue != null) {
            mQueue.destroy();
        }
        mQueue = queue;

        // TODO if the play call fails we should probably abandon the focus (applies for all uses of focus)
        if (requestAudioFocus()) {
            nativePlay(mQueue.getTrack(0));
            nativeSetNextTrack(mQueue.getTrack(1));
        }
    }

    @Keep
    private void onStateChanged(final int state) {
        if (mCallbacks.isEmpty()) {
            return;
        }
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
            }
        });
    }

    @Keep
    private void onTrackProgress(final int secondsPlayed, final int secondsDuration) {
        mTrackProgressSec = secondsPlayed;
        mTrackDurationSec = secondsDuration;
        if (mCallbacks.isEmpty()) {
            return;
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                for (Callback callback : mCallbacks) {
                    callback.onTrackProgress(secondsPlayed, secondsDuration);
                }
            }
        });
    }

    @Keep
    private void onCurrentTrackUpdated(final boolean playNext) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mQueue != null) {
                    mQueue.onCurrentTrackUpdated(playNext);
                    nativeSetNextTrack(mQueue.getTrack(1));

                    final Track currentTrack = mQueue.getTrack(0);
                    for (Callback callback : mCallbacks) {
                        callback.onCurrentTrackUpdated(currentTrack);
                    }

                    if (mRemoteControlClient != null) {
                        mRemoteControlClient.updateMediaData(currentTrack);
                    }
                }
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
            nativeNext();
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

    public interface Callback {
        void onStateChanged(int state);

        void onCurrentTrackUpdated(Track track);

        void onTrackProgress(int secondsPlayed, int secondsDuration);
    }
}
