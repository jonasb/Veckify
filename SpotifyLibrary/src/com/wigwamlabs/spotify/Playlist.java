package com.wigwamlabs.spotify;

import android.os.Handler;

import proguard.annotation.Keep;

public class Playlist extends NativeItemCollection<Track> {
    static {
        nativeInitClass();
    }

    public static final int OFFLINE_STATUS_NO = 0;
    public static final int OFFLINE_STATUS_YES = 1;
    public static final int OFFLINE_STATUS_DOWNLOADING = 2;
    public static final int OFFLINE_STATUS_WAITING = 3;

    private final Handler mHandler = new Handler();
    private Callback mCallback;

    Playlist(int handle) {
        super(handle);

        nativeInitInstance();
    }

    public static Playlist create(Session session, String link) {
        final int handle = nativeCreate(session, link);
        if (handle != 0) {
            return new Playlist(handle);
        }
        return null;
    }

    private static native void nativeInitClass();

    static native int nativeCreate(Session session, String link);

    @Override
    public Playlist clone() {
        final int handle = nativeClone();
        return new Playlist(handle);
    }

    private native void nativeInitInstance();

    @Override
    native void nativeDestroy();

    private native int nativeClone();

    private native boolean nativeIsLoaded();

    private native String nativeGetLink();

    private native String nativeGetName();

    @Override
    native int nativeGetCount();

    private native int nativeGetTrack(int position);

    private native void nativeSetOfflineMode(Session session, boolean offline);

    private native int nativeGetOfflineStatus(Session session);

    private native int nativeGetOfflineDownloadComplete(Session session);

    @Override
    Track createNewItem(int index) {
        final int handle = nativeGetTrack(index);
        return new Track(handle);
    }

    public boolean isLoaded() {
        return nativeIsLoaded();
    }

    public String getLink() {
        return nativeGetLink();
    }

    public String getName() {
        return nativeGetName();
    }

    public void setOfflineMode(Session session, boolean offline) {
        nativeSetOfflineMode(session, offline);
    }

    public int getOfflineStatus(Session session) {
        return nativeGetOfflineStatus(session);
    }

    public int getOfflineDownloadComplete(Session session) {
        return nativeGetOfflineDownloadComplete(session);
    }

    public void setCallback(Callback callback, boolean callbackNow) {
        mCallback = callback;
        if (callback != null && callbackNow) {
            //TODO how to treat the other callbacks
            if (isLoaded()) {
                callback.onPlaylistStateChanged();
            }
        }
    }

    @Keep
    private void onTracksMoved(final int[] oldPositions, final int newPosition) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Playlist.this.onItemsMoved(oldPositions, newPosition);
            }
        });
    }

    @Keep
    private void onPlaylistRenamed() {
        if (mCallback == null) {
            return;
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mCallback != null) {
                    mCallback.onPlaylistRenamed();
                }
            }
        });
    }

    @Keep
    private void onPlaylistStateChanged() {
        if (mCallback == null) {
            return;
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mCallback != null) {
                    mCallback.onPlaylistStateChanged();
                }
            }
        });
    }

    @Keep
    private void onPlaylistUpdateInProgress(final boolean done) {
        if (mCallback == null) {
            return;
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mCallback != null) {
                    mCallback.onPlaylistUpdateInProgress(done);
                }
            }
        });
    }

    public interface Callback {
        void onPlaylistUpdateInProgress(boolean done);

        void onPlaylistRenamed();

        void onPlaylistStateChanged();
    }
}
