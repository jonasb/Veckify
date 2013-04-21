package com.wigwamlabs.spotify.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;

import com.wigwamlabs.spotify.Session;
import com.wigwamlabs.spotify.SpotifyService;

public abstract class SpotifyActivity extends Activity implements Session.Callback, ServiceConnection {
    private static final String FRAGMENT_LOGIN_DIALOG = "LoginDialog";
    private SpotifyService mService;
    private Session mSpotifySession;
    private LoginDialogFragment mLoginDialog;
    private boolean mAutoLogin;

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

    protected SpotifyService getSpotifyService() {
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        mLoginDialog = (LoginDialogFragment) getFragmentManager().findFragmentByTag(FRAGMENT_LOGIN_DIALOG);
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

    protected void setAutoLogin(@SuppressWarnings("SameParameterValue") boolean autoLogin) {
        mAutoLogin = autoLogin;
    }

    @Override
    public void onLoggedIn(int error) {
        if (mLoginDialog != null) {
            mLoginDialog.onLoggedIn(error);
        }
    }

    @Override
    public void onConnectionStateUpdated(int state) {
        boolean showLogin = false;
        switch (state) {
        case Session.CONNECTION_STATE_LOGGED_OUT:
        case Session.CONNECTION_STATE_UNDEFINED:
            showLogin = true;
            if (mAutoLogin) {
                mAutoLogin = false;
                if (mSpotifySession.relogin()) {
                    showLogin = false;
                }
            }
            break;
        case Session.CONNECTION_STATE_DISCONNECTED:
        case Session.CONNECTION_STATE_LOGGED_IN:
        case Session.CONNECTION_STATE_OFFLINE:
            showLogin = false;
            break;
        }

        if (showLogin && mLoginDialog == null) {
            mLoginDialog = new LoginDialogFragment();
            mLoginDialog.show(getFragmentManager(), FRAGMENT_LOGIN_DIALOG);
        }
        if (!showLogin && mLoginDialog != null) {
            mLoginDialog.dismissAllowingStateLoss();
            mLoginDialog = null;
        }
    }
}
