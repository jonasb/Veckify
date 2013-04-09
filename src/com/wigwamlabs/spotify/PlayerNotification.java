package com.wigwamlabs.spotify;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

import com.wigwamlabs.spotify.app.R;

class PlayerNotification implements Player.Callback {
    private final Service mService;
    private final Player mPlayer;
    private final NotificationManager mNotificationManager;
    private final NotificationCompat.Builder mBuilder;
    private PendingIntent mIntent;
    private boolean mForeground = false;

    PlayerNotification(Service service, Player player, PendingIntent intent) {
        mService = service;
        mPlayer = player;
        mPlayer.addCallback(this, false);

        mNotificationManager = (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(service)
                .setSmallIcon(R.drawable.ic_stat_player)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        mBuilder.addAction(0, service.getString(R.string.notification_action_pause), intent);
    }

    void destroy() {
        mPlayer.removeCallback(this);
        mNotificationManager.cancel(R.id.notificationPlayer);
        if (mForeground) {
            mService.stopForeground(true);
        }
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
    public void onStateChanged(int state) {
        switch (state) {
        case Player.STATE_PLAYING:
            setForeground(true);
            break;
        case Player.STATE_STARTED:
        case Player.STATE_PAUSED_USER:
        case Player.STATE_STOPPED:
            setForeground(false);
            break;
        }
    }

    private void setForeground(boolean foreground) {
        if (foreground != mForeground) {
            mForeground = foreground;

            if (foreground) {
                mService.startForeground(R.id.notificationPlayer, mBuilder.build());
            } else {
                // this will cause flicker since the notification will be removed before readded,
                // however without it the notification is sticky and can't be removed by the user
                mService.stopForeground(true);
                mNotificationManager.notify(R.id.notificationPlayer, mBuilder.build());
            }
        }
    }

    @Override
    public void onTrackProgress(int secondsPlayed, int secondsDuration) {
    }
}
