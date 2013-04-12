#define LOG_TAG "FolderStart"
#define LOG_NDEBUG 0
#include "log.h"

#include "FolderStart.h"
#include <string.h>

namespace wigwamlabs {

FolderStart::FolderStart(sp_uint64 id, const char *name) :
    mId(id) {
    size_t size = strlen(name) + 1;
    mName = new char[size];
    strlcpy(mName, name, size);
}

FolderStart::~FolderStart() {
    delete[] mName;
}

const char *FolderStart::getName() const {
    return mName;
}

} // namespace wigwamlabs

