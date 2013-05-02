package com.wigwamlabs.spotify.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;

import com.wigwamlabs.spotify.ImageProvider;

public class SpotifyImageView extends android.widget.ImageView implements ImageProvider.Callback {
    private ImageProvider mImageProvider;
    private String mImageLink;

    public SpotifyImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setVisibility(GONE);
    }

    public void setImageProvider(ImageProvider imageProvider) {
        if (mImageProvider == null) {
            mImageProvider = imageProvider;
            loadImage();
        }
        mImageProvider = imageProvider;
    }

    public void setImageLink(String imageLink) {
        mImageLink = imageLink;
        loadImage();
    }

    private void loadImage() {
        if (mImageProvider == null || mImageLink == null) {
            setVisibility(GONE);
            return;
        }
        final Bitmap image = mImageProvider.get(mImageLink);
        if (image != null) {
            setImageBitmap(image);
            setVisibility(VISIBLE);
        } else {
            mImageProvider.load(mImageLink, this, true);
            setVisibility(GONE);
        }
    }

    @Override
    public void onImageImageLoaded(String imageLink, Bitmap image) {
        if (imageLink.equals(mImageLink)) {
            setImageBitmap(image);
            setVisibility(VISIBLE);
        }
    }
}
