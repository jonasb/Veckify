#ifndef WIGWAMLABS_ARTIST_H_INCLUDED
#define WIGWAMLABS_ARTIST_H_INCLUDED

#include <libspotify/api.h>

namespace wigwamlabs {

class Artist {
public:
    Artist(sp_artist *artist);
    ~Artist();
    sp_error destroy();

    const char *getName() const;

private:
    sp_artist *mArtist;
};

}

#endif // WIGWAMLABS_ARTIST_H_INCLUDED
