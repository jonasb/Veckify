#define LOG_TAG "Playlist"
#define LOG_NDEBUG 0
#include "log.h"

#include "Playlist.h"

namespace wigwamlabs {

Playlist::Playlist(sp_playlist *playlist, bool owner) :
    mPlaylist(playlist),
    mOwner(owner) {
}

sp_error Playlist::destroy() {
    LOGV("destroy()");
    sp_error error = SP_ERROR_OK;
    if (mPlaylist) {
        if (mOwner) {
            error = sp_playlist_release(mPlaylist);
        }
        mPlaylist = NULL;
    }
    return error;
}

const char *Playlist::getName() {
    return sp_playlist_name(mPlaylist);
}

} // namespace wigwamlabs

