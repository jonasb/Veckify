#ifndef WIGWAMLABS_FOLDERSTART_H_INCLUDED
#define WIGWAMLABS_FOLDERSTART_H_INCLUDED

#include <libspotify/api.h>

namespace wigwamlabs {

class FolderStart {
public:
    FolderStart(sp_uint64 id, const char *name);
    ~FolderStart();

    const char *getName() const;

private:
    sp_uint64 mId;
    char *mName;
};

}

#endif // WIGWAMLABS_FOLDERSTART_H_INCLUDED

