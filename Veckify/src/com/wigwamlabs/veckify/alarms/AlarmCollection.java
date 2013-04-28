package com.wigwamlabs.veckify.alarms;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateFormat;
import android.util.Pair;

import com.wigwamlabs.veckify.Debug;
import com.wigwamlabs.veckify.MainActivity;
import com.wigwamlabs.veckify.R;

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
    private final NotificationManager mNotificationManager;
    private final PendingIntent mAlarmPendingIntent;
    private final String mDateFormatYear;
    private final String mDateFormatNoYear;

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
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        mAlarmPendingIntent = PendingIntent.getActivity(mContext, 0, new Intent(mContext, MainActivity.class), 0);

        // TODO am/pm
        final char[] formatOrder = DateFormat.getDateFormatOrder(mContext);
        mDateFormatYear = String.format("%s/%s/%s k:mm", formatOrder[0], formatOrder[1], formatOrder[2]);
        final StringBuilder sb = new StringBuilder();
        for (char c : formatOrder) {
            if (c == DateFormat.YEAR) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append('/');
            }
            sb.append(c);
        }
        mDateFormatNoYear = sb.toString() + " k:mm";
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
            Debug.logAlarmScheduling("Scheduling alarm at " + DateFormat.format("yyyy-MM-dd kk:mm", next.second));
            mAlarmManager.set(AlarmManager.RTC_WAKEUP, next.second.getTimeInMillis(), pendingIntent);
            mNotificationManager.notify(R.id.notificationAlarm, getNotification(next.first, next.second));
        } else {
            Debug.logAlarmScheduling("No next alarm, cancel any existing");
            mAlarmManager.cancel(pendingIntent);
            mNotificationManager.cancel(R.id.notificationAlarm);
        }
    }

    private Notification getNotification(Alarm alarm, Calendar calendar) {
        final Calendar now = Calendar.getInstance();
        final String format = (now.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)) ? mDateFormatNoYear : mDateFormatYear;
        final String description = String.format(mContext.getString(R.string.notification_alarm_description),
                DateFormat.format(format, calendar),
                alarm.getPlaylistName());

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext)
                .setSmallIcon(com.wigwamlabs.spotify.R.drawable.ic_stat_alarm)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(mAlarmPendingIntent)
                .setContentTitle(mContext.getString(R.string.notification_alarm_title))
                .setContentText(description)
                .setOngoing(true);
        return builder.build();
    }
}
