#define LOG_TAG "ImageProvider"
#define LOG_NDEBUG 0
#include "log.h"

#include "ImageProvider.h"
#include "Session.h"

namespace wigwamlabs {

ImageProvider::ImageProvider(Session *session) :
    mSession(session->getSession()),
    mCallback(NULL) {
}

ImageProvider::~ImageProvider() {
    delete mCallback;
}

void ImageProvider::setCallback(ImageProviderCallback *callback) {
    if (mCallback) {
        delete mCallback;
    }
    mCallback = callback;
}

void ImageProvider::requestLoad(const char *imageLinkStr) {
    LOGV("%s %s", __func__, imageLinkStr);
    // create image from link
    sp_link *link = sp_link_create_from_string(imageLinkStr);
    if (!link) {
        LOGW("%s: Link: '%s' seems to be invalid", __func__, imageLinkStr);
        return;
    }
    sp_image *image = sp_image_create_from_link(mSession, link);
    sp_link_release(link);
    link = NULL;

    //
    if (sp_image_is_loaded(image)) {
        if (mCallback) {
            mCallback->onImageLoaded(image);
        }
        sp_image_release(image);
        image = NULL;
    } else {
        sp_image_add_load_callback(image, onImageLoaded, this);
    }
}

void ImageProvider::onImageLoaded(sp_image *image, void *userdata) {
    LOGV(__func__);
    sp_image_remove_load_callback(image, onImageLoaded, userdata);
    ImageProvider *self = static_cast<ImageProvider *>(userdata);
    if (self->mCallback) {
        self->mCallback->onImageLoaded(image);
    }
    sp_image_release(image);
}

} // namespace wigwamlabs
