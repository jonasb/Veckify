package com.wigwamlabs.spotify;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Pair;

import com.wigwamlabs.spotify.util.BitmapCacheHashMap;
import proguard.annotation.Keep;

import java.util.ArrayList;
import java.util.Iterator;

public class ImageProvider extends NativeItem {
    static {
        nativeInitClass();
    }

    public static final int SIZE_NORMAL = 0; // 300x300
    public static final int SIZE_SMALL = 1; // 64x64
    public static final int SIZE_LARGE = 2;
    private final BitmapCacheHashMap<String> mImages = new BitmapCacheHashMap<String>(20, 2 * 1024 * 1024);
    private final Handler mHandler = new Handler();
    private final ArrayList<Pair<String, Callback>> mLoadRequests = new ArrayList<Pair<String, Callback>>();

    public ImageProvider(Session session) {
        super(0);

        setHandle(nativeCreate(session));
    }

    private static native void nativeInitClass();

    private native int nativeCreate(Session session);

    @Override
    native void nativeDestroy();

    private native void nativeRequestLoad(String imageLink);

    @Override
    public void destroy() {
        super.destroy();

        for (String key : mImages.keySet()) {
            final Bitmap bitmap = mImages.get(key);
            bitmap.recycle();
        }
        mImages.clear();
        mLoadRequests.clear();
    }

    public Bitmap get(String imageLink) {
        return mImages.get(imageLink);
    }

    public void load(String imageLink, Callback callback, boolean replace) {
        boolean alreadyRequested = false;
        for (Iterator<Pair<String, Callback>> it = mLoadRequests.iterator(); it.hasNext(); ) {
            final Pair<String, Callback> req = it.next();
            if (imageLink.equals(req.first)) {
                alreadyRequested = true;
                if (!replace) {
                    break;
                }
            }
            if (replace && req.second == callback) {
                it.remove();
            }
        }
        if (callback != null) {
            mLoadRequests.add(Pair.create(imageLink, callback));
        }
        if (!alreadyRequested) {
            nativeRequestLoad(imageLink); //TODO handle error?
        }
    }

    @Keep
    private void onImageLoaded(final String imageLink, final byte[] imageData) {
        Bitmap loadedBitmap = null;
        if (imageData == null || imageData.length == 0) {
            Debug.logImageProvider("Got no image data for link:" + imageLink);
        } else {
            try {
                loadedBitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                Debug.logImageProvider("Loaded bitmap: " + loadedBitmap.getWidth() + " x " + loadedBitmap.getHeight() + " for link: " + imageLink);
            } catch (Exception e) {
                Debug.logImageProvider("Exception during loading of image for link: " + imageLink, e);
            }
        }
        final Bitmap bitmap = loadedBitmap;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mImages.put(imageLink, bitmap); // store even errors (null)

                final Iterator<Pair<String, Callback>> it = mLoadRequests.iterator();
                while (it.hasNext()) {
                    final Pair<String, Callback> next = it.next();
                    if (next.first.equals(imageLink)) {
                        next.second.onImageImageLoaded(imageLink, bitmap);
                        it.remove();
                    }
                }
            }
        });
    }

    public interface Callback {
        void onImageImageLoaded(String imageLink, Bitmap image);
    }
}
