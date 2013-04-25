package com.wigwamlabs.veckify.alarms;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.wigwamlabs.spotify.SpotifyService;
import com.wigwamlabs.veckify.NowPlayingActivity;

import java.util.Calendar;

public class Alarm {
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
    private boolean mEnabled;
    private int mHour;
    private int mMinute;
    private int mRepeatDays;
    private String mPlaylistName;
    private String mPlaylistLink;
    private int mVolume;

    public boolean isEnabled() {
        return mEnabled;
    }

    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
    }

    public int getHour() {
        return mHour;
    }

    public int getMinute() {
        return mMinute;
    }

    public void setTime(int hour, int minute) {
        mHour = hour;
        mMinute = minute;
    }

    public int getRepeatDays() {
        return mRepeatDays;
    }

    public void setRepeatDays(int repeatDays) {
        mRepeatDays = repeatDays;
    }

    public void setRepeatDay(int day, boolean enabled) {
        if (enabled) {
            mRepeatDays |= day;
        } else {
            mRepeatDays &= ~day;
        }
    }

    public String getPlaylistName() {
        return mPlaylistName;
    }

    public void setPlaylistName(String name) {
        mPlaylistName = name;
    }

    public String getPlaylistLink() {
        return mPlaylistLink;
    }

    public void setPlaylistLink(String link) {
        mPlaylistLink = link;
    }

    public int getVolume() {
        return mVolume;
    }

    public void setVolume(int volume) {
        mVolume = volume;
    }

    public Calendar getNextAlarmTime(long nowMs) {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(nowMs);
        cal.set(Calendar.HOUR_OF_DAY, mHour);
        cal.set(Calendar.MINUTE, mMinute);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        final long timeToAlarmMs = cal.getTimeInMillis() - nowMs;

        if (timeToAlarmMs < MINIMUM_TIME_TO_ALARM_MS) {
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        if (mRepeatDays != DAYS_NONE) {
            for (int i = 0; i < 7; i++) {
                final int day = cal.get(Calendar.DAY_OF_WEEK);
                if (day == Calendar.MONDAY && (mRepeatDays & DAY_MONDAY) != 0) {
                    break;
                }
                if (day == Calendar.TUESDAY && (mRepeatDays & DAY_TUESDAY) != 0) {
                    break;
                }
                if (day == Calendar.WEDNESDAY && (mRepeatDays & DAY_WEDNESDAY) != 0) {
                    break;
                }
                if (day == Calendar.THURSDAY && (mRepeatDays & DAY_THURSDAY) != 0) {
                    break;
                }
                if (day == Calendar.FRIDAY && (mRepeatDays & DAY_FRIDAY) != 0) {
                    break;
                }
                if (day == Calendar.SATURDAY && (mRepeatDays & DAY_SATURDAY) != 0) {
                    break;
                }
                if (day == Calendar.SUNDAY && (mRepeatDays & DAY_SUNDAY) != 0) {
                    break;
                }
                cal.add(Calendar.DAY_OF_YEAR, 1);
            }
        }

        return cal;
    }

    public void startAlarm(Context context) {
        // setup intent to launch now playing
        final Intent nowPlayingIntent = new Intent(context, NowPlayingActivity.class);
        nowPlayingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // tell service to start playing
        final Intent intent = new Intent(context, SpotifyService.class);
        intent.setAction(SpotifyService.ACTION_PLAY_PLAYLIST);
        intent.putExtra(SpotifyService.EXTRA_LINK, mPlaylistLink);
        intent.putExtra(SpotifyService.EXTRA_INTENT, PendingIntent.getActivity(context, 0, nowPlayingIntent, 0));
        intent.putExtra(SpotifyService.EXTRA_VOLUME, mVolume);
        context.startService(intent);

        // launch now playing in alarm mode
        nowPlayingIntent.setAction(NowPlayingActivity.ACTION_ALARM);
        context.startActivity(nowPlayingIntent);
    }
}
