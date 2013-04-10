package com.wigwamlabs.spotify;

public class TrackPlaylist implements Queue {
    private Track mTrack;

    public TrackPlaylist(Track track) {
        mTrack = track.clone();
    }

    @Override
    public void destroy() {
        if (mTrack != null) {
            mTrack.destroy();
            mTrack = null;
        }
    }

    @Override
    public Track getTrack(int index) {
        return mTrack;
    }

    @Override
    public void onCurrentTrackUpdated(boolean playNext) {
    }
}
