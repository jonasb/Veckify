#ifndef WIGWAMLABS_PLAYLIST_H_INCLUDED
#define WIGWAMLABS_PLAYLIST_H_INCLUDED

#include <libspotify/api.h>

namespace wigwamlabs {

class Track;

class Playlist {
public:
    Playlist(sp_playlist *playlist);
    Playlist *clone();
    sp_error destroy();

    const char *getName();
    int getCount();
    Track *getTrack(int index);

private:
    sp_playlist *mPlaylist;
    sp_playlist_callbacks mCallbacks;
};

}

#endif // WIGWAMLABS_PLAYLIST_H_INCLUDED

