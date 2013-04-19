#define LOG_TAG "Playlist"
#define LOG_NDEBUG 0
#include "log.h"

#include "Playlist.h"
#include "Session.h"
#include "Track.h"
#include <libspotify/api.h>
#include <string.h>

namespace wigwamlabs {

Playlist *Playlist::create(Session *session, const char *linkStr) {
    Playlist *instance = NULL;
    sp_link *link = sp_link_create_from_string(linkStr);
    if (!link) {
        LOGW("%s: Link: '%s' seems to be invalid", __func__, linkStr);
        return NULL;
    }

    sp_playlist *playlist = sp_playlist_create(session->getSession(), link);
    if (playlist) {
        instance = new Playlist(playlist);

        sp_playlist_release(playlist);
    } else {
        LOGW("%s: failed to get playlist from link: '%s'", __func__, linkStr);
    }

    sp_link_release(link);

    return instance;
}

Playlist::Playlist(sp_playlist *playlist) :
    mPlaylist(playlist),
    mCallback(NULL) {
    sp_playlist_add_ref(playlist);

    memset(&mCallbacks, 0, sizeof(sp_playlist_callbacks));
    mCallbacks.tracks_added = onTracksAdded;
    mCallbacks.tracks_removed = onTracksRemoved;
    mCallbacks.tracks_moved = onTracksMoved;
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

Playlist::~Playlist() {
    delete mCallback;
}

void Playlist::setCallback(PlaylistCallback *callback) {
    //assert(!mCallback);
    mCallback = callback;
}

bool Playlist::isLoaded() const {
    return sp_playlist_is_loaded(mPlaylist);
}

sp_link *Playlist::getLink() const {
    return sp_link_create_from_playlist(mPlaylist);
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

void Playlist::onTracksAdded(sp_playlist *playlist, sp_track * const *tracks, int numTracks, int position, void *self) {
    LOGV("%s count: %d, position: %d", __func__, numTracks, position);
    PlaylistCallback *callback = static_cast<Playlist *>(self)->mCallback;
    if (callback) {
        int *trackPositions = new int[numTracks];
        for (int i = 0; i < numTracks; i++) {
            trackPositions[i] = -1;
        }

        callback->onTracksMoved(trackPositions, numTracks, position);

        delete[] trackPositions;
    }
}

void Playlist::onTracksRemoved(sp_playlist *playlist, const int *tracks, int numTracks, void *self) {
    LOGV("%s count: %d", __func__, numTracks);
    PlaylistCallback *callback = static_cast<Playlist *>(self)->mCallback;
    if (callback) {
        callback->onTracksMoved(tracks, numTracks, -1);
    }
}

void Playlist::onTracksMoved(sp_playlist *playlist, const int *tracks, int numTracks, int newPosition, void *self) {
    LOGV("%s count: %d, newPosition: %d", __func__, numTracks, newPosition);
    PlaylistCallback *callback = static_cast<Playlist *>(self)->mCallback;
    if (callback) {
        callback->onTracksMoved(tracks, numTracks, newPosition);
    }
}

void Playlist::onPlaylistRenamed(sp_playlist *playlist, void *self) {
    LOGV("%s", __func__);
    PlaylistCallback *callback = static_cast<Playlist *>(self)->mCallback;
    if (callback) {
        callback->onPlaylistRenamed();
    }
}

void Playlist::onPlaylistStateChanged(sp_playlist *playlist, void *self) {
    LOGV(__func__);
    PlaylistCallback *callback = static_cast<Playlist *>(self)->mCallback;
    if (callback) {
        callback->onPlaylistStateChanged();
    }
}

void Playlist::onPlaylistUpdateInProgress(sp_playlist *playlist, bool done, void *self) {
    LOGV("%s (%d)", __func__, done);
    PlaylistCallback *callback = static_cast<Playlist *>(self)->mCallback;
    if (callback) {
        callback->onPlaylistUpdateInProgress(done);
    }
}

} // namespace wigwamlabs

