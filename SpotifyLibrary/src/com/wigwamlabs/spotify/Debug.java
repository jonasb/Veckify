package com.wigwamlabs.spotify;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

@SuppressWarnings({"ConstantIfStatement", "SameParameterValue"})
public final class Debug {
    private static final String TAG = "SpotifyLibrary";

    static void logAudioFocus(String msg) {
        if (BuildConfig.DEBUG) {
            Log.v(TAG, msg);
        }
    }

    static void logMediaButton(String button) {
        if (BuildConfig.DEBUG) {
            Log.v(TAG, "Received media button: " + button);
        }
    }

    static void logQueue(String msg) {
        if (BuildConfig.DEBUG) {
            Log.v(TAG, msg);
        }
    }

    static void logAudioResponsivenessVerbose(String msg) {
        if (false) {
            Log.v(TAG, "Audio responsiveness: " + msg);
        }
    }

    static void logAudioResponsiveness(String msg) {
        if (BuildConfig.DEBUG) {
            Log.v(TAG, "Audio responsiveness: " + msg);
        }
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
        if (BuildConfig.DEBUG) {
            Log.v(TAG, "Foreground notifications: " + msg);
        }
    }

    static void logLifecycle(String msg) {
        if (BuildConfig.DEBUG) {
            Log.v(TAG, msg);
        }
    }

    static void logTts(String msg) {
        if (BuildConfig.DEBUG) {
            Log.v(TAG, "TTS: " + msg);
        }
    }

    static void logImageProvider(String msg, Exception e) {
        Log.w(TAG, "Image provider: " + msg, e);
    }

    static void logImageProvider(String msg) {
        if (BuildConfig.DEBUG) {
            Log.v(TAG, "Image provider: " + msg);
        }
    }

    public static void logBitmapCache(String msg) {
        if (BuildConfig.DEBUG) {
            Log.v(TAG, "Bitmap cache: " + msg);
        }
    }
}
