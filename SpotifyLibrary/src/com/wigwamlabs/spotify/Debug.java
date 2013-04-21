package com.wigwamlabs.spotify;

import android.util.Log;

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
}
