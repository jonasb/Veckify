package com.wigwamlabs.spotify;

import proguard.annotation.KeepName;

public class PlaylistContainer {
    @KeepName
    private final int mHandle;

    static {
        nativeInitClass();
    }

    public PlaylistContainer(int handle) {
        mHandle = handle;
    }

    private static native void nativeInitClass();

    private native void nativeDestroy();

    private native int nativeGetCount();

    public void destroy() {
        if (mHandle != 0) {
            nativeDestroy();
        }
    }

    public int getCount() {
        return nativeGetCount();
    }
}
