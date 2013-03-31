#ifndef WIGWAMLABS_PLAYLIST_H_INCLUDED
#define WIGWAMLABS_PLAYLIST_H_INCLUDED

#include <libspotify/api.h>

namespace wigwamlabs {

class Track;

class PlaylistCallback {
public:
    virtual void onTracksMoved(const int *oldPositions, int oldPositionCount, int newPosition) = 0;
};

class Playlist {
public:
    Playlist(sp_playlist *playlist);
    Playlist *clone();
    sp_error destroy();
    ~Playlist();

    void setCallback(PlaylistCallback *callback);
    const char *getName();
    int getCount();
    Track *getTrack(int index);

private:
    static void onTracksAdded(sp_playlist *playlist, sp_track * const *tracks, int numTracks, int position, void *self);
    static void onTracksRemoved(sp_playlist *playlist, const int *tracks, int numTracks, void *self);
    static void onTracksMoved(sp_playlist *playlist, const int *tracks, int numTracks, int newPosition, void *self);
private:
    sp_playlist *mPlaylist;
    PlaylistCallback *mCallback;
    sp_playlist_callbacks mCallbacks;
};

}

#endif // WIGWAMLABS_PLAYLIST_H_INCLUDED

