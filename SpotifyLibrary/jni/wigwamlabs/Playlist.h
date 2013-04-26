#ifndef WIGWAMLABS_PLAYLIST_H_INCLUDED
#define WIGWAMLABS_PLAYLIST_H_INCLUDED

#include <libspotify/api.h>

namespace wigwamlabs {

class Session;
class Track;

class PlaylistCallback {
public:
    virtual void onTracksMoved(const int *oldPositions, int oldPositionCount, int newPosition) = 0;
    virtual void onPlaylistRenamed() = 0;
    virtual void onPlaylistStateChanged() = 0;
    virtual void onPlaylistUpdateInProgress(bool done) = 0;
};

class Playlist {
public:
    static Playlist *create(Session *session, const char *linkStr);
    Playlist(sp_playlist *playlist);
    Playlist *clone();
    sp_error destroy();
    ~Playlist();

    void setCallback(PlaylistCallback *callback);
    bool isLoaded() const;
    sp_link *getLink() const;
    const char *getName();
    int getCount();
    Track *getTrack(int index);
    sp_error setOfflineMode(Session *session, bool offline);
    sp_playlist_offline_status getOfflineStatus(Session *session);
    int getOfflineDownloadCompleted(Session *session);

private:
    static void onTracksAdded(sp_playlist *playlist, sp_track * const *tracks, int numTracks, int position, void *self);
    static void onTracksRemoved(sp_playlist *playlist, const int *tracks, int numTracks, void *self);
    static void onTracksMoved(sp_playlist *playlist, const int *tracks, int numTracks, int newPosition, void *self);
    static void onPlaylistRenamed(sp_playlist *playlist, void *self);
    static void onPlaylistStateChanged(sp_playlist *playlist, void *self);
    static void onPlaylistUpdateInProgress(sp_playlist *playlist, bool done, void *self);
private:
    sp_playlist *mPlaylist;
    PlaylistCallback *mCallback;
    sp_playlist_callbacks mCallbacks;
};

}

#endif // WIGWAMLABS_PLAYLIST_H_INCLUDED

