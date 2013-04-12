#define LOG_TAG "com_wigwamlabs_spotify_Placeholder"
//#define LOG_NDEBUG 0
#include "log.h"

#include <jni.h>
#include "utils.h"
#include "ExceptionUtils.h"
#include "wigwamlabs/Placeholder.h"

using namespace wigwamlabs;

jfieldID sPlaceholderHandleField = 0;

Placeholder *getNativePlaceholder(JNIEnv *env, jobject object) {
    const jint handle = env->GetIntField(object, sPlaceholderHandleField);
    return reinterpret_cast<Placeholder *>(handle);
}

JNI_STATIC_METHOD(void, com_wigwamlabs_spotify_Placeholder, nativeInitClass) {
    LOGV("nativeInitClass()");

    if (sPlaceholderHandleField == 0) {
        sPlaceholderHandleField = env->GetFieldID(klass, "mHandle", "I");
    }
}

JNI_METHOD(void, com_wigwamlabs_spotify_Placeholder, nativeDestroy) {
    LOGV("nativeDestroy()");

    Placeholder *placeholder = getNativePlaceholder(env, self);
    delete placeholder;
}

