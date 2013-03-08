package com.wigwamlabs.spotify;

import proguard.annotation.KeepName;

public class PlaylistContainer {
    @KeepName
    private int mHandle;

    static {
        nativeInitClass();
    }

    public PlaylistContainer(int handle) {
        mHandle = handle;
    }

    private static native void nativeInitClass();

    private native void nativeDestroy();

    private native int nativeGetCount();

    private native int nativeGetPlaylist(int index);

    public void destroy() {
        if (mHandle != 0) {
            nativeDestroy();
            mHandle = 0;
        }
    }

    public int getCount() {
        return nativeGetCount();
    }

    public Playlist getPlaylist(int index) {
        int handle = nativeGetPlaylist(index);
        return Playlist.create(handle);
    }
}
