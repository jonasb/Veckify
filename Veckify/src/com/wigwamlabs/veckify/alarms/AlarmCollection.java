package com.wigwamlabs.veckify.alarms;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class AlarmCollection {
    private static final String ALARM_HOUR = "alarm_hour";
    private static final String ALARM_MINUTE = "alarm_minute";
    private Alarm mAlarm;
    private final SharedPreferences mPreferences;

    public AlarmCollection(Context context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        final int hour = mPreferences.getInt(ALARM_HOUR, 9);
        final int minute = mPreferences.getInt(ALARM_MINUTE, 0);
        mAlarm = new Alarm(hour, minute);
    }

    public Alarm getAlarm() {
        return mAlarm;
    }

    public void onAlarmUpdated(Alarm alarm) {
        mPreferences.edit()
                .putInt(ALARM_HOUR, alarm.getHour())
                .putInt(ALARM_MINUTE, alarm.getMinute())
                .apply();
    }
}
