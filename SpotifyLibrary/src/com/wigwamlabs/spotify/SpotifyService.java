package com.wigwamlabs.spotify;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

public class SpotifyService extends android.app.Service {
    static final String ACTION_NEXT = "next";
    static final String ACTION_PAUSE = "pause";
    public static final String ACTION_PLAY_PLAYLIST = "play_playlist";
    static final String ACTION_RESUME = "resume";
    static final String ACTION_TOGGLE_PAUSE = "toggle_pause";
    public static final String EXTRA_LINK = "link";
    public static final String EXTRA_INTENT = "intent";
    public static final String EXTRA_VOLUME = "volume";
    public static final String EXTRA_SHUFFLE = "shuffle";
    private final IBinder mBinder = new LocalBinder();
    private final Handler mHandler = new Handler();
    private Session mSession;
    private int mClientCount;
    private PlayerNotification mPlayerNotification;
    private OfflineSyncNotification mOfflineSyncNotification;
    private Player mPlayer;
    private RuntimeBroadcastReceiver mRuntimeBroadcastReceiver;

    @Override
    public void onCreate() {
        Debug.logLifecycle("SpotifyService onCreate()");
        super.onCreate();

        mSession = new Session(this, null, null, null);
        mPlayer = mSession.getPlayer();

        //
        mRuntimeBroadcastReceiver = new RuntimeBroadcastReceiver(mPlayer);
        registerReceiver(mRuntimeBroadcastReceiver, mRuntimeBroadcastReceiver.getFilter());

        // init notifications
        final ForegroundNotificationManager foregroundNotificationManager = new ForegroundNotificationManager(this);

        final Intent pauseIntent = new Intent(this, SpotifyService.class);
        pauseIntent.setAction(ACTION_PAUSE);

        final Intent resumeIntent = new Intent(this, SpotifyService.class);
        resumeIntent.setAction(ACTION_RESUME);

        final Intent nextIntent = new Intent(this, SpotifyService.class);
        nextIntent.setAction(ACTION_NEXT);

        mPlayerNotification = new PlayerNotification(this, foregroundNotificationManager, mPlayer,
                PendingIntent.getService(this, 0, pauseIntent, 0),
                PendingIntent.getService(this, 0, resumeIntent, 0),
                PendingIntent.getService(this, 0, nextIntent, 0)
        );

        mOfflineSyncNotification = new OfflineSyncNotification(this, foregroundNotificationManager, mSession);
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
            } else if (ACTION_TOGGLE_PAUSE.equals(action)) {
                mPlayer.togglePause();
            } else if (ACTION_NEXT.equals(action)) {
                mPlayer.next();
            } else if (ACTION_PLAY_PLAYLIST.equals(action)) {
                final String link = intent.getStringExtra(EXTRA_LINK);
                final PendingIntent playIntent = intent.getParcelableExtra(EXTRA_INTENT);
                final int volume = intent.getIntExtra(EXTRA_VOLUME, -1);
                final boolean shuffle = intent.getBooleanExtra(EXTRA_SHUFFLE, false);
                new PendingPlayPlaylistAction(this, getSession(), link, playIntent, volume, shuffle).start();
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

        if (mRuntimeBroadcastReceiver != null) {
            unregisterReceiver(mRuntimeBroadcastReceiver);
            mRuntimeBroadcastReceiver = null;
        }

        if (mSession != null) {
            mSession.logout();
            mSession.destroy();
            mSession = null;
        }

        mPlayerNotification.destroy();
        mPlayerNotification = null;

        mOfflineSyncNotification.destroy();
        mOfflineSyncNotification = null;
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
