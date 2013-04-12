package com.wigwamlabs.spotify.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.wigwamlabs.spotify.Session;
import com.wigwamlabs.spotify.SpotifyService;

public abstract class SpotifyActivity extends Activity implements Session.Callback, ServiceConnection {
    private SpotifyService mService;
    private Session mSpotifySession;

    protected void bindSpotifyService() {
        final Intent intent = new Intent(this, SpotifyService.class);
        startService(intent);
        bindService(intent, this, BIND_AUTO_CREATE);
    }

    @Override
    public void onServiceConnected(ComponentName className, IBinder service) {
        if (service instanceof SpotifyService.LocalBinder) {
            final SpotifyService.LocalBinder binder = (SpotifyService.LocalBinder) service;
            mService = binder.getService();

            mSpotifySession = mService.getSession();
            onSpotifySessionAttached(mSpotifySession);

            mSpotifySession.addCallback(this, true);
        }
    }

    protected abstract void onSpotifySessionAttached(Session spotifySession);

    public SpotifyService getSpotifyService() {
        return mService;
    }

    protected Session getSpotifySession() {
        return mSpotifySession;
    }

    @Override
    public void onServiceDisconnected(ComponentName arg0) {
        mService = null;
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mSpotifySession != null) {
            mSpotifySession.removeCallback(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mSpotifySession != null) {
            mSpotifySession.addCallback(this, true);
        }
    }

    @Override
    protected void onDestroy() {
        if (mSpotifySession != null) {
            mSpotifySession.removeCallback(this);
            mSpotifySession = null;
        }

        unbindService(this);
        mService = null;

        super.onDestroy();
    }
}
