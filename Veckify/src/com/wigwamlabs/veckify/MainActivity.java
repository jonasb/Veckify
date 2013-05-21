package com.wigwamlabs.veckify;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.commonsware.cwac.loaderex.SQLiteCursorLoader;
import com.example.android.undobar.UndoBarController;
import com.wigwamlabs.spotify.ImageProvider;
import com.wigwamlabs.spotify.Player;
import com.wigwamlabs.spotify.Playlist;
import com.wigwamlabs.spotify.PlaylistContainer;
import com.wigwamlabs.spotify.PlaylistProvider;
import com.wigwamlabs.spotify.Session;
import com.wigwamlabs.spotify.ui.SpotifyImageView;
import com.wigwamlabs.spotify.ui.SpotifyPlayerActivity;
import com.wigwamlabs.veckify.db.AlarmEntry;
import com.wigwamlabs.veckify.db.AlarmsCursor;

import java.util.Calendar;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class MainActivity extends SpotifyPlayerActivity implements LoaderManager.LoaderCallbacks<Cursor>, AlarmAdapter.Callback, UndoBarController.UndoListener, TimeUpdater.Callback {
    private final Handler mHandler = new Handler();
    private PlaylistContainer mPlaylistContainer;
    private AlarmUtils mAlarmUtils;
    private SwipeDismissListView mAlarmList;
    private AlarmAdapter mAlarmAdapter;
    private View mNowPlaying;
    private Integer mScrollToPositionOnLoad;
    private Long mScrollToIdOnLoad;
    private UndoBarController mUndoBarController;
    private TimeUpdater mTimeUpdater;
    private PlaylistProvider mPlaylistProvider;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Debug.logLifecycle("MainActivity.onCreate()");
        super.onCreate(savedInstanceState);

        mAlarmUtils = new AlarmUtils(this);

        initUi();

        mTimeUpdater = new TimeUpdater(mHandler, this);

        bindSpotifyService();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Debug.logLifecycle("MainActivity.onSaveInstanceState()");
        super.onSaveInstanceState(outState);
        mUndoBarController.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Debug.logLifecycle("MainActivity.onRestoreInstanceState()");
        super.onRestoreInstanceState(savedInstanceState);
        mUndoBarController.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        Debug.logLifecycle("MainActivity.onResume()");
        super.onResume();

        mTimeUpdater.start();
    }

    @Override
    protected void onPause() {
        Debug.logLifecycle("MainActivity.onPause()");
        super.onPause();

        mTimeUpdater.stop();
    }

    @Override
    protected void onDestroy() {
        Debug.logLifecycle("MainActivity.onDestroy()");
        super.onDestroy();

        if (mPlaylistProvider != null) {
            mPlaylistProvider.destroy();
            mPlaylistProvider = null;
        }

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
        case R.id.addAlarm:
            onAddAlarm();
            return true;
        case R.id.offlineSync:
            startActivity(new Intent(this, OfflinePlaylistsActivity.class));
            return true;
        case R.id.settings:
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initUi() {
        setContentView(R.layout.activity_main);

        // alarms
        getLoaderManager().initLoader(R.id.loaderAlarms, null, this);

        mAlarmAdapter = new AlarmAdapter(this, this);
        mAlarmAdapter.setEnablePlaylistPickers(mPlaylistContainer != null);
        mAlarmList = (SwipeDismissListView) findViewById(R.id.alarmList);
        mAlarmList.setAdapter(mAlarmAdapter);
        mAlarmList.setCallback(new SwipeDismissListView.Callback() {
            @Override
            public boolean canDismiss(int position) {
                return true;
            }

            @Override
            public void onDismiss(SwipeDismissListView swipeDismissListView, int[] reverseSortedPositions) {
                final long[] ids = new long[reverseSortedPositions.length];
                for (int i = 0; i < ids.length; i++) {
                    ids[i] = mAlarmAdapter.getItemId(reverseSortedPositions[i]);
                }
                onDeleteAlarms(ids);
            }
        });

        // undo
        mUndoBarController = new UndoBarController(findViewById(R.id.undobar), this);

        // now playing
        mNowPlaying = findViewById(R.id.nowPlaying);
        mNowPlaying.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, NowPlayingActivity.class));
            }
        });
        setTrackImage((SpotifyImageView) findViewById(R.id.trackImage), ImageProvider.SIZE_NORMAL);
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

        mPlaylistProvider = new PlaylistProvider(spotifySession);
        mAlarmAdapter.setPlaylistProvider(mPlaylistProvider);
    }

    @Override
    public void onConnectionStateUpdated(int state) {
        super.onConnectionStateUpdated(state);

        if (state != Session.CONNECTION_STATE_LOGGED_OUT && mPlaylistContainer == null) {
            mPlaylistContainer = getSpotifySession().getPlaylistContainer();
            mAlarmAdapter.setEnablePlaylistPickers(true);
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
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
        case R.id.loaderAlarms:
            return AlarmsCursor.getAllAlarmsLoader(this, ((Application) getApplication()).getDb());
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        final AlarmsCursor alarm = (AlarmsCursor) data;
        mAlarmUtils.reschedule(this, alarm);
        mAlarmAdapter.changeCursor(alarm);

        if (mScrollToPositionOnLoad != null) {
            mAlarmList.smoothScrollToPosition(mScrollToPositionOnLoad.intValue());
            mScrollToPositionOnLoad = null;
        }
        if (mScrollToIdOnLoad != null) {
            final long id = mScrollToIdOnLoad.longValue();
            int pos = 0;
            for (boolean hasItem = alarm.moveToFirst(); hasItem; hasItem = alarm.moveToNext()) {
                if (id == alarm._id()) {
                    mAlarmList.smoothScrollToPosition(pos);
                    break;
                }
                pos++;
            }
            mScrollToIdOnLoad = null;
        }
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
    public void onTimeUpdated(Calendar cal) {
        // time to alarm is updated
        mAlarmAdapter.notifyDataSetChanged();
    }

    private void onAddAlarm() {
        final AlarmEntry entry = AlarmEntry.createNew();
        entry.insert(getAlarmLoader());

        mScrollToPositionOnLoad = mAlarmAdapter.getCount();
    }

    private void onDeleteAlarms(long[] ids) {
        final AlarmEntry entry = new AlarmEntry();
        entry.setDeleted(true);
        entry.update(getAlarmLoader(), ids);
        mAlarmAdapter.setItemsDeleted(ids);

        UndoAction undoAction = (UndoAction) mUndoBarController.getUndoToken();
        if (undoAction == null) {
            undoAction = new UndoAction(ids);
        } else {
            undoAction.add(ids);
        }
        mUndoBarController.showUndoBar(false, getString(R.string.alarm_deleted, undoAction.ids.length), undoAction);
    }

    @Override
    public void onUndo(Parcelable token) {
        if (token == null) {
            return;
        }
        final UndoAction action = (UndoAction) token;
        final long[] ids = action.ids;

        final AlarmEntry entry = new AlarmEntry();
        entry.setDeleted(false);
        entry.update(getAlarmLoader(), ids);

        mAlarmAdapter.setItemsUndeleted(ids);
        mScrollToIdOnLoad = ids[0];
    }

    @Override
    public void onAlarmEntryChanged(long alarmId, AlarmEntry entry, boolean enableIfPossible) {
        if (enableIfPossible) {
            if (entry.hasPlaylist() && entry.getTime() != null) {
                entry.setEnabled(true);
            }
        }
        entry.update(getAlarmLoader(), alarmId);
    }

    @Override
    public void onPickTime(long alarmId, AlarmEntry entry) {
        final TimePickerDialogFragment fragment = TimePickerDialogFragment.create(alarmId, entry);
        fragment.show(getFragmentManager(), "time-picker");
    }

    @Override
    public void onPickRepeatDays(long alarmId, AlarmEntry entry) {
        final RepeatDaysPickerFragment fragment = RepeatDaysPickerFragment.create(alarmId, entry);
        fragment.show(getFragmentManager(), "repeatdays-picker");
    }

    @Override
    public void onPickPlaylist(long alarmId, AlarmEntry entry, String playlistLink) {
        final PlaylistPickerFragment fragment = PlaylistPickerFragment.create(alarmId, entry, playlistLink);
        fragment.show(getFragmentManager(), "playlist-picker");
    }

    @Override
    public void onPickVolume(long alarmId, AlarmEntry entry) {
        final VolumePickerFragment fragment = VolumePickerFragment.create(alarmId, entry);
        fragment.show(getFragmentManager(), "volume-picker");
    }

    @Override
    public void onSetTts(long alarmId, AlarmEntry entry) {
        final TtsSettingsFragment fragment = TtsSettingsFragment.create(alarmId, entry);
        fragment.show(getFragmentManager(), "tts-settings");
    }

    @Override
    public boolean onDownloadPlaylist(String playlistLink) {
        final Playlist playlist = mPlaylistProvider.getPlaylist(playlistLink);
        if (playlist != null) {
            playlist.setOfflineMode(getSpotifySession(), true);
            return true;
        }
        return false;
    }

    private static class UndoAction implements Parcelable {
        public static final Parcelable.Creator CREATOR =
                new Parcelable.Creator() {
                    @Override
                    public UndoAction createFromParcel(Parcel in) {
                        return new UndoAction(in);
                    }

                    @Override
                    public UndoAction[] newArray(int size) {
                        return new UndoAction[size];
                    }
                };
        long[] ids;

        public UndoAction(long[] ids) {
            this.ids = ids;
        }

        public UndoAction(Parcel in) {
            ids = new long[in.readInt()];
            in.readLongArray(ids);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(ids.length);
            dest.writeLongArray(ids);
        }

        public void add(long[] newIds) {
            final long[] oldIds = ids;
            ids = new long[oldIds.length + newIds.length];
            System.arraycopy(oldIds, 0, ids, 0, oldIds.length);
            System.arraycopy(newIds, 0, ids, oldIds.length + 0, newIds.length);
        }
    }
}
