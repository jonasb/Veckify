package com.wigwamlabs.veckify;

import android.content.Intent;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import com.wigwamlabs.spotify.Player;
import com.wigwamlabs.spotify.Playlist;
import com.wigwamlabs.spotify.PlaylistContainer;
import com.wigwamlabs.spotify.Session;
import com.wigwamlabs.spotify.ui.SpotifyPlayerActivity;
import com.wigwamlabs.veckify.alarms.Alarm;
import com.wigwamlabs.veckify.alarms.AlarmCollection;

public class MainActivity extends SpotifyPlayerActivity {
    private final Handler mHandler = new Handler();
    private AlarmCollection mAlarmCollection;
    private Alarm mAlarm;
    private PlaylistContainer mPlaylistContainer;
    private TextView mAlarmTime;
    private TextView mPlaylistName;
    private Switch mAlarmEnabled;
    private View mRunNowButton;
    private View mNowPlaying;
    private View mVolume;
    private ContentObserver mContentObserver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Debug.logLifecycle("MainActivity.onCreate()");
        super.onCreate(savedInstanceState);

        mAlarmCollection = new AlarmCollection(this);
        mAlarm = mAlarmCollection.getAlarm();

        if (mAlarm.getVolume() < 0) {
            mAlarm.setVolume(getAudioManager().getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            mAlarmCollection.onAlarmUpdated(mAlarm, false);
        }

        initUi();
        updateUi();

        bindSpotifyService();
    }

    @Override
    protected void onResume() {
        Debug.logLifecycle("MainActivity.onResume()");
        super.onResume();

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
        mAlarmEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onAlarmEnabledChanged(isChecked);
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
        mVolume = findViewById(R.id.volume);
        mVolume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editVolume();
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
        if (playlist != null) {
            mAlarm.setEnabled(true);
            mAlarm.setPlaylistLink(playlist.getLink());
            mAlarm.setPlaylistName(playlist.getName());
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

    private void runAlarmNow() {
        mAlarm.startAlarm(this);
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
        }
    }

    public PlaylistContainer getPlaylistContainer() {
        return mPlaylistContainer;
    }

    @Override
    public void onStateChanged(int state) {
        super.onStateChanged(state);

        final boolean showNowPlaying = (state == Player.STATE_PLAYING || state == Player.STATE_PAUSED_USER || state == Player.STATE_PAUSED_NOISY || state == Player.STATE_PAUSED_AUDIOFOCUS);
        mNowPlaying.setVisibility(showNowPlaying ? View.VISIBLE : View.GONE);
    }
}
