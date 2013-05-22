package com.wigwamlabs.spotify;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

class Preferences implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String USERNAME = "username";
    private static final String CREDENTIALS_BLOB = "credentials_blob";
    private final SharedPreferences mPreferences;
    private final String mOfflineBitrateKey;
    private final String mStreamingBitrateKey;
    private final String mDownloadOverMobileKey;
    private Callback mCallback;

    public Preferences(Context context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mStreamingBitrateKey = context.getString(R.string.preferenceKeyStreamingBitrate);
        mOfflineBitrateKey = context.getString(R.string.preferenceKeyOfflineBitrate);
        mDownloadOverMobileKey = context.getString(R.string.preferenceKeyDownloadOverMobile);
    }

    public void setDefaultValues() {
        final SharedPreferences.Editor edit = mPreferences.edit();
        if (mPreferences.getString(mStreamingBitrateKey, null) == null) {
            edit.putString(mStreamingBitrateKey, "96");
        }
        if (mPreferences.getString(mOfflineBitrateKey, null) == null) {
            edit.putString(mOfflineBitrateKey, "96");
        }
        edit.apply();
    }

    public String getUsername() {
        return mPreferences.getString(USERNAME, null);
    }

    public void setUsername(String username) {
        mPreferences.edit()
                .putString(USERNAME, username)
                .putString(CREDENTIALS_BLOB, null)
                .apply();
    }

    public String getCredentialsBlob() {
        return mPreferences.getString(CREDENTIALS_BLOB, null);
    }

    public void setCredentialsBlob(String blob) {
        mPreferences.edit()
                .putString(CREDENTIALS_BLOB, blob)
                .apply();
    }

    public int getStreamingBitrate() {
        return bitrateStringToInt(mPreferences.getString(mStreamingBitrateKey, ""));
    }

    public int getOfflineBitrate() {
        return bitrateStringToInt(mPreferences.getString(mOfflineBitrateKey, ""));
    }

    private int bitrateStringToInt(String bitrateString) {
        int bitrate = Session.BITRATE_96K;
        if (bitrateString != null) {
            if ("96".equals(bitrateString)) {
                bitrate = Session.BITRATE_96K;
            } else if ("160".equals(bitrateString)) {
                bitrate = Session.BITRATE_160K;
            } else if ("320".equals(bitrateString)) {
                bitrate = Session.BITRATE_320K;
            }
        }
        return bitrate;
    }

    boolean getDownloadOverMobile() {
        return mPreferences.getBoolean(mDownloadOverMobileKey, false);
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
        if (mCallback != null) {
            mPreferences.registerOnSharedPreferenceChangeListener(this);
        } else {
            mPreferences.unregisterOnSharedPreferenceChangeListener(this);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (mCallback == null) {
            return;
        }

        if (mStreamingBitrateKey.equals(key)) {
            mCallback.onStreamingBitratePreferenceChanged(getStreamingBitrate());
        } else if (mOfflineBitrateKey.equals(key)) {
            mCallback.onOfflineBitratePreferenceChanged(getOfflineBitrate());
        } else if (mDownloadOverMobileKey.equals(key)) {
            mCallback.onConnectionRulesPreferenceChanged(getDownloadOverMobile());
        }
    }

    interface Callback {
        void onStreamingBitratePreferenceChanged(int bitrate);

        void onOfflineBitratePreferenceChanged(int bitrate);

        void onConnectionRulesPreferenceChanged(boolean downloadOverMobile);
    }
}
