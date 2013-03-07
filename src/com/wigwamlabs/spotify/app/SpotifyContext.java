package com.wigwamlabs.spotify.app;

//TODO perhaps this class is not needed since the session provides the same role
public class SpotifyContext {
    static {
        System.loadLibrary("spotify");
        System.loadLibrary("spotify-jni");

        nativeInitClass();
    }

    private int mHandle;

    public SpotifyContext() {
        mHandle = nativeCreate();
    }

    private static native void nativeInitClass();

    public void destroy() {
        if (mHandle != 0) {
            nativeDestroy();
            mHandle = 0;
        }
    }

    private native int nativeCreate();

    private native void nativeDestroy();
}
