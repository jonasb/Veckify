package com.wigwamlabs.spotify;

import android.os.Handler;

import proguard.annotation.Keep;

public class PlaylistContainer extends NativeItemCollection<NativeItem> {
    private static final int TYPE_PLAYLIST = 0;
    private static final int TYPE_START_FOLDER = 1;
    private static final int TYPE_END_FOLDER = 2;
    private static final int TYPE_PLACEHOLDER = 3;
    private final Handler mHandler = new Handler();
    private Callback mCallback;

    static {
        nativeInitClass();
    }

    public PlaylistContainer(int handle) {
        super(handle);
        nativeInitInstance();
    }

    private static native void nativeInitClass();

    private native void nativeInitInstance();

    native void nativeDestroy();

    protected native int nativeGetCount();

    private native int nativeGetPlaylistType(int index);

    private native int nativeGetPlaylist(int index);

    private native int nativeGetFolderStart(int index);

    private native int nativeGetFolderEnd(int index);

    private native int nativeGetPlaceholder(int index);

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    @Keep
    void onPlaylistMoved(final int oldPosition, final int newPosition) {
        mHandler.post(new Runnable() {
            public void run() {
                PlaylistContainer.this.onItemsMoved(oldPosition, newPosition);
            }
        });
    }

    @Keep
    void onContainerLoaded() {
        mHandler.post(new Runnable() {
            public void run() {
                initList();

                if (mCallback != null) {
                    mCallback.onContainerLoaded();
                }
            }
        });
    }


    NativeItem createNewItem(int index) {
        switch (nativeGetPlaylistType(index)) {
        case TYPE_PLAYLIST: {
            final int handle = nativeGetPlaylist(index);
            return new Playlist(handle);
        }
        case TYPE_START_FOLDER: {
            final int handle = nativeGetFolderStart(index);
            return new FolderStart(handle);
        }
        case TYPE_END_FOLDER: {
            final int handle = nativeGetFolderEnd(index);
            return new FolderEnd(handle);
        }
        default:
        case TYPE_PLACEHOLDER:
            final int handle = nativeGetPlaceholder(index);
            return new Placeholder(handle);
        }
    }

    public interface Callback {
        void onContainerLoaded();
    }
}
