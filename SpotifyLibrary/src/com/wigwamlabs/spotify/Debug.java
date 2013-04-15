package com.wigwamlabs.spotify;

import android.util.Log;

final class Debug {
    private static final String TAG = "SpotifyApp";

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
}
