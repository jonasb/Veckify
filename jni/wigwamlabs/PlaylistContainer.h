#ifndef WIGWAMLABS_PLAYLISTCONTAINER_H_INCLUDED
#define WIGWAMLABS_PLAYLISTCONTAINER_H_INCLUDED

#include <libspotify/api.h>

namespace wigwamlabs {

class Playlist;

class PlaylistContainer {
public:
    PlaylistContainer(sp_playlistcontainer *container);
    sp_error destroy();

    int getCount();
    Playlist *getPlaylist(int index);

private:
    sp_playlistcontainer *mContainer;
};

}

#endif // WIGWAMLABS_PLAYLISTCONTAINER_H_INCLUDED
