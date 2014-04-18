package com.wigwamlabs.veckify;

import android.os.StrictMode;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

public final class Debug {
    private static final String TAG = "Veckify";

    static void initApplication(Application app) {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());
        } else {
            Crashlytics.start(app);
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

    public static void logSql(String sql) {
        if (BuildConfig.DEBUG) {
            Log.v(TAG, "SQL: " + sql);
        }
    }
}
