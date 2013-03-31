package com.wigwamlabs.spotify;

public class Track extends NativeItem {
    static {
        nativeInitClass();
    }

    Track(int handle) {
        super(handle);
    }

    public Track(String uri) {
        super(0);
        setHandle(nativeCreate(uri));
    }

    private static native void nativeInitClass();

    private native int nativeCreate(String uri);

    native void nativeDestroy();

    private native String nativeGetName();

    public String getName() {
        return nativeGetName();
    }
}
