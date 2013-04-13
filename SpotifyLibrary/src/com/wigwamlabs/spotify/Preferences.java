package com.wigwamlabs.spotify;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

class Preferences {
    private static final String USERNAME = "username";
    private static final String CREDENTIALS_BLOB = "credentials_blob";
    private final SharedPreferences mPreferences;

    public Preferences(Context context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
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
}
