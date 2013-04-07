package com.wigwamlabs.spotify;

public class TrackPlaylist implements Queue {
    private Track mTrack;

    public TrackPlaylist(Track track) {
        mTrack = track.clone();
    }

    public void destroy() {
        if (mTrack != null) {
            mTrack.destroy();
            mTrack = null;
        }
    }

    public Track getTrack(int index) {
        return mTrack;
    }

    public void onCurrentTrackUpdated(boolean playNext) {
    }
}
