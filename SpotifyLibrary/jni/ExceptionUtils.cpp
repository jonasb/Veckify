#include "ExceptionUtils.h"

namespace wigwamlabs {

jclass ExceptionUtils::sRuntimeException = 0;

void ExceptionUtils::throwException(JNIEnv *env, Exception exception, const char *message) {
    jclass klass;
    switch (exception) {
    case RUNTIME_EXCEPTION:
    default:
        klass = getException(env, sRuntimeException, "java/lang/RuntimeException");
        break;
    }
    env->ThrowNew(klass, message);
}

jclass ExceptionUtils::getException(JNIEnv *env, jclass &exception, const char *name) {
    if (!exception) {
        // FindClass returns local references, so need to make it global
        jclass localException = env->FindClass(name);
        if (localException) {
            exception = static_cast<jclass>(env->NewGlobalRef(localException));
        }
    }
    return exception;
}

}
