package com.wigwamlabs.spotify;

import proguard.annotation.KeepName;

public abstract class NativeItem {
    @KeepName
    private int mHandle;

    NativeItem(int handle) {
        mHandle = handle;
    }

    void setHandle(int handle) {
        destroy();

        mHandle = handle;
    }

    public void destroy() {
        if (mHandle != 0) {
            nativeDestroy();
            mHandle = 0;
        }
    }

    abstract void nativeDestroy();

    public int getId() {
        return mHandle;
    }
}
