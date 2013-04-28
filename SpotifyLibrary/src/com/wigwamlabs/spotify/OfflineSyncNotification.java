package com.wigwamlabs.spotify;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

class OfflineSyncNotification extends ForegroundNotification implements Session.Callback {
    private final Service mService;
    private final Session mSession;
    private final PendingIntent mPendingIntent;
    private int mRemainingTracks;
    private int mApproxTotalTracks;

    OfflineSyncNotification(Service service, ForegroundNotificationManager manager, Session session) {
        super(manager);
        mService = service;
        mSession = session;
        mSession.addCallback(this, false);

        final Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setComponent(new ComponentName(mService, service.getString(R.string.offline_sync_notification_activity)));
        mPendingIntent = PendingIntent.getActivity(mService, 0, intent, 0);
    }

    @Override
    int getNotificationId() {
        return R.id.notificationOfflineSync;
    }

    @Override
    void destroy() {
        super.destroy();
        mSession.removeCallback(this);
    }

    @Override
    public void onLoggedIn(int error) {
    }

    @Override
    public void onConnectionStateUpdated(int state) {
    }

    @Override
    public void onOfflineTracksToSyncChanged(boolean syncing, int remainingTracks, int approxTotalTracks) {
        mRemainingTracks = remainingTracks;
        mApproxTotalTracks = approxTotalTracks;

        final boolean shouldBeForeground = (syncing && remainingTracks > 0);
        if (shouldBeForeground != isForeground()) {
            if (!setForeground(shouldBeForeground)) {
                onNotificationUpdated();
            }
        }
        onNotificationUpdated();
    }

    @Override
    Notification getNotification() {
        if (!isForeground()) {
            return null;
        }
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(mService)
                .setSmallIcon(R.drawable.ic_stat_offline_sync)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(mPendingIntent)
                .setContentTitle(mService.getString(R.string.offline_sync_notification_title))
                .setContentText(String.format(mService.getString(R.string.offline_sync_notification_text), mApproxTotalTracks - mRemainingTracks, mApproxTotalTracks))
                .setProgress(mApproxTotalTracks, mApproxTotalTracks - mRemainingTracks, false)
                .setOngoing(true);
        return builder.build();
    }
}
