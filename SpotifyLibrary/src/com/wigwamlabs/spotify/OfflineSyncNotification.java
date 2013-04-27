package com.wigwamlabs.spotify;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

class OfflineSyncNotification implements Session.Callback {
    private final Service mService;
    private final Session mSession;
    private final NotificationManager mNotificationManager;
    private final PendingIntent mPendingIntent;

    OfflineSyncNotification(Service service, Session session) {
        mService = service;
        mSession = session;
        mSession.addCallback(this, false);

        mNotificationManager = (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(R.id.notificationOfflineSync);

        final Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setComponent(new ComponentName(mService, service.getString(R.string.offline_sync_notification_activity)));
        mPendingIntent = PendingIntent.getActivity(mService, 0, intent, 0);
    }

    public void destroy() {
        mSession.removeCallback(this);
        mNotificationManager.cancel(R.id.notificationOfflineSync);
    }

    @Override
    public void onLoggedIn(int error) {
    }

    @Override
    public void onConnectionStateUpdated(int state) {
    }

    @Override
    public void onOfflineTracksToSyncChanged(boolean syncing, int remainingTracks, int approxTotalTracks) {
        if (!syncing || remainingTracks == 0) {
            mNotificationManager.cancel(R.id.notificationOfflineSync);
        } else {
            mNotificationManager.notify(R.id.notificationOfflineSync, getNotification(remainingTracks, approxTotalTracks));
        }
    }

    private Notification getNotification(int remainingTracks, int approxTotalTracks) {
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(mService)
                .setSmallIcon(R.drawable.ic_stat_offline_sync)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(mPendingIntent)
                .setContentTitle(mService.getString(R.string.offline_sync_notification_title))
                .setContentText(String.format(mService.getString(R.string.offline_sync_notification_text), approxTotalTracks - remainingTracks, approxTotalTracks))
                .setProgress(approxTotalTracks, approxTotalTracks - remainingTracks, false)
                .setOngoing(true);
        return builder.build();
    }
}
