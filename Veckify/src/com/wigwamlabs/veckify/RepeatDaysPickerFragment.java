package com.wigwamlabs.veckify;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.wigwamlabs.veckify.db.AlarmEntry;

public class RepeatDaysPickerFragment extends DialogFragment {
    private static final String ARG_ALARM_ID = "alarmid";
    private static final String ARG_ALARM_ENTRY = "alarmentry";
    private CheckBox mRepeatCheckbox;
    private TextView mRepeatSchedule;
    private boolean mUpdateUi;
    private DayPicker mDayPicker;

    public static RepeatDaysPickerFragment create(long alarmId, AlarmEntry entry) {
        final RepeatDaysPickerFragment fragment = new RepeatDaysPickerFragment();
        final Bundle bundle = new Bundle();
        bundle.putLong(ARG_ALARM_ID, alarmId);
        bundle.putParcelable(ARG_ALARM_ENTRY, entry);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final MainActivity activity = (MainActivity) getActivity();
        final Bundle bundle = getArguments();
        final AlarmEntry entry = bundle.getParcelable(ARG_ALARM_ENTRY);
        final int repeatDays = entry.getRepeatDays();

        final View view = LayoutInflater.from(activity).inflate(R.layout.dialog_repeat_days, null, false);

        mRepeatSchedule = (TextView) view.findViewById(R.id.repeatSchedule);

        mRepeatCheckbox = (CheckBox) view.findViewById(R.id.repeatCheckBox);
        mRepeatCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mUpdateUi) {
                    return;
                }
                updateUi(isChecked ? AlarmUtils.DAYS_ALL : AlarmUtils.DAYS_NONE);
            }
        });

        mDayPicker = (DayPicker) view.findViewById(R.id.dayPicker);
        mDayPicker.setDaysChangedListener(new DayPicker.OnDaysChangedListener() {
            @Override
            public void onDaysChanged(DayPicker dayPicker, int days, boolean fromUser) {
                if (fromUser) {
                    updateUi(days);
                }
            }
        });

        updateUi(repeatDays);

        return new AlertDialog.Builder(activity)
                .setView(view)
                .create();
    }

    private void updateUi(int repeatDays) {
        mUpdateUi = true;

        mRepeatSchedule.setText(AlarmUtils.repeatDaysText(getActivity(), repeatDays));

        mRepeatCheckbox.setChecked(repeatDays != AlarmUtils.DAYS_NONE);

        mDayPicker.setVisibility(repeatDays == AlarmUtils.DAYS_NONE ? View.GONE : View.VISIBLE);
        mDayPicker.setDays(repeatDays);

        mUpdateUi = false;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        final MainActivity activity = (MainActivity) getActivity();
        if (activity == null) {
            // happens if fragment is destroyed, e.g. at screen rotation
            return;
        }

        final int repeatDays = mDayPicker.getDays();

        final Bundle bundle = getArguments();
        final long alarmId = bundle.getLong(ARG_ALARM_ID);
        final AlarmEntry entry = bundle.getParcelable(ARG_ALARM_ENTRY);
        entry.setRepeatDays(repeatDays);

        activity.onAlarmEntryChanged(alarmId, entry, true);
    }
}
