package com.wigwamlabs.veckify;

import android.app.Activity;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.TimePicker;

import com.wigwamlabs.veckify.alarms.Alarm;
import com.wigwamlabs.veckify.alarms.AlarmCollection;

import java.util.Calendar;


public class MainActivity extends Activity {
    private AlarmCollection mAlarmCollection;
    private Alarm mAlarm;
    private TimePicker mTimePicker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAlarmCollection = new AlarmCollection(this);
        mAlarm = mAlarmCollection.getAlarm();

        // set up time picker
        mTimePicker = (TimePicker) findViewById(R.id.timePicker);
        final String twelveOrTwentyFour = Settings.System.getString(getContentResolver(), Settings.System.TIME_12_24);
        mTimePicker.setIs24HourView("24".equals(twelveOrTwentyFour));
        if (mAlarm != null) {
            mTimePicker.setCurrentHour(mAlarm.getHour());
            mTimePicker.setCurrentMinute(mAlarm.getMinute());
        } else {
            final Calendar calendar = Calendar.getInstance();
            mTimePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
            mTimePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));
        }

        findViewById(R.id.setAlarmButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSetAlarm();
            }
        });
    }

    private void onSetAlarm() {
        mAlarm.setTime(mTimePicker.getCurrentHour(), mTimePicker.getCurrentMinute());
        mAlarmCollection.onAlarmUpdated(mAlarm);
    }
}
