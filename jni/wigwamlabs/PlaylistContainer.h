#ifndef WIGWAMLABS_PLAYLISTCONTAINER_H_INCLUDED
#define WIGWAMLABS_PLAYLISTCONTAINER_H_INCLUDED

#include <libspotify/api.h>

namespace wigwamlabs {

class Playlist;

class PlaylistContainerCallback {
public:
    virtual void onContainerLoaded() = 0;
};

class PlaylistContainer {
public:
    PlaylistContainer(sp_playlistcontainer *container);
    sp_error destroy();
    ~PlaylistContainer();

    void setCallback(PlaylistContainerCallback *callback);

    int getCount();
    Playlist *getPlaylist(int index);

private:
    static void onPlaylistAdded(sp_playlistcontainer *container, sp_playlist *playlist, int position, void *self);
    static void onPlaylistRemoved(sp_playlistcontainer *container, sp_playlist *playlist, int position, void *self);
    static void onPlaylistMoved(sp_playlistcontainer *container, sp_playlist *playlist, int position, int newPosition, void *self);
    static void onContainerLoaded(sp_playlistcontainer *container, void *self);

private:
    sp_playlistcontainer *mContainer;
    PlaylistContainerCallback *mCallback;
    sp_playlistcontainer_callbacks mCallbacks;
};

}

#endif // WIGWAMLABS_PLAYLISTCONTAINER_H_INCLUDED
