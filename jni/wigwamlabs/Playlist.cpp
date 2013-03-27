#define LOG_TAG "Playlist"
#define LOG_NDEBUG 0
#include "log.h"

#include "Playlist.h"
#include "Track.h"
#include <string.h>

namespace wigwamlabs {

void onTracksAdded(sp_playlist *playlist, sp_track * const *tracks, int numTracks, int position, void *self) {
    LOGV("onTracksAdded()");
}

void onTracksRemoved(sp_playlist *playlist, const int *tracks, int numTracks, void *self) {
    LOGV("onTracksRemoved()");
}

void onPlaylistRenamed(sp_playlist *playlist, void *self) {
    LOGV("onPlaylistRenamed()");
}

void onPlaylistStateChanged(sp_playlist *playlist, void *self) {
    LOGV("onPlaylistStateChanged()");
}

void onPlaylistUpdateInProgress(sp_playlist *playlist, bool done, void *self) {
    LOGV("onPlaylistUpdateInProgress()");
}

Playlist::Playlist(sp_playlist *playlist) :
    mPlaylist(playlist) {
    sp_playlist_add_ref(playlist);

    memset(&mCallbacks, 0, sizeof(sp_playlist_callbacks));
    mCallbacks.tracks_added = onTracksAdded;
    mCallbacks.tracks_removed = onTracksRemoved;
    mCallbacks.tracks_moved = NULL;
    mCallbacks.playlist_renamed = onPlaylistRenamed;
    mCallbacks.playlist_state_changed = onPlaylistStateChanged;
    mCallbacks.playlist_update_in_progress = onPlaylistUpdateInProgress;
    mCallbacks.playlist_metadata_updated = NULL;
    mCallbacks.track_created_changed = NULL;
    mCallbacks.track_seen_changed = NULL;
    mCallbacks.description_changed = NULL;
    mCallbacks.image_changed = NULL;
    mCallbacks.track_message_changed = NULL;
    mCallbacks.subscribers_changed = NULL;

    /* ignore = */ sp_playlist_add_callbacks(playlist, &mCallbacks, this);
}

Playlist *Playlist::clone() {
    return new Playlist(mPlaylist);
}

sp_error Playlist::destroy() {
    LOGV("destroy()");
    sp_error error = SP_ERROR_OK;
    if (mPlaylist) {
        /* ignore = */ sp_playlist_remove_callbacks(mPlaylist, &mCallbacks, this);
        error = sp_playlist_release(mPlaylist);
        mPlaylist = NULL;
    }
    return error;
}

const char *Playlist::getName() {
    return sp_playlist_name(mPlaylist);
}

int Playlist::getCount() {
    return sp_playlist_num_tracks(mPlaylist);
}

Track *Playlist::getTrack(int index) {
    sp_track *track = sp_playlist_track(mPlaylist, index);
    if (track == NULL) {
        return NULL;
    }
    return new Track(track);
}

} // namespace wigwamlabs

