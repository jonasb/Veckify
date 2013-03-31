package com.wigwamlabs.spotify;

import android.os.Handler;

import proguard.annotation.Keep;

public class Playlist extends NativeItemCollection<Track> {
    static {
        nativeInitClass();
    }

    private final Handler mHandler = new Handler();
    private Callback mCallback;

    Playlist(int handle) {
        super(handle);

        nativeInitInstance();
    }

    public Playlist clone() {
        int handle = nativeClone();
        return new Playlist(handle);
    }

    private static native void nativeInitClass();

    private native void nativeInitInstance();

    native void nativeDestroy();

    private native int nativeClone();

    private native String nativeGetName();

    native int nativeGetCount();

    private native int nativeGetTrack(int position);

    @Override
    Track createNewItem(int index) {
        final int handle = nativeGetTrack(index);
        return new Track(handle);
    }

    public String getName() {
        return nativeGetName();
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    @Keep
    private void onTracksMoved(final int[] oldPositions, final int newPosition) {
        mHandler.post(new Runnable() {
            public void run() {
                Playlist.this.onItemsMoved(oldPositions, newPosition);
            }
        });
    }

    @Keep
    private void onPlaylistUpdateInProgress(final boolean done) {
        mHandler.post(new Runnable() {
            public void run() {
                if (mCallback != null) {
                    mCallback.onPlaylistUpdateInProgress(done);
                }
            }
        });
    }

    public interface Callback {
        void onPlaylistUpdateInProgress(boolean done);
    }
}
