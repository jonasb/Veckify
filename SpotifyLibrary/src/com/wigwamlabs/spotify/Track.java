package com.wigwamlabs.spotify;

public class Track extends NativeItem {
    static {
        nativeInitClass();
    }

    public static final int AVAILABILITY_UNAVAILABLE = 0;
    public static final int AVAILABILITY_AVAILABLE = 1;
    public static final int AVAILABILITY_NOT_STREAMABLE = 2;
    public static final int AVAILABILITY_BANNED_BY_ARTIST = 3;
    private Artist[] mArtists;

    Track(int handle) {
        super(handle);
    }

    public Track(String uri) {
        super(0);
        setHandle(nativeCreate(uri));
    }

    private static native void nativeInitClass();

    @Override
    public Track clone() {
        final int handle = nativeClone();
        return new Track(handle);
    }

    private native int nativeCreate(String uri);

    private native int nativeClone();

    @Override
    native void nativeDestroy();

    private native String nativeGetName();

    private native int nativeGetArtistCount();

    private native int nativeGetArtist(int index);

    private native String nativeGetImageLink(int size);

    private native int nativeGetAvailability(Session session);

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
        if (count == 0) {
            return new Artist[0];
        }

        mArtists = new Artist[count];
        for (int i = 0; i < mArtists.length; i++) {
            final int handle = nativeGetArtist(i);
            mArtists[i] = new Artist(handle);
        }
        return mArtists;
    }

    public String getArtistsString() {
        final Artist[] artists = getArtists();
        final StringBuilder sb = new StringBuilder();
        for (Artist artist : artists) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(artist.getName());
        }
        return sb.toString();
    }

    public String getImageLink(int size) {
        return nativeGetImageLink(size);
    }

    public int getAvailability(Session session) {
        return nativeGetAvailability(session);
    }
}
