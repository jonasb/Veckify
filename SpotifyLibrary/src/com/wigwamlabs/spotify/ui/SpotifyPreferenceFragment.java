package com.wigwamlabs.spotify.ui;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

import com.wigwamlabs.spotify.R;

public class SpotifyPreferenceFragment extends PreferenceFragment {
    public static void onCreateSimple(PreferenceActivity activity) {
        activity.addPreferencesFromResource(R.xml.pref_spotify);

        final SummaryProvider summaryProvider = new SummaryProvider();
        summaryProvider.addPreference(activity.findPreference(activity.getString(R.string.preferenceKeyStreamingBitrate)));
        summaryProvider.addPreference(activity.findPreference(activity.getString(R.string.preferenceKeyOfflineBitrate)));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pref_spotify);

        final SummaryProvider summaryProvider = new SummaryProvider();
        summaryProvider.addPreference(findPreference(getString(R.string.preferenceKeyStreamingBitrate)));
        summaryProvider.addPreference(findPreference(getString(R.string.preferenceKeyOfflineBitrate)));
    }

    private static class SummaryProvider implements Preference.OnPreferenceChangeListener {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            final String value = (String) newValue;
            if (preference instanceof ListPreference) {
                final ListPreference listPreference = (ListPreference) preference;
                final int index = listPreference.findIndexOfValue(value);
                preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
            }
            return true;
        }

        public void addPreference(Preference preference) {
            preference.setOnPreferenceChangeListener(this);
            onPreferenceChange(preference, preference.getSharedPreferences().getString(preference.getKey(), ""));
        }
    }
}
