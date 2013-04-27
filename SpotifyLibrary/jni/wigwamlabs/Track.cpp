#define LOG_TAG "Track"
//#define LOG_NDEBUG 0
#include "log.h"

#include "Track.h"
#include "Artist.h"
#include "Session.h"

namespace wigwamlabs {

Track *Track::create(const char *linkStr) {
    Track *instance = NULL;
    sp_link *link = sp_link_create_from_string(linkStr);
    if (!link) {
        return NULL;
    }

    sp_track *track = sp_link_as_track(link);
    if (track) {
        instance = new Track(track);
    }

    sp_link_release(link);

    return instance;
}

Track::Track(sp_track *track) :
    mTrack(track) {
    sp_track_add_ref(track);
}

Track *Track::clone() {
    return new Track(mTrack);
}

sp_error Track::destroy() {
    LOGV("destroy()");
    if (mTrack) {
        sp_track_release(mTrack);
        mTrack = NULL;
    }
}

Track::~Track() {
}

sp_track *Track::getTrack() {
    return mTrack;
}

const char *Track::getName() const {
    return sp_track_name(mTrack);
}

int Track::getArtistCount() const {
    return sp_track_num_artists(mTrack);
}

Artist *Track::getArtist(int index) const {
    sp_artist *artist = sp_track_artist(mTrack, index);
    return new Artist(artist);
}

int Track::getDurationMs() const {
    return sp_track_duration(mTrack);
}

sp_track_availability Track::getAvailability(Session *session) const {
    return sp_track_get_availability(session->getSession(), mTrack);
}

} // namespace wigwamlabs
