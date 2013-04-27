package com.wigwamlabs.veckify;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ListView;

import com.wigwamlabs.spotify.PlaylistContainer;
import com.wigwamlabs.spotify.Session;
import com.wigwamlabs.spotify.ui.ConfigureOfflinePlaylistContainerAdapter;
import com.wigwamlabs.spotify.ui.SpotifyActivity;

public class OfflinePlaylistsActivity extends SpotifyActivity {
    private ListView mList;
    private PlaylistContainer mPlaylistContainer;
    private ConfigureOfflinePlaylistContainerAdapter mListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Debug.logLifecycle("OfflinePlaylistsActivity.onCreate()");
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_PROGRESS);

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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            startActivity(getParentActivityIntent());
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
            mListAdapter = new ConfigureOfflinePlaylistContainerAdapter(this, getSpotifySession(), mPlaylistContainer);
            mList.setAdapter(mListAdapter);
        }
    }

    @Override
    public void onOfflineTracksToSyncChanged(boolean syncing, int remainingTracks, int approxTotalTracks) {
        super.onOfflineTracksToSyncChanged(syncing, remainingTracks, approxTotalTracks);

        if (!syncing || remainingTracks == 0) {
            setProgressBarVisibility(false);
        } else {
            setProgressBarVisibility(true);
            int progress = (10000 * (approxTotalTracks - remainingTracks)) / approxTotalTracks;
            if (progress == 0) {
                progress = 1;
            }
            setProgress(progress);
        }

        if (mListAdapter != null) {
            mListAdapter.refresh();
        }
    }
}
