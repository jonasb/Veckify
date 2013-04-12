package com.wigwamlabs.veckify.alarms;

public class Alarm {
    private int mHour;
    private int mMinute;

    Alarm(int hour, int minute) {
        mHour = hour;
        mMinute = minute;
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
}
