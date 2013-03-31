#define LOG_TAG "JNIEnvProvider"
//#define LOG_NDEBUG 0
#include "log.h"

#include "JNIEnvProvider.h"

JNIEnvProvider *JNIEnvProvider::sInstance = NULL;

JNIEnvProvider *JNIEnvProvider::instance(JNIEnv *env) {
    if (!sInstance) {
        JavaVM *vm = NULL;
        env->GetJavaVM(&vm);
        sInstance = new JNIEnvProvider(vm);
    }
    return sInstance;
}

JNIEnvProvider::JNIEnvProvider(JavaVM *vm) :
    mVm(vm),
    mTlsEnv(0) {
    /*ignore=*/ pthread_key_create(&mTlsEnv, NULL); //TODO destructor
}

JNIEnvProvider::~JNIEnvProvider() {
    //TODO currently this is never called since we're using a singleton
    pthread_key_delete(mTlsEnv);
}

JNIEnv *JNIEnvProvider::getEnv() {
    JNIEnv *env = static_cast<JNIEnv *>(pthread_getspecific(mTlsEnv));
    if (!env) {
        LOGV("Initializing JNI env for thread: %d", pthread_self());
        JavaVMAttachArgs args;
        args.version = JNI_VERSION_1_6;
        args.name = NULL;
        args.group = NULL;
        mVm->AttachCurrentThread(&env, &args); //TODO DetatchCurrentThread()?
        pthread_setspecific(mTlsEnv, env);
    }
    return env;
}
