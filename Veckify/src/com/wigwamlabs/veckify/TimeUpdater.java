package com.wigwamlabs.veckify;

import android.os.Handler;

import java.util.Calendar;

class TimeUpdater implements Runnable {
    private final Handler mHandler;
    private final Callback mCallback;

    TimeUpdater(Handler handler, Callback callback) {
        mHandler = handler;
        mCallback = callback;
    }

    void start() {
        mHandler.postDelayed(this, updateCurrentTime());
    }

    void stop() {
        mHandler.removeCallbacks(this);
    }

    @Override
    public void run() {
        final long timeToNextMinuteMs = updateCurrentTime();
        mHandler.postDelayed(this, timeToNextMinuteMs);
    }

    private long updateCurrentTime() {
        final Calendar cal = Calendar.getInstance();

        // calculate current time and time to next minute
        final int secs = cal.get(Calendar.SECOND);
        int secsToNextMinute = 60 - secs;
        if (secsToNextMinute < 2) { // allow for some slack in scheduling
            cal.add(Calendar.MINUTE, 1);
            secsToNextMinute += 60;
        }

        mCallback.onTimeUpdated(cal);

        return secsToNextMinute * 1000;
    }

    interface Callback {
        void onTimeUpdated(Calendar cal);
    }
}
