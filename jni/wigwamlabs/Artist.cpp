#define LOG_TAG "Artist"
#define LOG_NDEBUG 0
#include "log.h"

#include "Artist.h"

namespace wigwamlabs {

Artist::Artist(sp_artist *artist) :
    mArtist(artist) {
    sp_artist_add_ref(artist);
}

sp_error Artist::destroy() {
    LOGV(__func__);
    sp_error error = SP_ERROR_OK;
    if (mArtist) {
        error = sp_artist_release(mArtist);
        mArtist = NULL;
    }
    return error;
}

Artist::~Artist() {
}

const char *Artist::getName() const {
    return sp_artist_name(mArtist);
}

} // namespace wigwamlabs
