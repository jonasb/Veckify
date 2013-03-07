#ifndef UTILS_H_INCLUDED
#define UTILS_H_INCLUDED

namespace wigwamlabs {
    class Context;
    class Session;
}

wigwamlabs::Context *getNativeContext(JNIEnv *env, jobject object);
wigwamlabs::Session *getNativeSession(JNIEnv *env, jobject object);

#endif // UTILS_H_INCLUDED
