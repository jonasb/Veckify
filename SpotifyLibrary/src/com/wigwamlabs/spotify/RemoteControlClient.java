package com.wigwamlabs.spotify;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;

class RemoteControlClient extends android.media.RemoteControlClient {

    private RemoteControlClient(PendingIntent mediaButtonIntent) {
        super(mediaButtonIntent);

        setTransportControlFlags(FLAG_KEY_MEDIA_PLAY_PAUSE | FLAG_KEY_MEDIA_STOP | FLAG_KEY_MEDIA_NEXT);
    }

    static RemoteControlClient create(Context context, ComponentName receiver) {
        final Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        intent.setComponent(receiver);
        final PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        return new RemoteControlClient(pendingIntent);
    }

    public void updateMediaData(Track currentTrack) {
        final MetadataEditor editor = editMetadata(true);
        if (currentTrack != null) {
            editor.putString(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST, currentTrack.getArtistsString())
                    .putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, currentTrack.getArtistsString())
                    .putString(MediaMetadataRetriever.METADATA_KEY_TITLE, currentTrack.getName());
        }
        editor.apply();
    }

    public void onStateChanged(int state) {
        final int playbackState;
        switch (state) {
        case Player.STATE_PLAYING:
            playbackState = RemoteControlClient.PLAYSTATE_PLAYING;
            break;
        case Player.STATE_PAUSED_USER:
        case Player.STATE_PAUSED_AUDIOFOCUS:
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
