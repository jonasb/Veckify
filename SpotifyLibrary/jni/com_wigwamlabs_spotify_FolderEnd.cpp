#define LOG_TAG "com_wigwamlabs_spotify_FolderEnd"
//#define LOG_NDEBUG 0
#include "log.h"

#include <jni.h>
#include "utils.h"
#include "ExceptionUtils.h"
#include "wigwamlabs/FolderEnd.h"

using namespace wigwamlabs;

jfieldID sFolderEndHandleField = 0;

FolderEnd *getNativeFolderEnd(JNIEnv *env, jobject object) {
    const jint handle = env->GetIntField(object, sFolderEndHandleField);
    return reinterpret_cast<FolderEnd *>(handle);
}

JNI_STATIC_METHOD(void, com_wigwamlabs_spotify_FolderEnd, nativeInitClass) {
    LOGV("nativeInitClass()");

    if (sFolderEndHandleField == 0) {
        sFolderEndHandleField = env->GetFieldID(klass, "mHandle", "I");
    }
}

JNI_METHOD(void, com_wigwamlabs_spotify_FolderEnd, nativeDestroy) {
    LOGV("nativeDestroy()");

    FolderEnd *folderEnd = getNativeFolderEnd(env, self);
    delete folderEnd;
}

