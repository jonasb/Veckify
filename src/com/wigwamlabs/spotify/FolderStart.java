package com.wigwamlabs.spotify;

public class FolderStart extends NativeItem {
    static {
        nativeInitClass();
    }

    public FolderStart(int handle) {
        super(handle);
    }

    private static native void nativeInitClass();

    @Override
    native void nativeDestroy();

    private native String nativeGetName();

    public String getName() {
        return nativeGetName();
    }
}
