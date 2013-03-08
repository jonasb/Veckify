package com.wigwamlabs.spotify.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity implements SpotifySession.Callback {

    private SpotifyContext mSpotifyContext;
    private SpotifySession mSpotifySession;
    private TextView mConnectionState;
    private View mLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLoginButton = findViewById(R.id.login);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                login();
            }
        });

        mConnectionState = (TextView) findViewById(R.id.connectionState);

        mSpotifyContext = new SpotifyContext();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mSpotifySession != null) {
            mSpotifySession.destroy();
            mSpotifySession = null;
        }

        if (mSpotifyContext != null) {
            mSpotifyContext.destroy();
            mSpotifyContext = null;
        }
    }

    private void login() {
        mLoginButton.setEnabled(false);

        if (mSpotifySession != null) {
            mSpotifySession.destroy();
        }
        mSpotifySession = new SpotifySession(this, mSpotifyContext, null, null, null);
        mSpotifySession.setCallback(this);
        if (!mSpotifySession.relogin()) {
            mSpotifySession.login(TempPrivateSettings.username, TempPrivateSettings.password, true);
        }
    }

    public void onConnectionStateUpdated(int state) {
        final int res;
        switch (state) {
        case SpotifySession.CONNECTION_STATE_LOGGED_OUT:
            res = R.string.connectionStateLoggedOut;
            break;
        case SpotifySession.CONNECTION_STATE_LOGGED_IN:
            res = R.string.connectionStateLoggedIn;
            break;
        case SpotifySession.CONNECTION_STATE_DISCONNECTED:
            res = R.string.connectionStateDisconnected;
            break;
        case SpotifySession.CONNECTION_STATE_OFFLINE:
            res = R.string.connectionStateOffline;
            break;
        case SpotifySession.CONNECTION_STATE_UNDEFINED:
        default:
            res = R.string.connectionStateUndefined;
            break;
        }
        mConnectionState.setText(res);

        mLoginButton.setVisibility(state == SpotifySession.CONNECTION_STATE_LOGGED_OUT ? View.VISIBLE : View.GONE);
    }
}
