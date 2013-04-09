package com.wigwamlabs.spotify;

import android.os.Handler;

import proguard.annotation.Keep;

import java.util.ArrayList;


public class Player extends NativeItem {
    static {
        nativeInitClass();
    }

    private final Handler mHandler = new Handler();
    private final ArrayList<Callback> mCallbacks = new ArrayList<Callback>();
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
        if (mCallbacks.isEmpty()) {
            return;
        }
        mHandler.post(new Runnable() {
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
            public void run() {
                if (mQueue != null) {
                    mQueue.onCurrentTrackUpdated(playNext);
                    nativeSetNextTrack(mQueue.getTrack(1));

                    final Track currentTrack = mQueue.getTrack(0);
                    for (Callback callback : mCallbacks) {
                        callback.onCurrentTrackUpdated(currentTrack);
                    }
                }
            }
        });
    }

    public void addCallback(Callback callback, boolean callbackNow) {
        if (mCallbacks.contains(callback)) {
            return;
        }
        mCallbacks.add(callback);
        if (callbackNow) {
            callback.onCurrentTrackUpdated(mQueue != null ? mQueue.getTrack(0) : null);
            callback.onTrackProgress(mTrackProgressSec, mTrackDurationSec);
        }
    }

    public void removeCallback(Callback callback) {
        mCallbacks.remove(callback);
    }

    public void seek(int progressMs) {
        nativeSeek(progressMs);
    }

    public interface Callback {
        void onCurrentTrackUpdated(Track track);

        void onTrackProgress(int secondsPlayed, int secondsDuration);
    }
}
