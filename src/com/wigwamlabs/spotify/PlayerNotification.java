package com.wigwamlabs.spotify;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

import com.wigwamlabs.spotify.app.R;

class PlayerNotification implements Player.Callback {
    private final Service mService;
    private final Player mPlayer;
    private final PendingIntent mPauseIntent;
    private final PendingIntent mResumeIntent;
    private final NotificationManager mNotificationManager;
    private PendingIntent mIntent;
    private boolean mForeground = false;
    private String mArtists;
    private String mTrackName;
    private int mState;

    PlayerNotification(Service service, Player player, PendingIntent pauseIntent, PendingIntent resumeIntent) {
        mService = service;
        mPlayer = player;
        mPauseIntent = pauseIntent;
        mResumeIntent = resumeIntent;
        mPlayer.addCallback(this, false);

        mNotificationManager = (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);
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

    private Notification getNotification() {
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(mService)
                .setSmallIcon(R.drawable.ic_stat_player)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentTitle(mArtists)
                .setContentText(mTrackName)
                .setContentIntent(mIntent);

        switch (mState) {
        case Player.STATE_PLAYING:
            builder.addAction(R.drawable.ic_notification_action_pause, mService.getString(R.string.notification_action_pause), mPauseIntent);
            break;
        case Player.STATE_PAUSED_USER:
            builder.addAction(R.drawable.ic_notification_action_resume, mService.getString(R.string.notification_action_resume), mResumeIntent);
            break;
        }

        return builder.build();
    }

    @Override
    public void onCurrentTrackUpdated(Track track) {
        mArtists = track.getArtistsString();
        mTrackName = track.getName();

        mNotificationManager.notify(R.id.notificationPlayer, getNotification());
    }

    @Override
    public void onStateChanged(int state) {
        mState = state;
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
                mService.startForeground(R.id.notificationPlayer, getNotification());
            } else {
                // this will cause flicker since the notification will be removed before readded,
                // however without it the notification is sticky and can't be removed by the user
                mService.stopForeground(true);
                mNotificationManager.notify(R.id.notificationPlayer, getNotification());
            }
        }
    }

    @Override
    public void onTrackProgress(int secondsPlayed, int secondsDuration) {
    }
}
