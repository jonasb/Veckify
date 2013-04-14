package com.wigwamlabs.veckify;

import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.TimePicker;

import com.wigwamlabs.spotify.Session;
import com.wigwamlabs.spotify.ui.SpotifyActivity;
import com.wigwamlabs.veckify.alarms.Alarm;
import com.wigwamlabs.veckify.alarms.AlarmCollection;

import java.util.Calendar;

public class MainActivity extends SpotifyActivity {
    private AlarmCollection mAlarmCollection;
    private Alarm mAlarm;
    private TimePicker mTimePicker;

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

    private void onSetAlarm() {
        mAlarm.setTime(mTimePicker.getCurrentHour().intValue(), mTimePicker.getCurrentMinute().intValue());
        mAlarmCollection.onAlarmUpdated(mAlarm);
    }

    private void runAlarmNow() {
        mAlarmCollection.runAlarmNow(mAlarm);
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
    }
}
