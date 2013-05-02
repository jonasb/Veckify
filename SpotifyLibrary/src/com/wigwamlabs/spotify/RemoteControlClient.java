package com.wigwamlabs.spotify;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;

class RemoteControlClient extends android.media.RemoteControlClient implements ImageProvider.Callback {
    private final ImageProvider mImageProvider;
    private String mTrackImageLink;

    private RemoteControlClient(PendingIntent mediaButtonIntent, ImageProvider imageProvider) {
        super(mediaButtonIntent);
        mImageProvider = imageProvider;

        setTransportControlFlags(FLAG_KEY_MEDIA_PLAY_PAUSE | FLAG_KEY_MEDIA_STOP | FLAG_KEY_MEDIA_NEXT);
    }

    static RemoteControlClient create(Context context, ComponentName receiver, ImageProvider imageProvider) {
        final Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        intent.setComponent(receiver);
        final PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        return new RemoteControlClient(pendingIntent, imageProvider);
    }

    public void updateMediaData(Track currentTrack) {
        final MetadataEditor editor = editMetadata(true);
        if (currentTrack != null) {
            editor.putString(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST, currentTrack.getArtistsString())
                    .putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, currentTrack.getArtistsString())
                    .putString(MediaMetadataRetriever.METADATA_KEY_TITLE, currentTrack.getName());

            // deal with image
            mTrackImageLink = currentTrack.getImageLink(ImageProvider.SIZE_NORMAL);
            if (mTrackImageLink != null) {
                final Bitmap image = mImageProvider.get(mTrackImageLink);
                if (image != null) {
                    editor.putBitmap(MetadataEditor.BITMAP_KEY_ARTWORK, image);
                } else {
                    mImageProvider.load(mTrackImageLink, this, true);
                }
            }
        }
        editor.apply();
    }

    @Override
    public void onImageImageLoaded(String imageLink, Bitmap image) {
        if (imageLink.equals(mTrackImageLink)) {
            editMetadata(false)
                    .putBitmap(MetadataEditor.BITMAP_KEY_ARTWORK, image)
                    .apply();
        }
    }

    public void onStateChanged(int state) {
        final int playbackState;
        switch (state) {
        case Player.STATE_PLAYING:
            playbackState = RemoteControlClient.PLAYSTATE_PLAYING;
            break;
        case Player.STATE_PAUSED_USER:
        case Player.STATE_PAUSED_AUDIOFOCUS:
        case Player.STATE_PAUSED_NOISY:
            playbackState = RemoteControlClient.PLAYSTATE_PAUSED;
            break;
        default:
        case Player.STATE_STARTED:
        case Player.STATE_STOPPED:
            playbackState = RemoteControlClient.PLAYSTATE_STOPPED;
            break;
        }
        setPlaybackState(playbackState);
    }
}
