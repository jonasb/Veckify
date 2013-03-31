package com.wigwamlabs.spotify;

public class Playlist extends NativeItemCollection<Track> {
    static {
        nativeInitClass();
    }

    Playlist(int handle) {
        super(handle);
    }

    public Playlist clone() {
        int handle = nativeClone();
        return new Playlist(handle);
    }

    private static native void nativeInitClass();

    native void nativeDestroy();

    private native int nativeClone();

    private native String nativeGetName();

    native int nativeGetCount();

    private native int nativeGetTrack(int position);

    @Override
    Track createNewItem(int index) {
        final int handle = nativeGetTrack(index);
        return new Track(handle);
    }

    public String getName() {
        return nativeGetName();
    }
}
