package com.wigwamlabs.spotify.tts;

import java.util.Calendar;

public class TimeTtsProvider implements TtsProvider {
    private long mLastPlayedMs;

    @Override
    public String getText() {
        final long nowMs = System.currentTimeMillis();
        if (nowMs - mLastPlayedMs < 5000) {
            return null;
        }
        mLastPlayedMs = nowMs;

        final Calendar cal = Calendar.getInstance();
        final int min = cal.get(Calendar.MINUTE);
        final boolean past = (min <= 30);
        final String pastString = (past ? "past" : "to");
        if (!past) {
            cal.add(Calendar.HOUR, 1);
        }
        final int hourOfDay = cal.get(Calendar.HOUR_OF_DAY);
        final String hourString;
        if (hourOfDay == 12) {
            hourString = "noon";
        } else if (hourOfDay == 0 || hourOfDay == 24) {
            hourString = "midnight";
        } else {
            hourString = Integer.toString(cal.get(Calendar.HOUR));
        }

        final String time;
        switch (min) {
        case 0:
            if (hourOfDay == 0 || hourOfDay == 12 || hourOfDay == 24) {
                time = hourString;
            } else {
                time = hourString + " o'clock";
            }
            break;
        case 15:
        case 45:
            time = String.format("quarter %s %s", pastString, hourString);
            break;
        case 30:
            time = "half past " + hourString;
            break;
        default:
            final int minsToHour = (past ? min : 60 - min);
            final String hourPrefix;
            if (minsToHour == 1) {
                hourPrefix = " minute";
            } else if (minsToHour % 5 != 0) {
                hourPrefix = " minutes";
            } else {
                hourPrefix = "";
            }
            time = minsToHour + hourPrefix + " " + pastString + " " + hourString;
        }
        return "It's " + time;
    }
}
