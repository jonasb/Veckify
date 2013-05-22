package com.wigwamlabs.veckify;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;


public class LegalPreferenceFragment extends PreferenceFragment {
    public static void onCreateSimple(PreferenceActivity activity) {
        activity.addPreferencesFromResource(R.xml.pref_legal);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pref_legal);
    }
}
