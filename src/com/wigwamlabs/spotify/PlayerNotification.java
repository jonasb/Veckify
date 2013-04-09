package com.wigwamlabs.spotify;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

import com.wigwamlabs.spotify.app.R;

class PlayerNotification implements Player.Callback {
    private final Player mPlayer;
    private final NotificationManager mNotificationManager;
    private final NotificationCompat.Builder mBuilder;
    private PendingIntent mIntent;

    PlayerNotification(Context context, Player player) {
        mPlayer = player;
        mPlayer.addCallback(this, false);

        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_stat_player)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);
    }

    void destroy() {
        mPlayer.removeCallback(this);
        mNotificationManager.cancel(R.id.notificationPlayer);
    }

    void setIntent(PendingIntent intent) {
        mIntent = intent;
    }

    @Override
    public void onCurrentTrackUpdated(Track track) {
        mBuilder.setContentTitle(track.getArtistsString())
                .setContentText(track.getName())
                .setContentIntent(mIntent);

        mNotificationManager.notify(R.id.notificationPlayer, mBuilder.build());
    }

    @Override
    public void onTrackProgress(int secondsPlayed, int secondsDuration) {
    }
}
