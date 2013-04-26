package com.wigwamlabs.veckify;

import android.os.Bundle;
import android.widget.ListView;

import com.wigwamlabs.spotify.PlaylistContainer;
import com.wigwamlabs.spotify.Session;
import com.wigwamlabs.spotify.ui.ConfigureOfflinePlaylistContainerAdapter;
import com.wigwamlabs.spotify.ui.SpotifyActivity;

public class OfflinePlaylistsActivity extends SpotifyActivity {
    private ListView mList;
    private PlaylistContainer mPlaylistContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Debug.logLifecycle("OfflinePlaylistsActivity.onCreate()");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_offline_playlists);
        mList = (ListView) findViewById(R.id.list);

        bindSpotifyService();
    }

    @Override
    protected void onDestroy() {
        Debug.logLifecycle("OfflinePlaylistsActivity.onDestroy()");
        if (mPlaylistContainer != null) {
            mPlaylistContainer.destroy();
            mPlaylistContainer = null;
        }

        super.onDestroy();
    }

    @Override
    protected void onSpotifySessionAttached(Session spotifySession) {
        super.onSpotifySessionAttached(spotifySession);
        setAutoLogin(true);
    }

    @Override
    public void onConnectionStateUpdated(int state) {
        super.onConnectionStateUpdated(state);

        if (state != Session.CONNECTION_STATE_LOGGED_OUT && mPlaylistContainer == null) {
            mPlaylistContainer = getSpotifySession().getPlaylistContainer();
            mList.setAdapter(new ConfigureOfflinePlaylistContainerAdapter(this, getSpotifySession(), mPlaylistContainer));
        }
    }
}
