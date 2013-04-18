package com.wigwamlabs.veckify;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.wigwamlabs.spotify.Playlist;
import com.wigwamlabs.spotify.PlaylistContainer;
import com.wigwamlabs.spotify.Session;
import com.wigwamlabs.spotify.ui.SpotifyActivity;
import com.wigwamlabs.veckify.alarms.Alarm;
import com.wigwamlabs.veckify.alarms.AlarmCollection;

public class MainActivity extends SpotifyActivity {
    private AlarmCollection mAlarmCollection;
    private Alarm mAlarm;
    private PlaylistContainer mPlaylistContainer;
    private TextView mAlarmTime;
    private TextView mPlaylistName;

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
        // set up playlist picker
        mPlaylistName = (TextView) findViewById(R.id.playlistName);
        mPlaylistName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPickPlaylist();
            }
        });
        //
        findViewById(R.id.runNowButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runAlarmNow();
            }
        });
    }

    private void updateUi() {
        //TODO am/pm
        mAlarmTime.setText(String.format("%d:%02d", mAlarm.getHour(), mAlarm.getMinute()));

        mPlaylistName.setText(mAlarm.getPlaylistName()); //TODO deal with empty
    }

    private void onEditTime() {
        final TimePickerDialogFragment fragment = new TimePickerDialogFragment();
        fragment.show(getFragmentManager(), "timepicker");
    }

    Alarm getAlarm() {
        return mAlarm;
    }

    public void onAlarmTimeSet(int hour, int minute) {
        mAlarm.setTime(hour, minute);
        mAlarmCollection.onAlarmUpdated(mAlarm, true);
        updateUi();
    }

    private void onPickPlaylist() {
        final PlaylistPickerFragment fragment = new PlaylistPickerFragment();
        fragment.show(getFragmentManager(), "playlist-picker");
    }

    public void onPlaylistPicked(Playlist playlist) {
        if (playlist != null) {
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
        final Alarm alarm = new Alarm();
        alarm.setPlaylistLink(mAlarm.getPlaylistLink());
        mAlarmCollection.runAlarmNow(alarm);
    }

    @Override
    protected void onSpotifySessionAttached(Session spotifySession) {
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
}
