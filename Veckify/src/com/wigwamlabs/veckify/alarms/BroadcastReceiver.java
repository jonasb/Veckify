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
            if (ACTION_ALARM.equals(intent.getAction())) {
                Debug.logAlarmScheduling("Received alarm broadcast");
                Toast.makeText(context, "ALARM", Toast.LENGTH_LONG).show();

                final AlarmCollection alarmCollection = new AlarmCollection(context);
                alarmCollection.rescheduleAlarm();
            }
        }
    }
}
