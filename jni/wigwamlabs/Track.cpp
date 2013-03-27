#define LOG_TAG "Track"
#define LOG_NDEBUG 0
#include "log.h"

#include "Track.h"

namespace wigwamlabs {

Track::Track(sp_track *track) :
    mTrack(track) {
    sp_track_add_ref(track);
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

const char *Track::getName() const {
    return sp_track_name(mTrack);
}

} // namespace wigwamlabs

