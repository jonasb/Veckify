package com.wigwamlabs.spotify;

import android.os.Handler;

import proguard.annotation.Keep;


public class Player extends NativeItem {
    static {
        nativeInitClass();
    }

    private Handler mHandler = new Handler();
    private Callback mCallback;

    public Player(int handle) {
        super(handle);
        nativeInitInstance();
    }

    private static native void nativeInitClass();

    private native void nativeInitInstance();

    void nativeDestroy() {
        // do nothing, native instance is deleted by session
    }

    private native void nativePlay(Track track);

    private native void nativeSeek(int progressMs);

    public void play(Track track) {
        nativePlay(track);
    }

    @Keep
    private void onTrackProgress(final int secondsPlayed, final int secondsDuration) {
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

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public void seek(int progressMs) {
        nativeSeek(progressMs);
    }

    public interface Callback {
        void onTrackProgress(int secondsPlayed, int secondsDuration);
    }
}
