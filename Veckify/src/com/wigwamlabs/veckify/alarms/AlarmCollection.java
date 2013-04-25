package com.wigwamlabs.veckify.alarms;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.Pair;

import com.wigwamlabs.veckify.Debug;

import java.util.Calendar;

public class AlarmCollection {
    private static final String ALARM_ENABLED = "alarm_enabled";
    private static final String ALARM_HOUR = "alarm_hour";
    private static final String ALARM_MINUTE = "alarm_minute";
    private static final String ALARM_REPEAT_DAYS = "alarm_repeat_days";
    private static final String ALARM_ONEOFFTIME_MS = "alarm_oneofftime_ms";
    private static final String ALARM_PLAYLIST_NAME = "alarm_playlist_name";
    private static final String ALARM_PLAYLIST_LINK = "alarm_playlist_link";
    private static final String ALARM_VOLUME = "alarm_volume";
    private static final String ALARM_SHUFFLE = "alarm_shuffle";
    private final SharedPreferences mPreferences;
    private final Context mContext;
    private final AlarmManager mAlarmManager;
    private final Alarm mAlarm;

    public AlarmCollection(Context context) {
        mContext = context;
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        mAlarm = new Alarm();
        mAlarm.setEnabled(mPreferences.getBoolean(ALARM_ENABLED, false));
        mAlarm.setTime(mPreferences.getInt(ALARM_HOUR, 9), mPreferences.getInt(ALARM_MINUTE, 0));
        mAlarm.setRepeatDays(mPreferences.getInt(ALARM_REPEAT_DAYS, Alarm.DAYS_NONE));
        mAlarm.setOneOffTimeMs(mPreferences.getLong(ALARM_ONEOFFTIME_MS, 0));
        mAlarm.setPlaylistName(mPreferences.getString(ALARM_PLAYLIST_NAME, null));
        mAlarm.setPlaylistLink(mPreferences.getString(ALARM_PLAYLIST_LINK, null));
        mAlarm.setVolume(mPreferences.getInt(ALARM_VOLUME, -1));
        mAlarm.setShuffle(mPreferences.getBoolean(ALARM_SHUFFLE, false));

        mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    public Alarm getAlarm() {
        return mAlarm;
    }

    private Pair<Alarm, Calendar> getNextAlarm() {
        final long nowMs = System.currentTimeMillis();
        if (mAlarm.updateBeforeScheduling(nowMs)) {
            onAlarmUpdated(mAlarm, false);
        }

        if (mAlarm.isEnabled()) {
            return Pair.create(mAlarm, mAlarm.getNextAlarmTime(nowMs));
        }
        return null;
    }

    public void onAlarmUpdated(Alarm alarm, boolean reschedule) {
        alarm.updateBeforeSaving(System.currentTimeMillis());

        mPreferences.edit()
                .putBoolean(ALARM_ENABLED, alarm.isEnabled())
                .putInt(ALARM_HOUR, alarm.getHour())
                .putInt(ALARM_MINUTE, alarm.getMinute())
                .putInt(ALARM_REPEAT_DAYS, alarm.getRepeatDays())
                .putLong(ALARM_ONEOFFTIME_MS, alarm.getOneOffTimeMs())
                .putString(ALARM_PLAYLIST_NAME, alarm.getPlaylistName())
                .putString(ALARM_PLAYLIST_LINK, alarm.getPlaylistLink())
                .putInt(ALARM_VOLUME, alarm.getVolume())
                .putBoolean(ALARM_SHUFFLE, alarm.isShuffle())
                .apply();

        if (reschedule) {
            rescheduleAlarm();
        }
    }

    void rescheduleAlarm() {
        final Pair<Alarm, Calendar> next = getNextAlarm();

        final Intent intent = new Intent(BroadcastReceiver.ACTION_ALARM);
        if (next != null) {
            final long eventTimeMs = next.second.getTimeInMillis();
            intent.putExtra(BroadcastReceiver.EXTRA_EVENT_TIME_MS, eventTimeMs);
            intent.putExtra(BroadcastReceiver.EXTRA_PLAYLIST_LINK, next.first.getPlaylistLink());
        }
        final PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        if (next != null) {
            mAlarmManager.set(AlarmManager.RTC_WAKEUP, next.second.getTimeInMillis(), pendingIntent);
            Debug.logAlarmScheduling("Scheduling alarm at " + DateFormat.format("yyyy-MM-dd kk:mm", next.second));
        } else {
            mAlarmManager.cancel(pendingIntent);
            Debug.logAlarmScheduling("No next alarm, cancel any existing");
        }
    }
}
