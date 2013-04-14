package com.wigwamlabs.spotify;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

public class SpotifyService extends android.app.Service {
    private static final String ACTION_PAUSE = "pause";
    private static final String ACTION_RESUME = "resume";
    private static final String ACTION_NEXT = "next";
    private final IBinder mBinder = new LocalBinder();
    private final Handler mHandler = new Handler();
    private Session mSession;
    private int mClientCount;
    private PlayerNotification mPlayerNotification;
    private Player mPlayer;

    @Override
    public void onCreate() {
        Debug.logLifecycle("SpotifyService onCreate()");
        super.onCreate();

        mSession = new Session(this, null, null, null);
        mPlayer = mSession.getPlayer();

        final Intent pauseIntent = new Intent(this, SpotifyService.class);
        pauseIntent.setAction(ACTION_PAUSE);

        final Intent resumeIntent = new Intent(this, SpotifyService.class);
        resumeIntent.setAction(ACTION_RESUME);

        final Intent nextIntent = new Intent(this, SpotifyService.class);
        nextIntent.setAction(ACTION_NEXT);

        mPlayerNotification = new PlayerNotification(this, mPlayer,
                PendingIntent.getService(this, 0, pauseIntent, 0),
                PendingIntent.getService(this, 0, resumeIntent, 0),
                PendingIntent.getService(this, 0, nextIntent, 0)
        );
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Debug.logLifecycle("onStartCommand() intent=" + intent);
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_PAUSE.equals(action)) {
                mPlayer.pause();
            } else if (ACTION_RESUME.equals(action)) {
                mPlayer.resume();
            } else if (ACTION_NEXT.equals(action)) {
                mPlayer.next();
            } else if ("ALARM".equals(action)) {
                new PendingAction(getSession());
            }
        }

        return super.onStartCommand(intent, flags, startId);
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

        if (mSession != null) {
            mSession.logout();
            mSession.destroy();
            mSession = null;
        }

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
