package com.wigwamlabs.spotify;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

@SuppressWarnings({"ConstantIfStatement", "SameParameterValue"})
public final class Debug {
    private enum AndroidLog {
        OFF,
        DEBUG_ONLY,
    }

    private enum ServerLog {
        ON,
        OFF,
    }

    private static final String TAG = "SpotifyLibrary";
    private static final boolean CRASHLYTICS_ENABLED = !BuildConfig.DEBUG;

    static void logAudioFocus(String msg) {
        log(AndroidLog.DEBUG_ONLY, ServerLog.ON, msg);
    }

    static void logMediaButton(String button) {
        log(AndroidLog.DEBUG_ONLY, ServerLog.ON, "Received media button: " + button);
    }

    static void logQueue(String msg) {
        log(AndroidLog.DEBUG_ONLY, ServerLog.ON, msg);
    }

    static void logAudioResponsivenessVerbose(String msg) {
        log(AndroidLog.OFF, ServerLog.OFF, "Audio responsiveness: " + msg);
    }

    static void logAudioResponsiveness(String msg) {
        log(AndroidLog.DEBUG_ONLY, ServerLog.ON, "Audio responsiveness: " + msg);
    }

    static void notifyAudioUnresponsive(Context context, String title, String contentText) {
        if (BuildConfig.DEBUG) {
            final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            final NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.ic_stat_player)
                    .setContentTitle(title)
                    .setContentText(contentText);
            notificationManager.notify(R.id.actionLogin, builder.build());
        }
    }

    static void logForegroundNotification(String msg) {
        log(AndroidLog.DEBUG_ONLY, ServerLog.ON, "Foreground notifications: " + msg);
    }

    static void logLifecycle(String msg) {
        log(AndroidLog.DEBUG_ONLY, ServerLog.ON, msg);
    }

    static void logTts(String msg) {
        log(AndroidLog.DEBUG_ONLY, ServerLog.ON, "TTS: " + msg);
    }

    static void logImageProvider(String msg, Exception e) {
        log(AndroidLog.DEBUG_ONLY, ServerLog.ON, "Image provider: " + msg, e);
    }

    static void logImageProvider(String msg) {
        log(AndroidLog.DEBUG_ONLY, ServerLog.ON, "Image provider: " + msg);
    }

    public static void logBitmapCache(String msg) {
        log(AndroidLog.DEBUG_ONLY, ServerLog.ON, "Bitmap cache: " + msg);
    }

    @Deprecated
    public static void logTemp(String msg) {
        log(AndroidLog.DEBUG_ONLY, ServerLog.OFF, "XXX: " + msg);
    }

    private static void log(AndroidLog androidLog, ServerLog serverLog, String event) {
        if (androidLog == AndroidLog.DEBUG_ONLY && BuildConfig.DEBUG) {
            Log.d(TAG, event);
        }
        if (CRASHLYTICS_ENABLED && serverLog == ServerLog.ON) {
            Crashlytics.log(event);
        }
    }

    private static void log(AndroidLog androidLog, ServerLog serverLog, String event, Throwable exception) {
        if (androidLog == AndroidLog.DEBUG_ONLY && BuildConfig.DEBUG) {
            Log.w(TAG, event, exception);
        }
        if (CRASHLYTICS_ENABLED && serverLog == ServerLog.ON) {
            Crashlytics.logException(exception);
        }
    }
}
