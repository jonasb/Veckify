package com.wigwamlabs.veckify.alarms;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;

import com.wigwamlabs.veckify.Debug;

import java.util.Calendar;

public class AlarmCollection {
    private static final String ALARM_HOUR = "alarm_hour";
    private static final String ALARM_MINUTE = "alarm_minute";
    private final SharedPreferences mPreferences;
    private final Context mContext;
    private final AlarmManager mAlarmManager;
    private Alarm mAlarm;

    public AlarmCollection(Context context) {
        mContext = context;
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        final int hour = mPreferences.getInt(ALARM_HOUR, 9);
        final int minute = mPreferences.getInt(ALARM_MINUTE, 0);
        mAlarm = new Alarm(hour, minute);

        mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    public Alarm getAlarm() {
        return mAlarm;
    }

    public void onAlarmUpdated(Alarm alarm) {
        mPreferences.edit()
                .putInt(ALARM_HOUR, alarm.getHour())
                .putInt(ALARM_MINUTE, alarm.getMinute())
                .apply();

        rescheduleAlarm();
    }

    void rescheduleAlarm() {
        final Calendar cal = mAlarm.getNextAlarmTime();

        final Intent intent = new Intent(BroadcastReceiver.ACTION_ALARM);
        final PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        mAlarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
        Debug.logAlarmScheduling("Scheduling alarm at " + DateFormat.format("yyyy-MM-dd kk:mm", cal));
    }
}
