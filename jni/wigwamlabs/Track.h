#ifndef WIGWAMLABS_TRACK_H_INCLUDED
#define WIGWAMLABS_TRACK_H_INCLUDED

#include <libspotify/api.h>

namespace wigwamlabs {

class Artist;

class Track {
public:
    static Track *create(const char *linkStr);
    Track(sp_track *track);
    sp_error destroy();
    ~Track();

    const char *getName() const;
    int getArtistCount() const;
    Artist *getArtist(int index) const;

private:
    sp_track *mTrack;
};

}

#endif // WIGWAMLABS_TRACK_H_INCLUDED

