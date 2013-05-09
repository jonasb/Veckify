package com.wigwamlabs.veckify;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import com.wigwamlabs.veckify.db.AlarmEntry;

@SuppressWarnings("WeakerAccess")
public class TimePickerDialogFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
    private static final String ARG_ALARM_ID = "alarmid";
    private static final String ARG_HOUR = "hour";
    private static final String ARG_MINUTE = "minute";

    static TimePickerDialogFragment create(long alarmId, int hour, int minute) {
        final TimePickerDialogFragment fragment = new TimePickerDialogFragment();
        final Bundle bundle = new Bundle();
        bundle.putLong(ARG_ALARM_ID, alarmId);
        bundle.putInt(ARG_HOUR, hour);
        bundle.putInt(ARG_MINUTE, minute);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Bundle bundle = getArguments();
        final int hour = bundle.getInt(ARG_HOUR);
        final int minute = bundle.getInt(ARG_MINUTE);

        final Activity activity = getActivity();
        return new TimePickerDialog(activity, this, hour, minute, DateFormat.is24HourFormat(activity));
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        final Bundle bundle = getArguments();
        final long alarmId = bundle.getLong(ARG_ALARM_ID);

        final AlarmEntry entry = new AlarmEntry();
//        entry.setEnabled(true); //TODO is enablable?
        entry.setTime(hourOfDay, minute);

        ((MainActivity) getActivity()).onAlarmEntryChanged(alarmId, entry);
    }
}
