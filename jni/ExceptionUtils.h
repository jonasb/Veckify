#ifndef EXCEPTIONUTILS_H_INCLUDED
#define EXCEPTIONUTILS_H_INCLUDED

#include <jni.h>

namespace wigwamlabs {

class ExceptionUtils {
public:
    enum Exception {
        RUNTIME_EXCEPTION,
    };

    static void throwException(JNIEnv *env, Exception exception, const char *message);
private:
    static jclass getException(JNIEnv *env, jclass &exception, const char *name);

    static jclass sRuntimeException;
};

}

#endif // EXCEXCEPTIONUTILS_H_INCLUDED
