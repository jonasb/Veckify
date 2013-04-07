package com.wigwamlabs.spotify;

public class PlaylistQueue implements Queue {
    private Playlist mPlaylist;
    private int mIndex = 0; //TODO update to deal with playlist changes

    public PlaylistQueue(Playlist playlist) {
        mPlaylist = playlist.clone();
    }

    public void destroy() {
        if (mPlaylist != null) {
            mPlaylist.destroy();
            mPlaylist = null;
        }
    }

    public Track getTrack(int index) {
        return mPlaylist.getItem((mIndex + index) % mPlaylist.getCount());
    }

    public void onCurrentTrackUpdated(boolean playNext) {
        if (playNext) {
            mIndex++;
            if (mIndex >= mPlaylist.getCount()) {
                mIndex = 0;
            }
        }
    }
}
