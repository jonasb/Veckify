package com.wigwamlabs.spotify.app;

public class SpotifySession {
    static {
        nativeInitClass();
    }

    private int mHandle;

    public SpotifySession(SpotifyContext spotifyContext) {
        mHandle = nativeCreate(spotifyContext);
    }

    private static native void nativeInitClass();

    public void destroy() {
        if (mHandle != 0) {
            nativeDestroy();
            mHandle = 0;
        }
    }

    private native int nativeCreate(SpotifyContext spotifyContext);

    private native void nativeDestroy();
}
