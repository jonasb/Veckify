#ifndef UTILS_H_INCLUDED
#define UTILS_H_INCLUDED

#define JNI_STATIC_METHOD(returnType, klassName, name) \
    extern "C" JNIEXPORT returnType JNICALL Java_ ## klassName ## _ ## name (JNIEnv *env, jclass klass)

#define JNI_STATIC_METHOD_ARGS(returnType, klassName, name, ...) \
    extern "C" JNIEXPORT returnType JNICALL Java_ ## klassName ## _ ## name (JNIEnv *env, jclass klass, __VA_ARGS__)

#define JNI_METHOD(returnType, klass, name) \
    extern "C" JNIEXPORT returnType JNICALL Java_ ## klass ## _ ## name (JNIEnv *env, jobject self)

#define JNI_METHOD_ARGS(returnType, klass, name, ...) \
    extern "C" JNIEXPORT returnType JNICALL Java_ ## klass ## _ ## name (JNIEnv *env, jobject self, __VA_ARGS__)

namespace wigwamlabs {
    class Context;
    class Session;
    class Track;
}

wigwamlabs::Session *getNativeSession(JNIEnv *env, jobject object);
wigwamlabs::Track *getNativeTrack(JNIEnv *env, jobject object);

#endif // UTILS_H_INCLUDED
