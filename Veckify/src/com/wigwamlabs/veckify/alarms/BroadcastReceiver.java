package com.wigwamlabs.veckify.alarms;

import android.content.Context;
import android.content.Intent;

import com.wigwamlabs.spotify.SpotifyService;
import com.wigwamlabs.veckify.Debug;
import com.wigwamlabs.veckify.NowPlayingActivity;

public class BroadcastReceiver extends android.content.BroadcastReceiver {
    static final String ACTION_ALARM = "com.wigwamlabs.veckify.alarms.BroadcastReceiver.ALARM";
    static final String EXTRA_EVENT_TIME_MS = "eventtime";
    static final String EXTRA_PLAYLIST_LINK = "playlist_link";
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
                    startAlarm(context, intent.getStringExtra(EXTRA_PLAYLIST_LINK));
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

    private void startAlarm(Context context, String playlistLink) {
        {
            final Intent intent = new Intent(context, SpotifyService.class);
            intent.setAction(SpotifyService.ACTION_PLAY_PLAYLIST);
            intent.putExtra(SpotifyService.EXTRA_LINK, playlistLink);
            context.startService(intent);
        }
        {
            final Intent intent = new Intent(context, NowPlayingActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }
}
