#ifndef WIGWAMLABS_PLAYLISTCONTAINER_H_INCLUDED
#define WIGWAMLABS_PLAYLISTCONTAINER_H_INCLUDED

#include <libspotify/api.h>

namespace wigwamlabs {

class PlaylistContainer {
public:
    PlaylistContainer(sp_playlistcontainer *container);
    sp_error destroy();

    int getCount();

private:
    sp_playlistcontainer *mContainer;
};

}

#endif // WIGWAMLABS_PLAYLISTCONTAINER_H_INCLUDED
