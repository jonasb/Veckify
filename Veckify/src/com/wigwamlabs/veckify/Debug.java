package com.wigwamlabs.veckify;

import android.os.StrictMode;
import android.util.Log;

public final class Debug {
    private static final String TAG = "Veckify";

    static void enableStrictMode() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());
        }
    }

    public static void logAlarmScheduling(String msg) {
        Log.v(TAG, msg);
    }

    static void logLifecycle(String event) {
        if (BuildConfig.DEBUG) {
            Log.v(TAG, event);
        }
    }
}
