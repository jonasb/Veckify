package com.wigwamlabs.veckify.alarms;

import java.util.Calendar;

public class Alarm {
    private static final long MINIMUM_TIME_TO_ALARM_MS = 60 * 1000;
    private boolean mEnabled;
    private int mHour;
    private int mMinute;
    private String mPlaylistName;
    private String mPlaylistLink;

    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
    }

    public boolean isEnabled() {
        return mEnabled;
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

    public void setPlaylistName(String name) {
        mPlaylistName = name;
    }

    public String getPlaylistName() {
        return mPlaylistName;
    }

    public void setPlaylistLink(String link) {
        mPlaylistLink = link;
    }

    public String getPlaylistLink() {
        return mPlaylistLink;
    }

    public Calendar getNextAlarmTime() {
        final Calendar cal = Calendar.getInstance();
        final long nowMs = System.currentTimeMillis();
        cal.setTimeInMillis(nowMs);
        cal.set(Calendar.HOUR_OF_DAY, mHour);
        cal.set(Calendar.MINUTE, mMinute);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        final long timeToAlarmMs = cal.getTimeInMillis() - nowMs;

        if (timeToAlarmMs < MINIMUM_TIME_TO_ALARM_MS) {
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        return cal;
    }
}
