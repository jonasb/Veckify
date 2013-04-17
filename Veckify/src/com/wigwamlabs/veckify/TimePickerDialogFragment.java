package com.wigwamlabs.veckify;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;

public class TimePickerDialogFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MainActivity activity = (MainActivity) getActivity();

        return new TimePickerDialog(getActivity(), this, activity.getAlarmHour(), activity.getAlarmMinute(), DateFormat.is24HourFormat(activity));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        ((MainActivity) getActivity()).onAlarmTimeSet(hourOfDay, minute);
    }
}
