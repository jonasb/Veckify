package com.wigwamlabs.veckify;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.commonsware.cwac.loaderex.SQLiteCursorLoader;
import com.wigwamlabs.spotify.Player;
import com.wigwamlabs.spotify.PlaylistContainer;
import com.wigwamlabs.spotify.Session;
import com.wigwamlabs.spotify.ui.SpotifyPlayerActivity;
import com.wigwamlabs.veckify.db.AlarmEntry;
import com.wigwamlabs.veckify.db.AlarmsCursor;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class MainActivity extends SpotifyPlayerActivity implements LoaderManager.LoaderCallbacks<Cursor>, AlarmAdapter.Callback {
    private PlaylistContainer mPlaylistContainer;
    private View mNowPlaying;
    private AlarmAdapter mAlarmAdapter;
    private AlarmUtils mAlarmUtils;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Debug.logLifecycle("MainActivity.onCreate()");
        super.onCreate(savedInstanceState);

        mAlarmUtils = new AlarmUtils(this);

        initUi();

        bindSpotifyService();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Debug.logLifecycle("MainActivity.onRestoreInstanceState()");
        // prevent default restoration since we get the new state in onResume()
    }

    @Override
    protected void onResume() {
        Debug.logLifecycle("MainActivity.onResume()");
        super.onResume();

//TODO        updateUi();
    }

    @Override
    protected void onPause() {
        Debug.logLifecycle("MainActivity.onPause()");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Debug.logLifecycle("MainActivity.onDestroy()");
        super.onDestroy();

//TODO        if (mPlaylist != null) {
//            mPlaylist.destroy();
//            mPlaylist = null;
//        }

        if (mPlaylistContainer != null) {
            mPlaylistContainer.destroy();
            mPlaylistContainer = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.offlineSync:
            startActivity(new Intent(this, OfflinePlaylistsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initUi() {
        setContentView(R.layout.activity_main);

        // alarms
        getLoaderManager().initLoader(R.id.loaderAlarms, null, this);

        mAlarmAdapter = new AlarmAdapter(this, this);
        final ListView alarmList = (ListView) findViewById(R.id.alarmList);
        alarmList.setAdapter(mAlarmAdapter);

        // now playing
        mNowPlaying = findViewById(R.id.nowPlaying);
        mNowPlaying.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, NowPlayingActivity.class));
            }
        });
        setTrackArtists((TextView) findViewById(R.id.trackArtists));
        setTrackName((TextView) findViewById(R.id.trackName));
        setTrackProgress((ProgressBar) findViewById(R.id.trackProgress));
        setResumeButton(findViewById(R.id.resumeButton));
        setPauseButton(findViewById(R.id.pauseButton));
        setNextButton(findViewById(R.id.nextButton));
    }

    @Override
    protected void onSpotifySessionAttached(Session spotifySession) {
        super.onSpotifySessionAttached(spotifySession);
        setAutoLogin(true);

        /* TODO
        final String link = mAlarm.getPlaylistLink();
        if (link != null) {
            new PendingPlaylistAction(link, false) {
                @Override
                protected void onPlaylistLoaded(Playlist playlist) {
                    if (mPlaylist == null) {
                        onPlaylistPicked(playlist);
                    }
                    playlist.destroy();
                }
            }.start(getSpotifySession());
        }
        */
    }

    @Override
    public void onConnectionStateUpdated(int state) {
        super.onConnectionStateUpdated(state);

        if (state != Session.CONNECTION_STATE_LOGGED_OUT && mPlaylistContainer == null) {
            mPlaylistContainer = getSpotifySession().getPlaylistContainer();
//TODO            updateUi();
        }
    }

    public PlaylistContainer getPlaylistContainer() {
        return mPlaylistContainer;
    }

    @Override
    public void onStateChanged(int state) {
        super.onStateChanged(state);

        final boolean showNowPlaying = (state == Player.STATE_PLAYING || state == Player.STATE_PAUSED_USER || state == Player.STATE_PAUSED_NOISY || state == Player.STATE_PAUSED_AUDIOFOCUS);
        mNowPlaying.setVisibility(showNowPlaying ? VISIBLE : GONE);
    }

    @Override
    public void onOfflineTracksToSyncChanged(boolean syncing, int remainingTracks, int approxTotalTracks) {
        super.onOfflineTracksToSyncChanged(syncing, remainingTracks, approxTotalTracks);

//TODO        updateUi();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
        case R.id.loaderAlarms:
            return AlarmsCursor.getAllAlarmsLoader(this, ((Application) getApplication()).getDb());
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAlarmUtils.reschedule(this, (AlarmsCursor) data);
        mAlarmAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAlarmAdapter.changeCursor(null);
    }

    private SQLiteCursorLoader getAlarmLoader() {
        final Loader<Cursor> loader = getLoaderManager().getLoader(R.id.loaderAlarms);
        return (SQLiteCursorLoader) loader;
    }

    @Override
    public void onAlarmEntryChanged(long alarmId, AlarmEntry entry) {
        entry.update(getAlarmLoader(), alarmId);
    }

    @Override
    public void onPickTime(long alarmId, AlarmEntry entry) {
        final TimePickerDialogFragment fragment = TimePickerDialogFragment.create(alarmId, entry);
        fragment.show(getFragmentManager(), "timepicker");
    }

    @Override
    public void onPickPlaylist(long alarmId, AlarmEntry entry, String playlistLink) {
        final PlaylistPickerFragment fragment = PlaylistPickerFragment.create(alarmId, entry, playlistLink);
        fragment.show(getFragmentManager(), "playlist-picker");
        //TODO will change playlist...

        // forget old playlist
//TODO        if (mPlaylist != null) {
//            mPlaylist.destroy();
//            mPlaylist = null;
//        }
//        if (playlist != null)
//            mPlaylist = playlist.clone();

    }
}
