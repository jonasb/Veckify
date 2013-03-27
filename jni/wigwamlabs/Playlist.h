#ifndef WIGWAMLABS_PLAYLIST_H_INCLUDED
#define WIGWAMLABS_PLAYLIST_H_INCLUDED

#include <libspotify/api.h>

namespace wigwamlabs {

class Playlist {
public:
    Playlist(sp_playlist *playlist);
    sp_error destroy();

    const char *getName();

private:
    sp_playlist *mPlaylist;
    sp_playlist_callbacks mCallbacks;
};

}

#endif // WIGWAMLABS_PLAYLIST_H_INCLUDED

