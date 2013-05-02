#ifndef WIGWAMLABS_IMAGEPROVIDER_H_INCLUDED
#define WIGWAMLABS_IMAGEPROVIDER_H_INCLUDED

#include <libspotify/api.h>

namespace wigwamlabs {

class Session;

class ImageProviderCallback {
public:
    virtual void onImageLoaded(sp_image *image) = 0;
};

class ImageProvider {
public:
    ImageProvider(Session *session);
    ~ImageProvider();

    void setCallback(ImageProviderCallback *callback);

    void requestLoad(const char *imageLinkStr);
private:
    static void onImageLoaded(sp_image *image, void *userdata);

    sp_session *mSession;
    ImageProviderCallback *mCallback;
};

}

#endif // WIGWAMLABS_IMAGEPROVIDER_H_INCLUDED

