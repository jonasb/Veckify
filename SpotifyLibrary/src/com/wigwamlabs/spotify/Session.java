package com.wigwamlabs.spotify;

import android.content.Context;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;

import proguard.annotation.Keep;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;

public class Session extends NativeItem {
    public static final int CONNECTION_STATE_LOGGED_OUT = 0;
    public static final int CONNECTION_STATE_LOGGED_IN = 1;
    public static final int CONNECTION_STATE_DISCONNECTED = 2;
    public static final int CONNECTION_STATE_UNDEFINED = 3;
    public static final int CONNECTION_STATE_OFFLINE = 4;
    private static final Handler mHandler = new Handler();
    private final Context mContext;
    private final Preferences mPreferences;
    private final ArrayList<Callback> mCallbacks = new ArrayList<Callback>();
    private int mState;
    private Player mPlayer;
    private ImageProvider mImageProvider;
    private int mSyncApproxTotalTracks = 0;
    private int mSyncLastTrackDownloaded;
    private boolean mLastSyncStatus;

    public Session(Context context, String settingsPath, String cachePath, String deviceId) {
        super(0);
        mContext = context;
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
        mPreferences = new Preferences(context);
    }

    private static native void nativeInitClass();

    @Override
    public void destroy() {
        super.destroy();

        if (mPlayer != null) {
            mPlayer.destroy();
            mPlayer = null;
        }

        if (mImageProvider != null) {
            mImageProvider.destroy();
            mImageProvider = null;
        }
    }

    public boolean relogin() {
        // libspotify's relogin support is not reliable so we store the credentials ourselves
        final String username = mPreferences.getUsername();
        final String blob = mPreferences.getCredentialsBlob();

        if (username != null && blob != null) {
            nativeLogin(username, null, blob);
            return true;
        }
        return false;
    }

    public void login(String username, String password) {
        nativeLogin(username, password, null);

        mPreferences.setUsername(username);
    }

    public void logout() {
        nativeLogout();
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

    public void addCallback(Callback callback, boolean callbackNow) {
        if (mCallbacks.contains(callback)) {
            return;
        }
        mCallbacks.add(callback);
        if (callbackNow) {
            callback.onConnectionStateUpdated(nativeGetConnectionState());
            callback.onOfflineTracksToSyncChanged(mLastSyncStatus, mSyncApproxTotalTracks - mSyncLastTrackDownloaded, mSyncApproxTotalTracks);
        }
    }

    public void removeCallback(Callback callback) {
        mCallbacks.remove(callback);
    }

    private native void nativeLogin(String username, String password, String blob);

    private native void nativeLogout();

    private native int nativeGetPlaylistContainer();

    private native int nativeGetPlayer();

    public Player getPlayer() {
        if (mPlayer == null) {
            final int handle = nativeGetPlayer();
            mPlayer = new Player(mContext, handle, getImageProvider());
        }
        return mPlayer;
    }

    public ImageProvider getImageProvider() {
        if (mImageProvider == null) {
            mImageProvider = new ImageProvider(this);
        }
        return mImageProvider;
    }

    @Keep
    void onLoggedIn(final int error) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 100; i++) {
                    try {
                        for (Callback callback : mCallbacks) {
                            callback.onLoggedIn(error);
                        }
                        return;
                    } catch (ConcurrentModificationException e) {
                        // retry
                    }
                }
            }
        });
    }

    @Keep
    void onMetadataUpdated() {
        Log.d("XXX", "onMetadataUpdated()");
    }

    @Keep
    void onCredentialsBlobUpdated(final String blob) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mPreferences.setCredentialsBlob(blob);
            }
        });
    }

    public int getConnectionState() {
        return nativeGetConnectionState();
    }

    @Keep
    void onConnectionStateUpdated(int state) {
        mState = state;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                for (Callback callback : mCallbacks) {
                    callback.onConnectionStateUpdated(mState);
                }
            }
        });
    }

    @Keep
    private void onOfflineTracksToSyncChanged(final boolean syncing, final int remainingTracks) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mLastSyncStatus = syncing;
                // deal with tracks added to queue
                if (remainingTracks > mSyncApproxTotalTracks) {
                    mSyncApproxTotalTracks = remainingTracks + mSyncLastTrackDownloaded;
                }

                //
                if (remainingTracks == 0) {
                    mSyncApproxTotalTracks = 0;
                    mSyncLastTrackDownloaded = 0;
                } else {
                    mSyncLastTrackDownloaded = mSyncApproxTotalTracks - remainingTracks;
                }

                for (Callback callback : mCallbacks) {
                    callback.onOfflineTracksToSyncChanged(syncing, remainingTracks, mSyncApproxTotalTracks);
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
        void onLoggedIn(int error);

        void onConnectionStateUpdated(int state);

        void onOfflineTracksToSyncChanged(boolean syncing, int remainingTracks, int approxTotalTracks);
    }
}
