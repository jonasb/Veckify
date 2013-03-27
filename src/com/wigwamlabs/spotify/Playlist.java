package com.wigwamlabs.spotify;

import proguard.annotation.KeepName;

public class Playlist implements PlaylistContainerItem {
    @KeepName
    private int mHandle;

    static {
        nativeInitClass();
    }

    Playlist(int handle) {
        mHandle = handle;
    }

    public Playlist clone() {
        int handle = nativeClone();
        return new Playlist(handle);
    }

    private static native void nativeInitClass();

    private native void nativeDestroy();

    private native int nativeClone();

    private native String nativeGetName();

    private native int nativeGetCount();

    private native int nativeGetTrack(int position);

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

    public int getCount() {
        return nativeGetCount();
    }

    public Track getTrack(int position) {
        final int handle = nativeGetTrack(position);
        return new Track(handle);
    }
}
