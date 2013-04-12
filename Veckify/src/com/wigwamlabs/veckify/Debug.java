package com.wigwamlabs.veckify;

import android.util.Log;

public final class Debug {
    private static final String TAG = "Veckify";

    public static void logAlarmScheduling(String msg) {
        Log.v(TAG, msg);
    }
}
