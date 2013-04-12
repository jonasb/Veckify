#ifndef WIGWAMLABS_FOLDEREND_H_INCLUDED
#define WIGWAMLABS_FOLDEREND_H_INCLUDED

#include <libspotify/api.h>

namespace wigwamlabs {

class FolderEnd {
public:
    FolderEnd(sp_uint64 id);
    ~FolderEnd();

private:
    sp_uint64 mId;
};

}

#endif // WIGWAMLABS_FOLDEREND_H_INCLUDED

