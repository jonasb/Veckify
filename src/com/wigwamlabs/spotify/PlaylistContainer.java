package com.wigwamlabs.spotify;

import android.util.Log;

import proguard.annotation.Keep;
import proguard.annotation.KeepName;

public class PlaylistContainer {
    @KeepName
    private int mHandle;

    static {
        nativeInitClass();
    }

    public PlaylistContainer(int handle) {
        mHandle = handle;
        nativeInitInstance();
    }

    private static native void nativeInitClass();

    private native void nativeInitInstance();

    private native void nativeDestroy();

    private native int nativeGetCount();

    private native int nativeGetPlaylist(int index);

    public void destroy() {
        if (mHandle != 0) {
            nativeDestroy();
            mHandle = 0;
        }
    }

    @Keep
    void onContainerLoaded() {
        Log.d("XXX", "onContainerLoaded()");
    }

    public int getCount() {
        return nativeGetCount();
    }

    public Playlist getPlaylist(int index) {
        int handle = nativeGetPlaylist(index);
        return Playlist.create(handle);
    }
}
