package com.wigwamlabs.spotify.app;

import android.content.Context;
import android.provider.Settings;

public class SpotifySession {
    static {
        nativeInitClass();
    }

    private int mHandle;

    public SpotifySession(Context context, SpotifyContext spotifyContext, String settingsPath, String cachePath, String deviceId) {
        if (settingsPath == null) {
            settingsPath = context.getFilesDir().getAbsolutePath();
        }
        if (cachePath == null) {
            cachePath = context.getCacheDir().getAbsolutePath();
        }
        if (deviceId == null) {
            deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        }
        mHandle = nativeCreate(spotifyContext, settingsPath, cachePath, deviceId);
    }

    private static native void nativeInitClass();

    public void destroy() {
        if (mHandle != 0) {
            nativeDestroy();
            mHandle = 0;
        }
    }

    private native int nativeCreate(SpotifyContext spotifyContext, String settingsPath, String cachePath, String deviceId);

    private native void nativeDestroy();
}
