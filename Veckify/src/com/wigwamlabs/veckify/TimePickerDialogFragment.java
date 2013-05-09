package com.wigwamlabs.veckify;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import com.wigwamlabs.veckify.db.AlarmEntry;

@SuppressWarnings("WeakerAccess")
public class TimePickerDialogFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
    private static final String ARG_ALARM_ID = "alarmid";
    private static final String ARG_ALARM_ENTRY = "alarmentry";
    private AlarmEntry mEntry;

    static TimePickerDialogFragment create(long alarmId, AlarmEntry entry) {
        final TimePickerDialogFragment fragment = new TimePickerDialogFragment();
        final Bundle bundle = new Bundle();
        bundle.putLong(ARG_ALARM_ID, alarmId);
        bundle.putParcelable(ARG_ALARM_ENTRY, entry.getValues());
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Bundle bundle = getArguments();
        mEntry = new AlarmEntry((ContentValues) bundle.getParcelable(ARG_ALARM_ENTRY));

        final Activity activity = getActivity();
        return new TimePickerDialog(activity, this, mEntry.getHour(), mEntry.getMinute(), DateFormat.is24HourFormat(activity));
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // this method is called twice sometimes by TimePickerDialog,
        // so check that the value has changed to prevent unnecessary db writes
        if (hourOfDay != mEntry.getHour() || minute != mEntry.getMinute()) {
            mEntry.setTime(hourOfDay, minute);
            //        entry.setEnabled(true); //TODO is enablable?

            final Bundle bundle = getArguments();
            final long alarmId = bundle.getLong(ARG_ALARM_ID);
            ((MainActivity) getActivity()).onAlarmEntryChanged(alarmId, mEntry);
        }
    }
}
