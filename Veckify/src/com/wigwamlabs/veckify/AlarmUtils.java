package com.wigwamlabs.veckify;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateFormat;

import com.wigwamlabs.veckify.db.AlarmsCursor;

import java.util.Calendar;

public final class AlarmUtils {
    public static final int DAY_MONDAY = 0x01;
    public static final int DAY_TUESDAY = 0x02;
    public static final int DAY_WEDNESDAY = 0x04;
    public static final int DAY_THURSDAY = 0x08;
    public static final int DAY_FRIDAY = 0x10;
    public static final int DAY_SATURDAY = 0x20;
    public static final int DAY_SUNDAY = 0x40;
    public static final int DAYS_NONE = 0x00;
    public static final int DAYS_ALL = DAY_MONDAY | DAY_TUESDAY | DAY_WEDNESDAY | DAY_THURSDAY | DAY_FRIDAY | DAY_SATURDAY | DAY_SUNDAY;
    private static final long MINIMUM_TIME_TO_ALARM_MS = 60 * 1000;
    private final Context mContext;
    private final AlarmManager mAlarmManager;
    private final NotificationManager mNotificationManager;
    private final PendingIntent mAlarmPendingIntent;
    private final String mDateFormatYear;
    private final String mDateFormatNoYear;

    public AlarmUtils(Context context) {
        mContext = context;

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

    public static Calendar getNextAlarmTime(boolean enabled, int hour, int minute, int repeatDays, long oneOffTimeMs, long nowMs) {
        if (!enabled) {
            return null;
        }

        if (repeatDays == DAYS_NONE && oneOffTimeMs > 0) { // one-off and we like to check that it's still valid
            // check if in past
            if (oneOffTimeMs < nowMs) {
                return null;
            }
        }

        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(nowMs);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        final long timeToAlarmMs = cal.getTimeInMillis() - nowMs;

        if (timeToAlarmMs < MINIMUM_TIME_TO_ALARM_MS) {
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        if (repeatDays != DAYS_NONE) {
            for (int i = 0; i < 7; i++) {
                final int day = cal.get(Calendar.DAY_OF_WEEK);
                if (day == Calendar.MONDAY && (repeatDays & DAY_MONDAY) != 0) {
                    break;
                }
                if (day == Calendar.TUESDAY && (repeatDays & DAY_TUESDAY) != 0) {
                    break;
                }
                if (day == Calendar.WEDNESDAY && (repeatDays & DAY_WEDNESDAY) != 0) {
                    break;
                }
                if (day == Calendar.THURSDAY && (repeatDays & DAY_THURSDAY) != 0) {
                    break;
                }
                if (day == Calendar.FRIDAY && (repeatDays & DAY_FRIDAY) != 0) {
                    break;
                }
                if (day == Calendar.SATURDAY && (repeatDays & DAY_SATURDAY) != 0) {
                    break;
                }
                if (day == Calendar.SUNDAY && (repeatDays & DAY_SUNDAY) != 0) {
                    break;
                }
                cal.add(Calendar.DAY_OF_YEAR, 1);
            }
        }

        return cal;
    }

    public void reschedule(Context context, AlarmsCursor alarm) {
        final long nowMs = System.currentTimeMillis();

        Calendar nextTime = null;
        long nextAlarmId = 0;
        String nextPlaylistName = null;
        String nextPlaylistLink = null;

        for (boolean hasItem = alarm.moveToFirst(); hasItem; hasItem = alarm.moveToNext()) {
            final Calendar alarmTime = AlarmUtils.getNextAlarmTime(alarm.enabled(), alarm.hour(), alarm.minute(), alarm.repeatDays(), alarm.oneoffTimeMs(), nowMs);
            if (alarmTime != null) {
                if (nextTime == null || alarmTime.before(nextTime)) {
                    nextTime = alarmTime;
                    nextAlarmId = alarm._id();
                    nextPlaylistName = alarm.playlistName();
                    nextPlaylistLink = alarm.playlistLink();
                }
            }
        }

        final Intent intent = new Intent(BroadcastReceiver.ACTION_ALARM);
        if (nextTime != null) {
            intent.putExtra(BroadcastReceiver.EXTRA_EVENT_TIME_MS, nextTime.getTimeInMillis());
            intent.putExtra(BroadcastReceiver.EXTRA_ALARM_ID, nextAlarmId);
            intent.putExtra(BroadcastReceiver.EXTRA_PLAYLIST_LINK, nextPlaylistLink);
        }
        final PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        if (nextTime != null) {
            Debug.logAlarmScheduling("Scheduling alarm at " + DateFormat.format("yyyy-MM-dd kk:mm", nextTime));
            mAlarmManager.set(AlarmManager.RTC_WAKEUP, nextTime.getTimeInMillis(), pendingIntent);
            mNotificationManager.notify(R.id.notificationAlarm, getNotification(nextPlaylistName, nextTime));
        } else {
            Debug.logAlarmScheduling("No next alarm, cancel any existing");
            mAlarmManager.cancel(pendingIntent);
            mNotificationManager.cancel(R.id.notificationAlarm);
        }
    }

    private Notification getNotification(String playlistName, Calendar calendar) {
        final Calendar now = Calendar.getInstance();
        final String format = (now.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)) ? mDateFormatNoYear : mDateFormatYear;
        final String description = String.format(mContext.getString(R.string.notification_alarm_description),
                DateFormat.format(format, calendar),
                playlistName);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.drawable.ic_stat_alarm)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(mAlarmPendingIntent)
                .setContentTitle(mContext.getString(R.string.notification_alarm_title))
                .setContentText(description)
                .setOngoing(true);
        return builder.build();
    }
}
