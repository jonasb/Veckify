#ifndef WIGWAMLABS_TRACK_H_INCLUDED
#define WIGWAMLABS_TRACK_H_INCLUDED

#include <libspotify/api.h>

namespace wigwamlabs {

class Track {
public:
    Track(sp_track *track);
    sp_error destroy();
    ~Track();

    const char *getName() const;

private:
    sp_track *mTrack;
};

}

#endif // WIGWAMLABS_TRACK_H_INCLUDED

