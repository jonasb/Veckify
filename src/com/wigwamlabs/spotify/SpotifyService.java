package com.wigwamlabs.spotify;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

public class SpotifyService extends android.app.Service {
    private final IBinder mBinder = new LocalBinder();
    private final Handler mHandler = new Handler();
    private Session mSession;
    private int mClientCount;
    private PlayerNotification mPlayerNotification;

    @Override
    public void onCreate() {
        Debug.logLifecycle("SpotifyService onCreate()");
        super.onCreate();

        mSession = new Session(this, null, null, null);
        mPlayerNotification = new PlayerNotification(this, mSession.getPlayer());
    }

    @Override
    public IBinder onBind(Intent intent) {
        mClientCount++;
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mClientCount--;

        stopSelfIfPossible();

        return true;
    }

    @Override
    public void onRebind(Intent intent) {
        mClientCount++;
    }

    @Override
    public void onDestroy() {
        Debug.logLifecycle("SpotifyService onDestroy()");
        super.onDestroy();

        mPlayerNotification.destroy();
        mPlayerNotification = null;
    }

    private void stopSelfIfPossible() {
        if (mClientCount == 0) {
            Debug.logLifecycle("Should consider stopping service since no clients");
            /* TODO stop service
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mClientCount == 0) {
                        stopSelf();
                    }
                }
            }, 10000);
            */
        }
    }

    public Session getSession() {
        return mSession;
    }

    public void setPlayIntent(PendingIntent intent) {
        mPlayerNotification.setIntent(intent);
    }

    public class LocalBinder extends Binder {
        public SpotifyService getService() {
            return SpotifyService.this;
        }
    }
}
