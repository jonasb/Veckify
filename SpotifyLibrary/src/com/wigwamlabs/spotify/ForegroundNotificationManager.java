package com.wigwamlabs.spotify;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;

import java.util.ArrayList;

class ForegroundNotificationManager {
    private final NotificationManager mNotificationManager;
    private final Service mService;
    private final ArrayList<ForegroundNotification> mNotifications = new ArrayList<ForegroundNotification>();
    private ForegroundNotification mCurrentForeground;

    ForegroundNotificationManager(Service service) {
        mService = service;
        mNotificationManager = (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    void register(ForegroundNotification notification) {
        Debug.logForegroundNotification("registering: " + notification);
        mNotifications.add(notification);
        mNotificationManager.cancel(notification.getNotificationId());
    }

    void unregister(ForegroundNotification notification) {
        Debug.logForegroundNotification("unregistering: " + notification);
        mNotifications.remove(notification);
        mNotificationManager.cancel(notification.getNotificationId());
    }

    boolean onForegroundChanged(ForegroundNotification notification) {
        if (mCurrentForeground == null && notification.isForeground()) {
            Debug.logForegroundNotification("got new foreground notification: " + notification);
            mCurrentForeground = notification;
            mService.startForeground(notification.getNotificationId(), notification.getNotification());
            return true;
        }
        if (mCurrentForeground != null && mCurrentForeground != notification) {
            Debug.logForegroundNotification("got another foreground notification when another is active: " + notification);
            return false;
        }
        if (mCurrentForeground == notification && !notification.isForeground()) {
            Debug.logForegroundNotification("current foreground moved into background: " + notification);
            ForegroundNotification nextForeground = null;
            for (ForegroundNotification n : mNotifications) {
                if (n != notification && n.isForeground()) {
                    nextForeground = n;
                    break;
                }
            }
            if (nextForeground == null) {
                Debug.logForegroundNotification("no other foreground to replace it, so just remove it");
                mCurrentForeground = null;
                // this will cause flicker since the notification will be removed before readded,
                // however without it the notification is sticky and can't be removed by the user
                mService.stopForeground(true);
                final Notification n = notification.getNotification();
                if (n != null) {
                    mNotificationManager.notify(notification.getNotificationId(), n);
                }
            } else {
                Debug.logForegroundNotification("move foreground to: " + nextForeground);
                mCurrentForeground = nextForeground;
                mService.startForeground(mCurrentForeground.getNotificationId(), mCurrentForeground.getNotification());
                final Notification n = notification.getNotification();
                if (n == null) {
                    mNotificationManager.cancel(notification.getNotificationId());
                } else {
                    mNotificationManager.notify(notification.getNotificationId(), n);
                }
            }
            return false;
        }
        Debug.logForegroundNotification("STATE NOT HANDLED");
        return false;
    }

    void onNotificationUpdated(ForegroundNotification notification) {
        final Notification n = notification.getNotification();
        if (n == null) {
            mNotificationManager.cancel(notification.getNotificationId());
        } else {
            mNotificationManager.notify(notification.getNotificationId(), n);
        }
    }
}
