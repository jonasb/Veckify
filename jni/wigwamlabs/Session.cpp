#include "Session.h"
#include <stddef.h>

namespace wigwamlabs {

Session::Session(Context *context) :
    mContext(context) {
}

Session::~Session() {
    mContext = NULL;
}

} // namespace wigwamlabs
