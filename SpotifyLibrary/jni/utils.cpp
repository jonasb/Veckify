#include "utils.h"

char linkStr[256];

jstring convertLinkToString(JNIEnv *env, sp_link *link) {
    if (!link) {
        return 0;
    }

    sp_link_as_string(link, linkStr, sizeof(linkStr));
    sp_link_release(link);

    return env->NewStringUTF(linkStr);
}
