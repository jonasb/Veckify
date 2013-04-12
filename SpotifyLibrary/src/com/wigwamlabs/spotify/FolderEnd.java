package com.wigwamlabs.spotify;

public class FolderEnd extends NativeItem {
    static {
        nativeInitClass();
    }

    public FolderEnd(int handle) {
        super(handle);
    }

    private static native void nativeInitClass();

    @Override
    native void nativeDestroy();
}
