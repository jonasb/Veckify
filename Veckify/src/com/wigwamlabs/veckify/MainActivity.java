package com.wigwamlabs.veckify;

import android.content.Intent;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.wigwamlabs.spotify.PendingPlaylistAction;
import com.wigwamlabs.spotify.Player;
import com.wigwamlabs.spotify.Playlist;
import com.wigwamlabs.spotify.PlaylistContainer;
import com.wigwamlabs.spotify.Session;
import com.wigwamlabs.spotify.ui.SpotifyPlayerActivity;
import com.wigwamlabs.veckify.alarms.Alarm;
import com.wigwamlabs.veckify.alarms.AlarmCollection;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.widget.CompoundButton.OnCheckedChangeListener;

public class MainActivity extends SpotifyPlayerActivity {
    private static final int[] REPEAT_DAY_IDS = new int[]{R.id.repeatDayMonday, R.id.repeatDayTuesday, R.id.repeatDayWednesday, R.id.repeatDayThursday, R.id.repeatDayFriday, R.id.repeatDaySaturday, R.id.repeatDaySunday};
    private final Handler mHandler = new Handler();
    private AlarmCollection mAlarmCollection;
    private Alarm mAlarm;
    private PlaylistContainer mPlaylistContainer;
    private Playlist mPlaylist;
    private TextView mAlarmTime;
    private TextView mPlaylistName;
    private Switch mAlarmEnabled;
    private ImageButton mRepeatShuffleToggle;
    private View mRunNowButton;
    private View mNowPlaying;
    private ContentObserver mContentObserver;
    private CheckBox mRepeatCheckBox;
    private ToggleButton[] mRepeatDayToggles;
    private View mRepeatToggles;
    private boolean mUiIsUpdating;
    private View mDownloadPlaylistButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Debug.logLifecycle("MainActivity.onCreate()");
        super.onCreate(savedInstanceState);

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

        mAlarmCollection = new AlarmCollection(this);
        mAlarm = mAlarmCollection.getAlarm();

        if (mAlarm.getVolume() < 0) {
            mAlarm.setVolume(getAudioManager().getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            mAlarmCollection.onAlarmUpdated(mAlarm, false);
        }

        updateUi();

        // detect volume changes
        mContentObserver = new ContentObserver(mHandler) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                onVolumeChanged(getAudioManager().getStreamVolume(AudioManager.STREAM_MUSIC));
            }
        };
        getContentResolver().registerContentObserver(Settings.System.CONTENT_URI, true, mContentObserver);
    }

    @Override
    protected void onPause() {
        Debug.logLifecycle("MainActivity.onPause()");
        super.onPause();

        getContentResolver().unregisterContentObserver(mContentObserver);
        mContentObserver = null;
    }

    @Override
    protected void onDestroy() {
        Debug.logLifecycle("MainActivity.onDestroy()");
        super.onDestroy();

        if (mPlaylist != null) {
            mPlaylist.destroy();
            mPlaylist = null;
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
        case R.id.offlineSync:
            startActivity(new Intent(this, OfflinePlaylistsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initUi() {
        setContentView(R.layout.activity_main);

        // set up time picker
        mAlarmTime = (TextView) findViewById(R.id.alarmTime);
        mAlarmTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onEditTime();
            }
        });
        // set up enable switch
        mAlarmEnabled = (Switch) findViewById(R.id.alarmEnabled);
        mAlarmEnabled.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!mUiIsUpdating) {
                    onAlarmEnabledChanged(isChecked);
                }
            }
        });
        // set up playlist picker
        mPlaylistName = (TextView) findViewById(R.id.playlistName);
        mPlaylistName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPickPlaylist();
            }
        });
        // volume
        final View volume = findViewById(R.id.volume);
        volume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editVolume();
            }
        });
        // repeat/shuffle
        mRepeatShuffleToggle = (ImageButton) findViewById(R.id.repeatShuffleToggle);
        mRepeatShuffleToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRepeatShuffleClicked();
            }
        });
        //
        mRunNowButton = findViewById(R.id.runNowButton);
        mRunNowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runAlarmNow();
            }
        });
        // offline
        mDownloadPlaylistButton = findViewById(R.id.downloadPlaylistButton);
        mDownloadPlaylistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDownloadPlaylist();
            }
        });
        // repeats
        mRepeatCheckBox = (CheckBox) findViewById(R.id.repeatCheckBox);
        mRepeatCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!mUiIsUpdating) {
                    onRepeatChanged(isChecked);
                }
            }
        });
        mRepeatToggles = findViewById(R.id.repeatToggles);
        final OnCheckedChangeListener changeListener = new

                OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (!mUiIsUpdating) {
                            onRepeatDayChanged(buttonView.getId(), isChecked);
                        }
                    }
                };
        mRepeatDayToggles = new ToggleButton[REPEAT_DAY_IDS.length];
        for (int i = 0; i < REPEAT_DAY_IDS.length; i++) {
            mRepeatDayToggles[i] = (ToggleButton) findViewById(REPEAT_DAY_IDS[i]);
            mRepeatDayToggles[i].setOnCheckedChangeListener(changeListener);
        }
        //TODO ensure toggles follow locale's first weekday: Calendar.getFirstDayOfWeek()
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

    private void updateUi() {
        mUiIsUpdating = true;
        //TODO disable switch if not playable
        mAlarmEnabled.setChecked(mAlarm.isEnabled());
        //TODO am/pm
        mAlarmTime.setText(String.format("%d:%02d", mAlarm.getHour(), mAlarm.getMinute()));

        final String name = mAlarm.getPlaylistName();
        if (name == null || name.length() == 0) {
            mPlaylistName.setText(R.string.noPlaylistSelected);
            mRunNowButton.setEnabled(false);
        } else {
            mPlaylistName.setText(name);
            mRunNowButton.setEnabled(true);
        }

        final Session session = getSpotifySession();
        mDownloadPlaylistButton.setVisibility(mPlaylist == null || mPlaylist.getOfflineStatus(session) != Playlist.OFFLINE_STATUS_NO ? GONE : VISIBLE);

        mRepeatShuffleToggle.setImageResource(mAlarm.isShuffle() ? R.drawable.ic_button_shuffle_inverse : R.drawable.ic_button_repeat_inverse);

        final int repeatDays = mAlarm.getRepeatDays();
        mRepeatCheckBox.setChecked(repeatDays != Alarm.DAYS_NONE);
        mRepeatToggles.setVisibility(repeatDays != Alarm.DAYS_NONE ? VISIBLE : GONE);
        int day = 1;
        for (final ToggleButton toggle : mRepeatDayToggles) {
            toggle.setChecked((repeatDays & day) != 0);
            day <<= 1;
        }
        mUiIsUpdating = false;
    }

    private void onEditTime() {
        final TimePickerDialogFragment fragment = new TimePickerDialogFragment();
        fragment.show(getFragmentManager(), "timepicker");
    }

    Alarm getAlarm() {
        return mAlarm;
    }

    public void onAlarmTimeSet(int hour, int minute) {
        mAlarm.setEnabled(true);
        mAlarm.setTime(hour, minute);
        mAlarmCollection.onAlarmUpdated(mAlarm, true);
        updateUi();
    }

    private void onAlarmEnabledChanged(boolean enabled) {
        mAlarm.setEnabled(enabled);
        mAlarmCollection.onAlarmUpdated(mAlarm, true);
    }

    private void onPickPlaylist() {
        final PlaylistPickerFragment fragment = new PlaylistPickerFragment();
        fragment.show(getFragmentManager(), "playlist-picker");
    }

    public void onPlaylistPicked(Playlist playlist) {
        // forget old playlist
        if (mPlaylist != null) {
            mPlaylist.destroy();
            mPlaylist = null;
        }
        //
        if (playlist != null) {
            mAlarm.setEnabled(true);
            mAlarm.setPlaylistLink(playlist.getLink());
            mAlarm.setPlaylistName(playlist.getName());
            mPlaylist = playlist.clone();
        } else {
            mAlarm.setPlaylistLink(null);
            mAlarm.setPlaylistName(null);
        }
        mAlarmCollection.onAlarmUpdated(mAlarm, false);
        updateUi();
    }

    private void editVolume() {
        final AudioManager audioManager = getAudioManager();
        final int volume = mAlarm.getVolume();
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_SAME, AudioManager.FLAG_SHOW_UI);
    }

    private void onVolumeChanged(int volume) {
        if (volume != mAlarm.getVolume()) {
            mAlarm.setVolume(volume);
            mAlarmCollection.onAlarmUpdated(mAlarm, false);
            updateUi();
        }
    }

    private void onDownloadPlaylist() {
        if (mPlaylist == null) {
            return;
        }
        mPlaylist.setOfflineMode(getSpotifySession(), true);
        updateUi();
    }

    private void onRepeatShuffleClicked() {
        mAlarm.setShuffle(!mAlarm.isShuffle());
        mAlarmCollection.onAlarmUpdated(mAlarm, false);
        updateUi();
    }

    private void runAlarmNow() {
        mAlarm.startAlarm(this);
    }

    private void onRepeatChanged(boolean checked) {
        mAlarm.setRepeatDays(checked ? Alarm.DAYS_ALL : Alarm.DAYS_NONE);
        mAlarmCollection.onAlarmUpdated(mAlarm, true);
        updateUi();
    }

    private void onRepeatDayChanged(int id, boolean checked) {
        final int day;
        switch (id) {
        case R.id.repeatDayMonday:
            day = Alarm.DAY_MONDAY;
            break;
        case R.id.repeatDayTuesday:
            day = Alarm.DAY_TUESDAY;
            break;
        case R.id.repeatDayWednesday:
            day = Alarm.DAY_WEDNESDAY;
            break;
        case R.id.repeatDayThursday:
            day = Alarm.DAY_THURSDAY;
            break;
        case R.id.repeatDayFriday:
            day = Alarm.DAY_FRIDAY;
            break;
        case R.id.repeatDaySaturday:
            day = Alarm.DAY_SATURDAY;
            break;
        case R.id.repeatDaySunday:
            day = Alarm.DAY_SUNDAY;
            break;
        default:
            return;
        }
        mAlarm.setRepeatDay(day, checked);
        mAlarmCollection.onAlarmUpdated(mAlarm, true);
        updateUi();
    }

    @Override
    protected void onSpotifySessionAttached(Session spotifySession) {
        super.onSpotifySessionAttached(spotifySession);
        setAutoLogin(true);

        final String link = mAlarm.getPlaylistLink();
        if (link != null) {
            new PendingPlaylistAction(getSpotifySession(), link, false) {
                @Override
                protected void onPlaylistLoaded(Playlist playlist) {
                    if (mPlaylist == null) {
                        onPlaylistPicked(playlist);
                    }
                    playlist.destroy();
                }
            }.start();
        }
    }

    @Override
    public void onConnectionStateUpdated(int state) {
        super.onConnectionStateUpdated(state);

        if (state != Session.CONNECTION_STATE_LOGGED_OUT && mPlaylistContainer == null) {
            mPlaylistContainer = getSpotifySession().getPlaylistContainer();
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

        updateUi();
    }
}
