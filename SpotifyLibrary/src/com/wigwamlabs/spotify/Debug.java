package com.wigwamlabs.spotify;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

@SuppressWarnings({"ConstantIfStatement", "SameParameterValue"})
final class Debug {
    private static final String TAG = "SpotifyLibrary";

    static void logLifecycle(String msg) {
        if (BuildConfig.DEBUG) {
            Log.v(TAG, msg);
        }
    }

    public static void logAudioFocus(String msg) {
        if (BuildConfig.DEBUG) {
            Log.v(TAG, msg);
        }
    }

    public static void logMediaButton(String button) {
        if (BuildConfig.DEBUG) {
            Log.v(TAG, "Received media button: " + button);
        }
    }

    public static void logQueue(String msg) {
        if (BuildConfig.DEBUG) {
            Log.v(TAG, msg);
        }
    }

    public static void logAudioResponsivenessVerbose(String msg) {
        if (false) {
            Log.v(TAG, "Audio responsiveness: " + msg);
        }
    }

    public static void logAudioResponsiveness(String msg) {
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
}
