package com.wigwamlabs.veckify.alarms;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.wigwamlabs.veckify.Debug;

public class BroadcastReceiver extends android.content.BroadcastReceiver {
    static final String ACTION_ALARM = "com.wigwamlabs.veckify.alarms.BroadcastReceiver.ALARM";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            final AlarmCollection alarmCollection = new AlarmCollection(context);
            if (ACTION_ALARM.equals(action)) {
                Debug.logAlarmScheduling("Received alarm broadcast");
                Toast.makeText(context, "ALARM", Toast.LENGTH_LONG).show();
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
