package com.wigwamlabs.spotify;

import android.util.Log;

import com.wigwamlabs.spotify.app.BuildConfig;

final class Debug {
    private static final String TAG = "SpotifyApp";

    static void logLifecycle(String msg) {
        if (BuildConfig.DEBUG) {
            Log.v(TAG, msg);
        }
    }
}
