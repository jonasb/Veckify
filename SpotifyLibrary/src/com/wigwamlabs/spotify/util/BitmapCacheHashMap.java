package com.wigwamlabs.spotify.util;

import android.graphics.Bitmap;

import com.wigwamlabs.spotify.Debug;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class BitmapCacheHashMap<Key> extends LinkedHashMap<Key, Bitmap> {
    private static final int TINY_BITMAP_SIZE = 64 * 64 * 4;
    private final int mLimitBytes;
    private final int mLimitSize;
    int mSizeBytes;

    public BitmapCacheHashMap(int limitSize, int limitBytes) {
        super(100, 0.75f, true);
        mLimitSize = limitSize;
        mLimitBytes = limitBytes;
    }

    @Override
    public void clear() {
        Debug.logBitmapCache("clearing");
        mSizeBytes = 0;
        super.clear();
    }

    @Override
    public Bitmap put(Key key, Bitmap value) {
        final Bitmap oldValue = super.put(key, value);
        if (oldValue != null) {
            final int size = oldValue.getRowBytes() * oldValue.getHeight();
            Debug.logBitmapCache("replacing old bitmap: " + mSizeBytes + " - " + size + " = " + (mSizeBytes - size));
            mSizeBytes -= size;
        }
        if (value != null) {
            final int size = value.getRowBytes() * value.getHeight();
            Debug.logBitmapCache("adding new bitmap: " + mSizeBytes + " + " + size + " = " + (mSizeBytes + size));
            mSizeBytes += size;
            removeOldEntries();
        }
        return oldValue;
    }

    @Override
    public Bitmap remove(Object key) {
        final Bitmap oldValue = super.remove(key);
        if (oldValue != null) {
            final int bitmapSize = oldValue.getRowBytes() * oldValue.getHeight();
            Debug.logBitmapCache("remove old bitmap: " + mSizeBytes + " - " + bitmapSize + " = " + (mSizeBytes - bitmapSize));
            mSizeBytes -= bitmapSize;
        }
        return oldValue;
    }

    private void removeOldEntries() {
        // remove non-tiny bitmaps
        if (mSizeBytes > mLimitBytes) {
            final Iterator<Map.Entry<Key, Bitmap>> it = entrySet().iterator();
            while (mSizeBytes > mLimitBytes) {
                if (!it.hasNext()) {
                    break;
                }
                final Bitmap bitmap = it.next().getValue();
                final int bitmapSize = (bitmap != null ? bitmap.getRowBytes() * bitmap.getHeight() : 0);
                if (bitmapSize > TINY_BITMAP_SIZE) {
                    it.remove();
                }
            }
        }

        // remove entries no matter which size
        int entriesToRemove = size() - mLimitSize;
        if (entriesToRemove > 0) {
            final Iterator<Map.Entry<Key, Bitmap>> it = entrySet().iterator();
            while (entriesToRemove > 0) {
                if (!it.hasNext()) {
                    break;
                }
                it.next();
                it.remove();
                entriesToRemove--;
            }
        }
    }
}
