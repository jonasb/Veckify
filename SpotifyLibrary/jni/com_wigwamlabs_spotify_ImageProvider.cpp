#define LOG_TAG "com_wigwamlabs_spotify_ImageProvider"
#define LOG_NDEBUG 0
#include "log.h"

#include <jni.h>
#include "utils.h"
#include "ExceptionUtils.h"
#include "JNIEnvProvider.h"
#include "wigwamlabs/ImageProvider.h"

using namespace wigwamlabs;

jfieldID sImageProviderHandleField = 0;
jmethodID sImageProviderOnImageLoadedMethod = 0;

class ImageProviderCallbackJNI : public ImageProviderCallback {
public:
    ImageProviderCallbackJNI(JNIEnv *env, jobject imageProvider) :
        mProvider(JNIEnvProvider::instance(env)) {
        mImageProvider = env->NewGlobalRef(imageProvider);
    }

    ~ImageProviderCallbackJNI() {
        mProvider->getEnv()->DeleteGlobalRef(mImageProvider);
    }

    void onImageLoaded(sp_image *image) {
        LOGV(__func__);
        JNIEnv *env = mProvider->getEnv();

        sp_link *link = sp_link_create_from_image(image);
        jstring linkStr = convertLinkToString(env, link);

        jbyteArray imageData = 0;
        sp_error error = sp_image_error(image);
        if (error != SP_ERROR_OK) {
            LOGW("Image got error: %s", sp_error_message(error));
        } else {
            size_t dataSize = 0;
            const void *data = sp_image_data(image, &dataSize);
            if (dataSize == 0 || data == NULL) {
                LOGW("Got no data from image");
            } else {
                imageData = env->NewByteArray(dataSize);
                env->SetByteArrayRegion(imageData, 0, dataSize, (const jbyte*) data);
            }
        }

        env->CallVoidMethod(mImageProvider, sImageProviderOnImageLoadedMethod, linkStr, imageData);

        if (imageData) {
            env->DeleteLocalRef(imageData);
        }
        env->DeleteLocalRef(linkStr);
    }

private:
    JNIEnvProvider *mProvider;
    jobject mImageProvider;
};

ImageProvider *getNativeImageProvider(JNIEnv *env, jobject object) {
    const jint handle = env->GetIntField(object, sImageProviderHandleField);
    return reinterpret_cast<ImageProvider *>(handle);
}

JNI_STATIC_METHOD(void, com_wigwamlabs_spotify_ImageProvider, nativeInitClass) {
    LOGV("nativeInitClass()");
    if (sImageProviderHandleField == 0) {
        sImageProviderHandleField = env->GetFieldID(klass, "mHandle", "I");
    }
    if (sImageProviderOnImageLoadedMethod == 0) {
        sImageProviderOnImageLoadedMethod = env->GetMethodID(klass, "onImageLoaded", "(Ljava/lang/String;[B)V");
    }
}

JNI_METHOD_ARGS(jint, com_wigwamlabs_spotify_ImageProvider, nativeCreate, jobject sessionObj) {
    LOGV("nativeCreate()");
    Session *session = getNativeSession(env, sessionObj);
    ImageProvider *imageProvider = new ImageProvider(session);
    ImageProviderCallbackJNI *callback = new ImageProviderCallbackJNI(env, self);
    imageProvider->setCallback(callback);
    return reinterpret_cast<jint>(imageProvider);
}

JNI_METHOD(void, com_wigwamlabs_spotify_ImageProvider, nativeDestroy) {
    LOGV("nativeDestroy()");
    ImageProvider *imageProvider = getNativeImageProvider(env, self);
    delete imageProvider;
}

JNI_METHOD_ARGS(void, com_wigwamlabs_spotify_ImageProvider, nativeRequestLoad, jstring imageLink) {
    LOGV("nativeRequestLoad()");
    ImageProvider *imageProvider = getNativeImageProvider(env, self);
    const char *imageLinkStr = env->GetStringUTFChars(imageLink, NULL);
    imageProvider->requestLoad(imageLinkStr);
    env->ReleaseStringUTFChars(imageLink, imageLinkStr);
}
