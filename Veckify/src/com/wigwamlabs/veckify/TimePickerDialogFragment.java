package com.wigwamlabs.veckify;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import com.wigwamlabs.veckify.alarms.Alarm;

@SuppressWarnings("WeakerAccess")
public class TimePickerDialogFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final MainActivity activity = (MainActivity) getActivity();
        final Alarm alarm = activity.getAlarm();

        return new TimePickerDialog(getActivity(), this, alarm.getHour(), alarm.getMinute(), DateFormat.is24HourFormat(activity));
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        ((MainActivity) getActivity()).onAlarmTimeSet(hourOfDay, minute);
    }
}
