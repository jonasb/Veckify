package com.wigwamlabs.spotify;

import proguard.annotation.KeepName;

public class Track {
    @KeepName
    private int mHandle;

    static {
        nativeInitClass();
    }

    Track(int handle) {
        mHandle = handle;
    }

    public Track(String uri) {
        mHandle = nativeCreate(uri);
    }

    private static native void nativeInitClass();

    private native int nativeCreate(String uri);

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
