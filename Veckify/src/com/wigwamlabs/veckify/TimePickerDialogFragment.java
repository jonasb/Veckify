package com.wigwamlabs.veckify;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.doomonafireball.betterpickers.timepicker.TimePicker;
import com.wigwamlabs.veckify.db.AlarmEntry;

@SuppressWarnings("WeakerAccess")
public class TimePickerDialogFragment extends DialogFragment {
    private static final String ARG_ALARM_ID = "alarmid";
    private static final String ARG_ALARM_ENTRY = "alarmentry";
    private AlarmEntry mEntry;
    private TimePicker mTimePicker;

    static TimePickerDialogFragment create(long alarmId, AlarmEntry entry) {
        final TimePickerDialogFragment fragment = new TimePickerDialogFragment();
        final Bundle bundle = new Bundle();
        bundle.putLong(ARG_ALARM_ID, alarmId);
        bundle.putParcelable(ARG_ALARM_ENTRY, entry);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Bundle bundle = getArguments();
        mEntry = bundle.getParcelable(ARG_ALARM_ENTRY);

        final Activity activity = getActivity();
        final View view = LayoutInflater.from(activity).inflate(R.layout.dialog_time, null, false);
        mTimePicker = (TimePicker) view.findViewById(R.id.time_picker);

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

        final int mins = mTimePicker.getHours() * 60 + mTimePicker.getMinutes();

        entry.setTime(mins / 60, mins % 60);

        activity.onAlarmEntryChanged(alarmId, entry, true);
    }
}
