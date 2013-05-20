package com.wigwamlabs.veckify;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

import com.wigwamlabs.veckify.db.AlarmEntry;

public class TtsSettingsFragment extends DialogFragment {
    private static final String ARG_ALARM_ID = "alarmid";
    private static final String ARG_ALARM_ENTRY = "alarmentry";
    private CheckBox mTellTimeCheckbox;

    static TtsSettingsFragment create(long alarmId, AlarmEntry entry) {
        final TtsSettingsFragment fragment = new TtsSettingsFragment();
        final Bundle bundle = new Bundle();
        bundle.putLong(ARG_ALARM_ID, alarmId);
        bundle.putParcelable(ARG_ALARM_ENTRY, entry);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Activity activity = getActivity();
        final View view = LayoutInflater.from(activity).inflate(R.layout.dialog_tts, null, false);
        mTellTimeCheckbox = (CheckBox) view.findViewById(R.id.tellTimeCheckbox);

        final Bundle bundle = getArguments();
        final AlarmEntry entry = bundle.getParcelable(ARG_ALARM_ENTRY);

        mTellTimeCheckbox.setChecked(entry.getTellTime());

        return new AlertDialog.Builder(activity)
                .setView(view)
                .create();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        final MainActivity activity = (MainActivity) getActivity();
        if (activity == null) {
            // happens if fragment is destroyed, e.g. at screen rotation
            return;
        }

        final Bundle bundle = getArguments();
        final long alarmId = bundle.getLong(ARG_ALARM_ID);
        final AlarmEntry entry = bundle.getParcelable(ARG_ALARM_ENTRY);

        entry.setTellTime(mTellTimeCheckbox.isChecked());

        activity.onAlarmEntryChanged(alarmId, entry, true);
    }
}
