#define LOG_TAG "PlaylistContainer"
#define LOG_NDEBUG 0
#include "log.h"

#include "PlaylistContainer.h"
#include "Playlist.h"

namespace wigwamlabs {

PlaylistContainer::PlaylistContainer(sp_playlistcontainer *container) :
    mContainer(container) {
}

sp_error PlaylistContainer::destroy() {
    LOGV("destroy()");
    sp_error error = SP_ERROR_OK;
    if (mContainer) {
        //TODO crashes sometimes, should not release when comes from session?  error = sp_playlistcontainer_release(mContainer);
        mContainer = NULL;
    }
    return error;
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
