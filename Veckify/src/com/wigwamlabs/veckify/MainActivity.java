package com.wigwamlabs.veckify;

import android.os.Bundle;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

import com.wigwamlabs.spotify.Playlist;
import com.wigwamlabs.spotify.PlaylistContainer;
import com.wigwamlabs.spotify.Session;
import com.wigwamlabs.spotify.ui.PlaylistContainerAdapter;
import com.wigwamlabs.spotify.ui.SpotifyActivity;
import com.wigwamlabs.veckify.alarms.Alarm;
import com.wigwamlabs.veckify.alarms.AlarmCollection;

public class MainActivity extends SpotifyActivity {
    private AlarmCollection mAlarmCollection;
    private Alarm mAlarm;
    private PlaylistContainer mPlaylistContainer;
    private TextView mAlarmTime;
    private Spinner mPlaylistSpinner;
    private int mAlarmHour;
    private int mAlarmMinute;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAlarmCollection = new AlarmCollection(this);
        mAlarm = mAlarmCollection.getAlarm();
        mAlarmHour = mAlarm.getHour();
        mAlarmMinute = mAlarm.getMinute();

        initUi();

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
        onAlarmTimeSet(mAlarmHour, mAlarmMinute);
        // set up spinner
        mPlaylistSpinner = (Spinner) findViewById(R.id.playlistSpinner);
        //
        findViewById(R.id.setAlarmButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSetAlarm();
            }
        });
        findViewById(R.id.runNowButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runAlarmNow();
            }
        });
    }

    private void onEditTime() {
        final TimePickerDialogFragment fragment = new TimePickerDialogFragment();
        fragment.show(getFragmentManager(), "timepicker");
    }

    public int getAlarmHour() {
        return mAlarmHour;
    }

    public int getAlarmMinute() {
        return mAlarmMinute;
    }

    public void onAlarmTimeSet(int hour, int minute) {
        mAlarmHour = hour;
        mAlarmMinute = minute;

        //TODO am/pm
        mAlarmTime.setText(String.format("%d:%02d", hour, minute));
    }

    private Playlist getSelectedPlaylist() {
        final Object item = mPlaylistSpinner.getSelectedItem();
        if (item != null && item instanceof Playlist) {
            return (Playlist) item;
        }
        return null;
    }

    private void onSetAlarm() {
        mAlarm.setTime(mAlarmHour, mAlarmMinute);
        final Playlist playlist = getSelectedPlaylist();
        mAlarm.setPlaylistLink(playlist == null ? null : playlist.getLink());
        mAlarmCollection.onAlarmUpdated(mAlarm);
    }

    private void runAlarmNow() {
        final Alarm alarm = new Alarm();
        final Playlist playlist = getSelectedPlaylist();
        alarm.setPlaylistLink(playlist == null ? null : playlist.getLink());
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
            mPlaylistSpinner.setAdapter(new PlaylistContainerAdapter(this, mPlaylistContainer));
        }
    }
}
