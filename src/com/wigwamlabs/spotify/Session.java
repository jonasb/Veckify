package com.wigwamlabs.spotify;

import android.content.Context;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;

import proguard.annotation.Keep;

import java.util.ArrayList;

public class Session extends NativeItem {
    public static final int CONNECTION_STATE_LOGGED_OUT = 0;
    public static final int CONNECTION_STATE_LOGGED_IN = 1;
    public static final int CONNECTION_STATE_DISCONNECTED = 2;
    public static final int CONNECTION_STATE_UNDEFINED = 3;
    public static final int CONNECTION_STATE_OFFLINE = 4;
    private static final Handler mHandler = new Handler();
    private int mState;
    private final ArrayList<Callback> mCallbacks = new ArrayList<Callback>();
    private Player mPlayer;

    public Session(Context context, String settingsPath, String cachePath, String deviceId) {
        super(0);
        if (settingsPath == null) {
            settingsPath = context.getFilesDir().getAbsolutePath();
        }
        if (cachePath == null) {
            cachePath = context.getCacheDir().getAbsolutePath();
        }
        if (deviceId == null) {
            deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        }
        setHandle(nativeCreate(settingsPath, cachePath, deviceId));
    }

    private static native void nativeInitClass();

    @Override
    public void destroy() {
        super.destroy();

        if (mPlayer != null) {
            mPlayer.destroy();
            mPlayer = null;
        }
    }

    public boolean relogin() {
        return nativeRelogin();
    }

    public void login(String username, String password, boolean rememberMe) {
        nativeLogin(username, password, rememberMe);
    }

    public PlaylistContainer getPlaylistContainer() {
        final int handle = nativeGetPlaylistContainer();
        if (handle == 0) {
            return null;
        }
        return new PlaylistContainer(handle);
    }

    private native int nativeCreate(String settingsPath, String cachePath, String deviceId);

    @Override
    native void nativeDestroy();

    private native int nativeGetConnectionState();

    private native boolean nativeRelogin();

    public void addCallback(Callback callback, boolean callbackNow) {
        if (mCallbacks.contains(callback)) {
            return;
        }
        mCallbacks.add(callback);
        if (callbackNow) {
            callback.onConnectionStateUpdated(nativeGetConnectionState());
        }
    }

    public void removeCallback(Callback callback) {
        mCallbacks.remove(callback);
    }

    private native void nativeLogin(String username, String password, boolean rememberMe);

    private native int nativeGetPlaylistContainer();

    private native int nativeGetPlayer();

    public Player getPlayer() {
        if (mPlayer == null) {
            final int handle = nativeGetPlayer();
            mPlayer = new Player(handle);
        }
        return mPlayer;
    }

    @Keep
    void onMetadataUpdated() {
        Log.d("XXX", "onMetadataUpdated()");
    }

    public int getConnectionState() {
        return nativeGetConnectionState();
    }

    @Keep
    void onConnectionStateUpdated(int state) {
        mState = state;
        if (mCallbacks.isEmpty()) {
            return;
        }
        mHandler.post(new Runnable() {
            public void run() {
                for (Callback callback : mCallbacks) {
                    callback.onConnectionStateUpdated(mState);
                }
            }
        });
    }

    static {
        System.loadLibrary("spotify");
        System.loadLibrary("spotify-jni");

        nativeInitClass();
    }

    public interface Callback {
        void onConnectionStateUpdated(int state);
    }
}
