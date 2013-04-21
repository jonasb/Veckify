package com.wigwamlabs.spotify.ui;

import com.wigwamlabs.spotify.Player;
import com.wigwamlabs.spotify.Session;

public abstract class SpotifyPlayerActivity extends SpotifyActivity implements Player.Callback {
    private Player mPlayer;

    @Override
    protected void onResume() {
        super.onResume();

        if (mPlayer != null) {
            mPlayer.addCallback(this, true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mPlayer != null) {
            mPlayer.removeCallback(this);
        }
    }

    @Override
    protected void onDestroy() {
        if (mPlayer != null) {
            mPlayer.removeCallback(this);
            mPlayer = null;
        }

        super.onDestroy();
    }

    protected Player getPlayer() {
        return mPlayer;
    }

    @Override
    protected void onSpotifySessionAttached(Session spotifySession) {
        mPlayer = spotifySession.getPlayer();
        mPlayer.addCallback(this, true);
    }
}
