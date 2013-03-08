package com.wigwamlabs.spotify;

import proguard.annotation.KeepName;

public class Playlist {
    @KeepName
    private int mHandle;

    static {
        nativeInitClass();
    }

    private Playlist(int handle) {
        mHandle = handle;
    }

    private static native void nativeInitClass();

    public static Playlist create(int handle) {
        return new Playlist(handle);
    }

    private native void nativeDestroy();

    private native String nativeGetName();

    public void destroy() {
        if (mHandle != 0) {
            nativeDestroy();
            mHandle = 0;
        }
    }

    public String getName() {
        return nativeGetName();
    }
}
