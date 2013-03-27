package com.wigwamlabs.spotify;

import proguard.annotation.KeepName;

public class FolderStart implements PlaylistContainerItem {
    @KeepName
    private int mHandle;

    static {
        nativeInitClass();
    }

    public FolderStart(int handle) {
        mHandle = handle;
    }

    private static native void nativeInitClass();

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

    public int getId() {
        return mHandle;
    }
}
