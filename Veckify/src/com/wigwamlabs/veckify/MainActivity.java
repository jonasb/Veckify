package com.wigwamlabs.veckify;

import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Spinner;
import android.widget.TimePicker;

import com.wigwamlabs.spotify.Playlist;
import com.wigwamlabs.spotify.PlaylistContainer;
import com.wigwamlabs.spotify.Session;
import com.wigwamlabs.spotify.ui.PlaylistContainerAdapter;
import com.wigwamlabs.spotify.ui.SpotifyActivity;
import com.wigwamlabs.veckify.alarms.Alarm;
import com.wigwamlabs.veckify.alarms.AlarmCollection;

import java.util.Calendar;

public class MainActivity extends SpotifyActivity {
    private AlarmCollection mAlarmCollection;
    private Alarm mAlarm;
    private TimePicker mTimePicker;
    private PlaylistContainer mPlaylistContainer;
    private Spinner mPlaylistSpinner;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAlarmCollection = new AlarmCollection(this);
        mAlarm = mAlarmCollection.getAlarm();

        initUi();

        bindSpotifyService();
    }

    private void initUi() {
        setContentView(R.layout.activity_main);

        // set up time picker
        mTimePicker = (TimePicker) findViewById(R.id.timePicker);
        final String twelveOrTwentyFour = Settings.System.getString(getContentResolver(), Settings.System.TIME_12_24);
        mTimePicker.setIs24HourView(Boolean.valueOf("24".equals(twelveOrTwentyFour)));
        if (mAlarm != null) {
            mTimePicker.setCurrentHour(Integer.valueOf(mAlarm.getHour()));
            mTimePicker.setCurrentMinute(Integer.valueOf(mAlarm.getMinute()));
        } else {
            final Calendar calendar = Calendar.getInstance();
            mTimePicker.setCurrentHour(Integer.valueOf(calendar.get(Calendar.HOUR_OF_DAY)));
            mTimePicker.setCurrentMinute(Integer.valueOf(calendar.get(Calendar.MINUTE)));
        }
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

    private Playlist getSelectedPlaylist() {
        final Object item = mPlaylistSpinner.getSelectedItem();
        if (item != null && item instanceof Playlist) {
            return (Playlist) item;
        }
        return null;
    }

    private void onSetAlarm() {
        mAlarm.setTime(mTimePicker.getCurrentHour().intValue(), mTimePicker.getCurrentMinute().intValue());
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
