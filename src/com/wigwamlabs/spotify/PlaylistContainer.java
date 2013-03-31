package com.wigwamlabs.spotify;

import android.os.Handler;
import android.util.Log;

import proguard.annotation.Keep;
import proguard.annotation.KeepName;

import java.util.ArrayList;

public class PlaylistContainer {
    private static final int TYPE_PLAYLIST = 0;
    private static final int TYPE_START_FOLDER = 1;
    private static final int TYPE_END_FOLDER = 2;
    private static final int TYPE_PLACEHOLDER = 3;
    private final Handler mHandler = new Handler();
    @KeepName
    private int mHandle;
    private ArrayList<PlaylistContainerItem> mItems;
    private Callback mCallback;

    static {
        nativeInitClass();
    }

    public PlaylistContainer(int handle) {
        mHandle = handle;
        nativeInitInstance();
    }

    private static native void nativeInitClass();

    private native void nativeInitInstance();

    private native void nativeDestroy();

    private native int nativeGetCount();

    private native int nativeGetPlaylistType(int index);

    private native int nativeGetPlaylist(int index);

    private native int nativeGetFolderStart(int index);

    private native int nativeGetFolderEnd(int index);

    private native int nativeGetPlaceholder(int index);

    public void destroy() {
        if (mHandle != 0) {
            nativeDestroy();
            mHandle = 0;
        }
        if (mItems != null) {
            for (PlaylistContainerItem item : mItems) {
                if (item != null) {
                    item.destroy();
                }
            }
            mItems = null;
        }
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    @Keep
    void onPlaylistMoved(final int oldPosition, final int newPosition) {
        Log.d("XXX", "onPlaylistMoved(" + oldPosition + ", " + newPosition + ")");

        mHandler.post(new Runnable() {
            public void run() {
                if (mItems != null) {
                    //TODO Probably not correct, seems as if new position is before change
                    PlaylistContainerItem old = null;
                    if (oldPosition >= 0) {
                        old = mItems.remove(oldPosition);
                    }
                    if (newPosition >= 0) {
                        mItems.add(newPosition, old);
                    }
                }
            }
        });
    }

    @Keep
    void onContainerLoaded() {
        Log.d("XXX", "onContainerLoaded() callback = " + mCallback);

        mHandler.post(new Runnable() {
            public void run() {
                initList();

                if (mCallback != null) {
                    mCallback.onContainerLoaded();
                }
            }
        });
    }

    private void initList() {
        if (mItems == null) {
            final int count = nativeGetCount();
            mItems = new ArrayList<PlaylistContainerItem>(count);
            for (int i = 0; i < count; i++) {
                mItems.add(null);
            }
        }
    }

    public int getCount() {
        initList();

        return mItems.size();
    }

    public PlaylistContainerItem getPlaylist(int index) {
        initList();

        // check cache
        PlaylistContainerItem item = mItems.get(index);
        if (item != null) {
            return item;
        }

        switch (nativeGetPlaylistType(index)) {
        case TYPE_PLAYLIST: {
            final int handle = nativeGetPlaylist(index);
            item = new Playlist(handle);
            break;
        }
        case TYPE_START_FOLDER: {
            final int handle = nativeGetFolderStart(index);
            item = new FolderStart(handle);
            break;
        }
        case TYPE_END_FOLDER: {
            final int handle = nativeGetFolderEnd(index);
            item = new FolderEnd(handle);
            break;
        }
        default:
        case TYPE_PLACEHOLDER:
            final int handle = nativeGetPlaceholder(index);
            item = new Placeholder(handle);
            break;
        }
        mItems.set(index, item);
        return item;
    }

    public interface Callback {
        void onContainerLoaded();
    }
}
