#define LOG_TAG "com_wigwamlabs_spotify_FolderStart"
//#define LOG_NDEBUG 0
#include "log.h"

#include <jni.h>
#include "utils.h"
#include "ExceptionUtils.h"
#include "wigwamlabs/FolderStart.h"

using namespace wigwamlabs;

jfieldID sFolderStartHandleField = 0;

FolderStart *getNativeFolderStart(JNIEnv *env, jobject object) {
    const jint handle = env->GetIntField(object, sFolderStartHandleField);
    return reinterpret_cast<FolderStart *>(handle);
}

JNI_STATIC_METHOD(void, com_wigwamlabs_spotify_FolderStart, nativeInitClass) {
    LOGV("nativeInitClass()");

    if (sFolderStartHandleField == 0) {
        sFolderStartHandleField = env->GetFieldID(klass, "mHandle", "I");
    }
}

JNI_METHOD(void, com_wigwamlabs_spotify_FolderStart, nativeDestroy) {
    LOGV("nativeDestroy()");

    FolderStart *folderStart = getNativeFolderStart(env, self);
    delete folderStart;
}

JNI_METHOD(jstring, com_wigwamlabs_spotify_FolderStart, nativeGetName) {
    LOGV("nativeGetName()");

    FolderStart *folderStart = getNativeFolderStart(env, self);

    const char *nameStr = folderStart->getName();

    jstring name = env->NewStringUTF(nameStr);

    return name;
}
