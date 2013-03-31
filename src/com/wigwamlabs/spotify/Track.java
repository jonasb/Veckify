package com.wigwamlabs.spotify;

public class Track extends NativeItem {
    static {
        nativeInitClass();
    }

    private Artist[] mArtists;

    Track(int handle) {
        super(handle);
    }

    public Track(String uri) {
        super(0);
        setHandle(nativeCreate(uri));
    }

    public Track clone() {
        int handle = nativeClone();
        return new Track(handle);
    }

    private static native void nativeInitClass();

    private native int nativeCreate(String uri);

    private native int nativeClone();

    native void nativeDestroy();

    private native String nativeGetName();

    private native int nativeGetArtistCount();

    private native int nativeGetArtist(int index);

    @Override
    public void destroy() {
        super.destroy();

        if (mArtists != null) {
            for (Artist artist : mArtists) {
                artist.destroy();
            }
        }
    }

    public String getName() {
        return nativeGetName();
    }

    public Artist[] getArtists() {
        if (mArtists != null) {
            return mArtists;
        }
        final int count = nativeGetArtistCount();
        mArtists = new Artist[count];
        for (int i = 0; i < mArtists.length; i++) {
            int handle = nativeGetArtist(i);
            mArtists[i] = new Artist(handle);
        }
        return mArtists;
    }

}
