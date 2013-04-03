package com.wigwamlabs.spotify;

public class Artist extends NativeItem {
    static {
        nativeInitClass();
    }

    public Artist(int handle) {
        super(handle);
    }

    private static native void nativeInitClass();

    private native String nativeGetName();

    @Override
    native void nativeDestroy();

    public String getName() {
        return nativeGetName();
    }
}
