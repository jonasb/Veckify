package com.wigwamlabs.spotify;

import android.os.Handler;

import proguard.annotation.Keep;


public class Player extends NativeItem {
    static {
        nativeInitClass();
    }

    private final Handler mHandler = new Handler();
    private Callback mCallback;
    private Queue mQueue;
    private int mTrackProgressSec = 0;
    private int mTrackDurationSec = 0;

    public Player(int handle) {
        super(handle);
        nativeInitInstance();
    }

    private static native void nativeInitClass();

    private native void nativeInitInstance();

    @Override
    void nativeDestroy() {
        // do nothing, native instance is deleted by session
    }

    private native void nativePlay(Track track);

    private native void nativeSetNextTrack(Track track);

    private native void nativeSeek(int progressMs);

    public void play(Queue queue) {
        if (mQueue != null) {
            mQueue.destroy();
        }
        mQueue = queue;

        nativePlay(mQueue.getTrack(0));
        nativeSetNextTrack(mQueue.getTrack(1));
    }

    @Keep
    private void onTrackProgress(final int secondsPlayed, final int secondsDuration) {
        mTrackProgressSec = secondsPlayed;
        mTrackDurationSec = secondsDuration;
        if (mCallback == null) {
            return;
        }
        mHandler.post(new Runnable() {
            public void run() {
                if (mCallback != null) {
                    mCallback.onTrackProgress(secondsPlayed, secondsDuration);
                }
            }
        });
    }

    @Keep
    private void onCurrentTrackUpdated(final boolean playNext) {
        mHandler.post(new Runnable() {
            public void run() {
                if (mQueue != null) {
                    mQueue.onCurrentTrackUpdated(playNext);
                    nativeSetNextTrack(mQueue.getTrack(1));

                    if (mCallback != null) {
                        mCallback.onCurrentTrackUpdated(mQueue.getTrack(0));
                    }
                }
            }
        });
    }

    public void setCallback(Callback callback, boolean callbackNow) {
        mCallback = callback;
        if (mCallback != null && callbackNow) {
            mCallback.onCurrentTrackUpdated(mQueue != null ? mQueue.getTrack(0) : null);
            mCallback.onTrackProgress(mTrackProgressSec, mTrackDurationSec);
        }
    }

    public void seek(int progressMs) {
        nativeSeek(progressMs);
    }

    public interface Callback {
        void onCurrentTrackUpdated(Track track);

        void onTrackProgress(int secondsPlayed, int secondsDuration);
    }
}
