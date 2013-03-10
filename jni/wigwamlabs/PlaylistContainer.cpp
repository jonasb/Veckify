#define LOG_TAG "PlaylistContainer"
#define LOG_NDEBUG 0
#include "log.h"

#include "PlaylistContainer.h"
#include "Playlist.h"

namespace wigwamlabs {

void PlaylistContainer::onPlaylistAdded(sp_playlistcontainer *container, sp_playlist *playlist, int position, void *self) {
    LOGV("onPlaylistAdded()");
}

void PlaylistContainer::onPlaylistRemoved(sp_playlistcontainer *container, sp_playlist *playlist, int position, void *self) {
    LOGV("onPlaylistRemoved()");
}

void PlaylistContainer::onPlaylistMoved(sp_playlistcontainer *container, sp_playlist *playlist, int position, int newPosition, void *self) {
    LOGV("onPlaylistMoved()");
}

void PlaylistContainer::onContainerLoaded(sp_playlistcontainer *container, void *self) {
    LOGV("onContainerLoaded()");
    static_cast<PlaylistContainer *>(self)->mCallback->onContainerLoaded();
}

PlaylistContainer::PlaylistContainer(sp_playlistcontainer *container) :
    mContainer(container),
    mCallback(NULL) {

    mCallbacks.playlist_added = onPlaylistAdded;
    mCallbacks.playlist_removed = onPlaylistRemoved;
    mCallbacks.playlist_moved = onPlaylistMoved;
    mCallbacks.container_loaded = onContainerLoaded;

    sp_playlistcontainer_add_callbacks(mContainer, &mCallbacks, this);
}

PlaylistContainer::~PlaylistContainer() {
    delete mCallback;
}

sp_error PlaylistContainer::destroy() {
    LOGV("destroy()");
    sp_error error = SP_ERROR_OK;
    if (mContainer) {
        sp_playlistcontainer_remove_callbacks(mContainer, &mCallbacks, this);
        //TODO crashes sometimes, should not release when comes from session?  error = sp_playlistcontainer_release(mContainer);
        mContainer = NULL;
    }
    return error;
}

void PlaylistContainer::setCallback(PlaylistContainerCallback *callback) {
    //assert(!mCallback);
    mCallback = callback;
}

int PlaylistContainer::getCount() {
    return sp_playlistcontainer_num_playlists(mContainer);
}

Playlist *PlaylistContainer::getPlaylist(int index) {
    sp_playlist *playlist = sp_playlistcontainer_playlist(mContainer, index);
    if (playlist == NULL) {
        return NULL;
    }
    return new Playlist(playlist, false);
}

} // namespace wigwamlabs
