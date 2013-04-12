#ifndef JNIENVPROVIDER_H_INCLUDED
#define JNIENVPROVIDER_H_INCLUDED

#include <jni.h>
#include <pthread.h>

class JNIEnvProvider {
public:
    static JNIEnvProvider *instance(JNIEnv *env);
    JNIEnv *getEnv();
private:
    JNIEnvProvider(JavaVM *vm);
    ~JNIEnvProvider();
private:
    static JNIEnvProvider *sInstance;
    JavaVM *mVm;
    pthread_key_t mTlsEnv;
};

#endif //JNIENVPROVIDER_H_INCLUDED
