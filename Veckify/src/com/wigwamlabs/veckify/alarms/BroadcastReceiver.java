package com.wigwamlabs.veckify.alarms;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.wigwamlabs.veckify.Debug;
import com.wigwamlabs.veckify.R;

public class BroadcastReceiver extends android.content.BroadcastReceiver {
    static final String ACTION_ALARM = "com.wigwamlabs.veckify.alarms.BroadcastReceiver.ALARM";
    static final String EXTRA_EVENT_TIME_MS = "eventtime";
    private static final int MAX_TIME_SINCE_SCHEDULED_MS = 5 * 60 * 1000;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            final AlarmCollection alarmCollection = new AlarmCollection(context);
            if (ACTION_ALARM.equals(action)) {
                final long eventTimeMs = intent.getLongExtra(EXTRA_EVENT_TIME_MS, 0);
                final long timeSinceEventMs = System.currentTimeMillis() - eventTimeMs;
                Debug.logAlarmScheduling("Received alarm broadcast " + timeSinceEventMs / 1000 + "s after scheduled");

                if (timeSinceEventMs < MAX_TIME_SINCE_SCHEDULED_MS) {
                    final NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                            .setDefaults(Notification.DEFAULT_SOUND)
                            .setSmallIcon(R.drawable.ic_stat_alarm)
                            .setPriority(NotificationCompat.PRIORITY_MAX)
                            .setContentTitle("Veckify")
                            .setContentText("Alarm");
                    final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(0, builder.build());
                } else {
                    Debug.logAlarmScheduling("Skipping alarm since too long since scheduled");
                }
                alarmCollection.rescheduleAlarm();
            } else if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
                Debug.logAlarmScheduling("Device booted");
                alarmCollection.rescheduleAlarm();
            } else if (Intent.ACTION_TIME_CHANGED.equals(action)) {
                Debug.logAlarmScheduling("Time changed");
                alarmCollection.rescheduleAlarm();
            } else if (Intent.ACTION_TIMEZONE_CHANGED.equals(action)) {
                Debug.logAlarmScheduling("Timezone changed");
                // need to reschedule since alarm is scheduled in UTC
                alarmCollection.rescheduleAlarm();
            }
        }
    }
}
