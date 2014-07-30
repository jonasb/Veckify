package com.wigwamlabs.veckify;

import android.os.StrictMode;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

public final class Debug {
    private enum AndroidLog {
        DEBUG_ONLY
    }

    private enum ServerLog {
        ON,
        OFF,
    }

    private static final String TAG = "Veckify";
    private static final boolean CRASHLYTICS_ENABLED = !BuildConfig.DEBUG;

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
        }
        if (CRASHLYTICS_ENABLED) {
            Crashlytics.start(app);
        }
    }

    public static void logAlarmScheduling(String msg) {
        log(AndroidLog.DEBUG_ONLY, ServerLog.ON, msg);
    }

    static void logLifecycle(String event) {
        log(AndroidLog.DEBUG_ONLY, ServerLog.ON, event);
    }

    public static void logSql(String sql) {
        log(AndroidLog.DEBUG_ONLY, ServerLog.OFF, "SQL: " + sql);
    }

    private static void log(AndroidLog androidLog, ServerLog serverLog, String event) {
        if (androidLog == AndroidLog.DEBUG_ONLY && BuildConfig.DEBUG) {
            Log.d(TAG, event);
        }
        if (CRASHLYTICS_ENABLED && serverLog == ServerLog.ON) {
            Crashlytics.log(event);
        }
    }

}
