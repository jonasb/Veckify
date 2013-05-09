package com.wigwamlabs.veckify;

import android.content.Context;
import android.content.Intent;
import android.util.Pair;

import com.wigwamlabs.veckify.db.AlarmEntry;
import com.wigwamlabs.veckify.db.AlarmsCursor;
import com.wigwamlabs.veckify.db.DataDatabaseAdapter;

public class BroadcastReceiver extends android.content.BroadcastReceiver {
    public static final String ACTION_ALARM = "com.wigwamlabs.veckify.BroadcastReceiver.ALARM";
    public static final String EXTRA_EVENT_TIME_MS = "eventtime";
    public static final String EXTRA_ALARM_ID = "alarmid";
    public static final String EXTRA_PLAYLIST_LINK = "playlist_link";
    private static final int MAX_TIME_SINCE_SCHEDULED_MS = 5 * 60 * 1000;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            final AlarmUtils alarmUtils = new AlarmUtils(context);
            final Application application = (Application) context.getApplicationContext();
            final DataDatabaseAdapter db = application.getDb();
            if (ACTION_ALARM.equals(action)) {
                final long eventTimeMs = intent.getLongExtra(EXTRA_EVENT_TIME_MS, 0);
                final long alarmId = intent.getLongExtra(EXTRA_ALARM_ID, 0);
                final long timeSinceEventMs = System.currentTimeMillis() - eventTimeMs;
                Debug.logAlarmScheduling("Received alarm broadcast " + timeSinceEventMs / 1000 + "s after scheduled");

                if (timeSinceEventMs < MAX_TIME_SINCE_SCHEDULED_MS) {
                    startAlarm(context, db, alarmId);
                } else {
                    Debug.logAlarmScheduling("Skipping alarm since too long since scheduled");
                }
                rescheduleAlarm(context, alarmUtils, db);
            } else if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
                Debug.logAlarmScheduling("Device booted");
                rescheduleAlarm(context, alarmUtils, db);
            } else if (Intent.ACTION_TIME_CHANGED.equals(action)) {
                Debug.logAlarmScheduling("Time changed");
                rescheduleAlarm(context, alarmUtils, db);
            } else if (Intent.ACTION_TIMEZONE_CHANGED.equals(action)) {
                Debug.logAlarmScheduling("Timezone changed");
                // need to reschedule since alarm is scheduled in UTC
                rescheduleAlarm(context, alarmUtils, db);
            }
        }
    }

    private void rescheduleAlarm(Context context, AlarmUtils alarmUtils, DataDatabaseAdapter db) {
        final AlarmsCursor alarm = AlarmsCursor.getAllAlarmsCursor(db);
        alarmUtils.reschedule(context, alarm);
        alarm.close();
    }

    private void startAlarm(Context context, DataDatabaseAdapter db, long alarmId) {
        final AlarmsCursor alarm = AlarmsCursor.getAlarmCursor(db, alarmId);
        if (alarm.moveToFirst()) {
            final Pair<Intent, Intent> intents = alarm.createIntents(context);
            context.startService(intents.first);
            context.startActivity(intents.second);
        }
        alarm.close();

        final AlarmEntry entry = new AlarmEntry();
        entry.setEnabled(false);
        entry.setOneoffTimeMs(0);
        entry.updateExpiredOneoffAlarms(db);
    }
}
