package com.wigwamlabs.spotify;

import android.content.Context;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;

import proguard.annotation.Keep;
import proguard.annotation.KeepName;

public class SpotifySession {
    public static final int CONNECTION_STATE_LOGGED_OUT = 0;
    public static final int CONNECTION_STATE_LOGGED_IN = 1;
    public static final int CONNECTION_STATE_DISCONNECTED = 2;
    public static final int CONNECTION_STATE_UNDEFINED = 3;
    public static final int CONNECTION_STATE_OFFLINE = 4;
    private static final Handler mHandler = new Handler();
    @KeepName
    private int mHandle;
    private int mState;
    private Callback mCallback;

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

    public boolean relogin() {
        return nativeRelogin();
    }

    public void login(String username, String password, boolean rememberMe) {
        nativeLogin(username, password, rememberMe);
    }

    public PlaylistContainer getPlaylistContainer() {
        int handle = nativeGetPlaylistContainer();
        if (handle == 0) {
            return null;
        }
        return new PlaylistContainer(handle);
    }

    private native int nativeCreate(SpotifyContext spotifyContext, String settingsPath, String cachePath, String deviceId);

    private native void nativeDestroy();

    private native boolean nativeRelogin();

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    private native void nativeLogin(String username, String password, boolean rememberMe);

    private native int nativeGetPlaylistContainer();

    @Keep
    void onMetadataUpdated() {
        Log.d("XXX", "onMetadataUpdated()");
    }

    @Keep
    void onConnectionStateUpdated(int state) {
        mState = state;
        if (mCallback == null) {
            return;
        }
        mHandler.post(new Runnable() {
            public void run() {
                if (mCallback != null) {
                    mCallback.onConnectionStateUpdated(mState);
                }
            }
        });
    }

    static {
        nativeInitClass();
    }

    public interface Callback {
        void onConnectionStateUpdated(int state);
    }
}
