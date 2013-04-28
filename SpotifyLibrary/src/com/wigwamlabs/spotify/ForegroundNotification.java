package com.wigwamlabs.spotify;

import android.app.Notification;

abstract class ForegroundNotification {
    private final ForegroundNotificationManager mManager;
    private boolean mForeground;

    ForegroundNotification(ForegroundNotificationManager manager) {
        mManager = manager;

        mManager.register(this);
    }

    abstract int getNotificationId();

    void destroy() {
        setForeground(false);
        mManager.unregister(this);
    }

    boolean isForeground() {
        return mForeground;
    }

    boolean setForeground(boolean foreground) {
        mForeground = foreground;
        return mManager.onForegroundChanged(this);
    }

    abstract Notification getNotification();

    void onNotificationUpdated() {
        mManager.onNotificationUpdated(this);
    }
}
