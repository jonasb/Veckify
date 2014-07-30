package com.wigwamlabs.spotify;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.graphics.Bitmap;
import android.support.v4.app.NotificationCompat;

class PlayerNotification extends ForegroundNotification implements Player.Callback, ImageProvider.Callback {
    private final Service mService;
    private final Player mPlayer;
    private final ImageProvider mImageProvider;
    private final PendingIntent mPauseIntent;
    private final PendingIntent mResumeIntent;
    private final PendingIntent mNextIntent;
    private PendingIntent mIntent;
    private String mArtists;
    private String mTrackName;
    private String mTrackImageLink;
    private Bitmap mTrackImage;
    private int mState;

    PlayerNotification(Service service, ForegroundNotificationManager manager, Session session, PendingIntent pauseIntent, PendingIntent resumeIntent, PendingIntent nextIntent) {
        super(manager);
        mService = service;
        mPlayer = session.getPlayer();
        mImageProvider = session.getImageProvider();
        mPauseIntent = pauseIntent;
        mResumeIntent = resumeIntent;
        mNextIntent = nextIntent;
        mPlayer.addCallback(this, false);
    }

    @Override
    void destroy() {
        super.destroy();
        mPlayer.removeCallback(this);
    }

    @Override
    int getNotificationId() {
        return R.id.notificationPlayer;
    }

    void setIntent(PendingIntent intent) {
        mIntent = intent;
    }

    @Override
    Notification getNotification() {
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(mService)
                .setSmallIcon(R.drawable.ic_stat_player)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(isForeground())
                .setContentTitle(mArtists)
                .setContentText(mTrackName)
                .setContentIntent(mIntent);

        resetTrackImageIfRecycled();
        if (mTrackImage != null) {
            builder.setLargeIcon(mTrackImage);
        }

        switch (mState) {
        case Player.STATE_PLAYING:
            builder.addAction(R.drawable.ic_notification_action_pause, mService.getString(R.string.notification_action_pause), mPauseIntent);
            builder.addAction(R.drawable.ic_notification_action_next, mService.getString(R.string.notification_action_next), mNextIntent);
            break;
        case Player.STATE_PAUSED_USER:
        case Player.STATE_PAUSED_AUDIOFOCUS:
        case Player.STATE_PAUSED_NOISY:
            builder.addAction(R.drawable.ic_notification_action_resume, mService.getString(R.string.notification_action_resume), mResumeIntent);
            builder.addAction(R.drawable.ic_notification_action_next, mService.getString(R.string.notification_action_next), mNextIntent);
            break;
        }

        return builder.build();
    }

    @Override
    public void onCurrentTrackUpdated(Track track) {
        mArtists = track.getArtistsString();
        mTrackName = track.getName();
        mTrackImageLink = track.getImageLink(ImageProvider.SIZE_NORMAL);
        if (mTrackImageLink == null) {
            mTrackImage = null;
        } else {
            mTrackImage = mImageProvider.get(mTrackImageLink);
            resetTrackImageIfRecycled();
            if (mTrackImage == null) {
                mImageProvider.load(mTrackImageLink, this, true);
            }
        }

        onNotificationUpdated();
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
        case Player.STATE_PAUSED_AUDIOFOCUS:
        case Player.STATE_PAUSED_NOISY:
        case Player.STATE_STOPPED:
            setForeground(false);
            break;
        }
    }

    @Override
    public void onTrackProgress(int secondsPlayed, int secondsDuration) {
    }

    @Override
    public void onImageImageLoaded(String imageLink, Bitmap image) {
        if (imageLink.equals(mTrackImageLink)) {
            mTrackImage = image;
            onNotificationUpdated();
        }
    }

    private void resetTrackImageIfRecycled() {
        if (mTrackImage != null && mTrackImage.isRecycled()) {
            mTrackImage = null;
        }
    }
}
