package com.wigwamlabs.veckify;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.wigwamlabs.spotify.Player;
import com.wigwamlabs.spotify.Playlist;
import com.wigwamlabs.spotify.PlaylistContainer;
import com.wigwamlabs.spotify.Session;
import com.wigwamlabs.spotify.Track;
import com.wigwamlabs.spotify.ui.SpotifyPlayerActivity;
import com.wigwamlabs.veckify.alarms.Alarm;
import com.wigwamlabs.veckify.alarms.AlarmCollection;

public class MainActivity extends SpotifyPlayerActivity {
    private AlarmCollection mAlarmCollection;
    private Alarm mAlarm;
    private PlaylistContainer mPlaylistContainer;
    private TextView mAlarmTime;
    private TextView mPlaylistName;
    private Switch mAlarmEnabled;
    private View mRunNowButton;
    private View mNowPlaying;
    private TextView mTrackArtists;
    private TextView mTrackName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAlarmCollection = new AlarmCollection(this);
        mAlarm = mAlarmCollection.getAlarm();

        initUi();
        updateUi();

        bindSpotifyService();
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
        mTrackArtists = (TextView) findViewById(R.id.trackArtists);
        mTrackName = (TextView) findViewById(R.id.trackName);
    }

    private void updateUi() {
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

    private void runAlarmNow() {
        Alarm.startAlarm(this, mAlarm.getPlaylistLink());
    }

    @Override
    protected void onSpotifySessionAttached(Session spotifySession) {
        super.onSpotifySessionAttached(spotifySession);
        setAutoLogin(true);
    }

    @Override
    public void onLoggedIn(int error) {
        super.onLoggedIn(error);
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
        mNowPlaying.setVisibility(state == Player.STATE_PLAYING ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onCurrentTrackUpdated(Track track) {
        if (track != null) {
            mTrackArtists.setText(track.getArtistsString());
            mTrackName.setText(track.getName());
        }
    }

    @Override
    public void onTrackProgress(int secondsPlayed, int secondsDuration) {
    }
}
