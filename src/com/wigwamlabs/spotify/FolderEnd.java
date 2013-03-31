package com.wigwamlabs.spotify;

import proguard.annotation.KeepName;

public class FolderEnd implements PlaylistContainerItem {

    @KeepName
    private int mHandle;

    static {
        nativeInitClass();
    }

    public FolderEnd(int handle) {
        mHandle = handle;
    }

    private static native void nativeInitClass();

    private native void nativeDestroy();

    public void destroy() {
        if (mHandle != 0) {
            nativeDestroy();
            mHandle = 0;
        }
    }

    public int getId() {
        return mHandle;
    }
}