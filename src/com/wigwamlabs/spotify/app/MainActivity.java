package com.wigwamlabs.spotify.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {

    private SpotifyContext mSpotifyContext;
    private SpotifySession mSpotifySession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSpotifyContext = new SpotifyContext();

        final View login = findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                login();
            }
        });
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
        if (mSpotifySession != null) {
            mSpotifySession.destroy();
        }
        mSpotifySession = new SpotifySession(this, mSpotifyContext, null, null, null);
        if (!mSpotifySession.relogin()) {
            mSpotifySession.login(TempPrivateSettings.username, TempPrivateSettings.password, true);
        }
    }

}
