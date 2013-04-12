package com.wigwamlabs.spotify;

public interface Queue {
    void destroy();

    Track getTrack(int index);

    void onCurrentTrackUpdated(boolean playNext);
}
