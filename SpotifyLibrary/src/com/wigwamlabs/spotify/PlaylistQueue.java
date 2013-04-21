package com.wigwamlabs.spotify;

public class PlaylistQueue implements Queue, Playlist.Callback {
    private final Track[] mTracks = new Track[2];
    private final int[] mTrackIds = new int[mTracks.length];
    private Playlist mPlaylist;
    private int mPlaylistIndex = 0;

    public PlaylistQueue(Playlist playlist, int initialIndex) {
        mPlaylist = playlist.clone();
        mPlaylist.setCallback(this, false);
        mPlaylistIndex = initialIndex;
    }

    @Override
    public void destroy() {
        for (int i = 0; i < mTracks.length; i++) {
            final Track track = mTracks[i];
            if (track != null) {
                track.destroy();
                mTracks[i] = null;
            }
        }
        if (mPlaylist != null) {
            mPlaylist.setCallback(null, false);
            mPlaylist.destroy();
            mPlaylist = null;
        }
    }

    @Override
    public Track getTrack(int index) {
        if (mTracks[index] == null) {
            final Track track = getTrackAtPlaylistIndex(index);
            if (track != null) {
                mTracks[index] = track.clone();
                mTrackIds[index] = track.getId();
            }
        }
        return mTracks[index];
    }

    private Track getTrackAtPlaylistIndex(int index) {
        return mPlaylist.getItem((mPlaylistIndex + index) % mPlaylist.getCount());
    }

    @Override
    public void next() {
        mPlaylistIndex = calculateIndexOfNext();
        if (mPlaylistIndex >= mPlaylist.getCount()) {
            mPlaylistIndex = 0;
        }

        // remove item 0 in array
        if (mTracks[0] != null) {
            mTracks[0].destroy();
            mTracks[0] = null;
        }
        for (int i = 0; i < mTracks.length - 1; i++) {
            mTracks[i] = mTracks[i + 1];
            mTrackIds[i] = mTrackIds[i + 1];
        }
        mTracks[mTracks.length - 1] = null;
        mTrackIds[mTracks.length - 1] = -1;

        // check that current is still valid
        for (int i = 0; i < mTracks.length; i++) {
            final Track track = mTracks[i];
            if (track != null && !trackIsWhereItShould(i)) {
                Debug.logQueue("Queue next: removing invalid track '" + track.getName() + "' at " + i);
                track.destroy();
                mTracks[i] = null;
                mTrackIds[i] = -1;
            }
        }
    }

    private int calculateIndexOfNext() {
        if (trackIsWhereItShould(0)) {
            Debug.logQueue("Queue next: current track is still at same position " + mPlaylistIndex);
            return mPlaylistIndex + 1;
        }
        // -> current track has moved or been removed

        // check if current track has moved
        final int currentIndex = findTrack(0);
        if (currentIndex >= 0) { // -> moved
            Debug.logQueue(String.format("Queue next: current track has moved (%d -> %d), adjusting", mPlaylistIndex, currentIndex));
            return currentIndex + 1;
        }
        // -> current track has been removed

        // check if any of the next tracks have been moved
        for (int i = 1; i < mTracks.length; i++) {
            final int playlistIndex = findTrack(i);
            if (playlistIndex >= 0) { // track has moved
                Debug.logQueue("Queue next: current track no longer exists in playlist, but the next track exists at pos: " + playlistIndex);
                return playlistIndex;
            }
        }
        // -> all known tracks are removed

        Debug.logQueue("Queue next: neither current nor next tracks exist in playlist, use same index as before: " + mPlaylistIndex);
        return mPlaylistIndex;
    }

    private int findTrack(int index) {
        if (mTracks[index] == null) {
            return -1;
        }
        final int id = mTrackIds[index];
        final int count = mPlaylist.getCount();
        for (int i = 0; i < count; i++) {
            final Track track = mPlaylist.getItem(i);
            if (track != null && track.getId() == id) {
                return i;
            }
        }
        return -1;
    }

    private boolean trackIsWhereItShould(int index) {
        if (mTracks[index] == null) {
            return false;
        }
        final int id = mTrackIds[index];
        final Track track = getTrackAtPlaylistIndex(index);
        return (track != null && track.getId() == id);
    }

    @Override
    public void onPlaylistUpdateInProgress(boolean done) {
        if (done) {
            Debug.logQueue("Queue: playlist updated");
            //TODO update queue
        }
    }

    @Override
    public void onPlaylistRenamed() {
    }

    @Override
    public void onPlaylistStateChanged() {
    }
}
