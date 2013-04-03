package com.wigwamlabs.spotify;

public class Placeholder extends NativeItem {
    static {
        nativeInitClass();
    }

    public Placeholder(int handle) {
        super(handle);
    }

    private static native void nativeInitClass();

    @Override
    native void nativeDestroy();
}
