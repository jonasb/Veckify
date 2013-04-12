package com.wigwamlabs.spotify;

public class PlaylistQueue implements Queue {
    private Playlist mPlaylist;
    private int mIndex = 0; //TODO update to deal with playlist changes

    public PlaylistQueue(Playlist playlist) {
        this(playlist, 0);
    }

    public PlaylistQueue(Playlist playlist, int initialIndex) {
        mPlaylist = playlist.clone();
        mIndex = initialIndex;
    }

    @Override
    public void destroy() {
        if (mPlaylist != null) {
            mPlaylist.destroy();
            mPlaylist = null;
        }
    }

    @Override
    public Track getTrack(int index) {
        return mPlaylist.getItem((mIndex + index) % mPlaylist.getCount());
    }

    @Override
    public void onCurrentTrackUpdated(boolean playNext) {
        if (playNext) {
            mIndex++;
            if (mIndex >= mPlaylist.getCount()) {
                mIndex = 0;
            }
        }
    }
}
