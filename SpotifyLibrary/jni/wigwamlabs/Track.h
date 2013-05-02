#ifndef WIGWAMLABS_TRACK_H_INCLUDED
#define WIGWAMLABS_TRACK_H_INCLUDED

#include <libspotify/api.h>

namespace wigwamlabs {

class Artist;
class Session;

class Track {
public:
    static Track *create(const char *linkStr);
    Track(sp_track *track);
    Track *clone();
    sp_error destroy();
    ~Track();

    sp_track *getTrack();
    const char *getName() const;
    int getArtistCount() const;
    Artist *getArtist(int index) const;
    sp_link *getImageLink(sp_image_size size);
    int getDurationMs() const;
    sp_track_availability getAvailability(Session *session) const;

private:
    sp_track *mTrack;
};

}

#endif // WIGWAMLABS_TRACK_H_INCLUDED

