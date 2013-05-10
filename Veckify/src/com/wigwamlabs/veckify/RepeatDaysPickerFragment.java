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
import android.widget.ToggleButton;

import com.wigwamlabs.veckify.db.AlarmEntry;

public class RepeatDaysPickerFragment extends DialogFragment implements CompoundButton.OnCheckedChangeListener {
    private static final int[] REPEAT_DAY_IDS = new int[]{R.id.repeatDayMonday, R.id.repeatDayTuesday, R.id.repeatDayWednesday, R.id.repeatDayThursday, R.id.repeatDayFriday, R.id.repeatDaySaturday, R.id.repeatDaySunday};
    private static final String ARG_ALARM_ID = "alarmid";
    private static final String ARG_ALARM_ENTRY = "alarmentry";
    private final ToggleButton[] mRepeatDayToggles = new ToggleButton[REPEAT_DAY_IDS.length];
    private CheckBox mRepeatCheckbox;
    private TextView mRepeatSchedule;
    private boolean mUpdateUi;
    private View mRepeatToggles;

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

        //TODO ensure toggles follow locale's first weekday: Calendar.getFirstDayOfWeek()
        mRepeatToggles = view.findViewById(R.id.repeatToggles);
        for (int i = 0; i < REPEAT_DAY_IDS.length; i++) {
            mRepeatDayToggles[i] = (ToggleButton) view.findViewById(REPEAT_DAY_IDS[i]);
        }

        updateUi(repeatDays);

        return new AlertDialog.Builder(activity)
                .setView(view)
                .create();
    }

    private void updateUi(int repeatDays) {
        mUpdateUi = true;

        mRepeatSchedule.setText(AlarmUtils.repeatDaysText(getActivity(), repeatDays));

        mRepeatCheckbox.setChecked(repeatDays != AlarmUtils.DAYS_NONE);

        mRepeatToggles.setVisibility(repeatDays == AlarmUtils.DAYS_NONE ? View.GONE : View.VISIBLE);

        int day = 1;
        for (final ToggleButton toggle : mRepeatDayToggles) {
            toggle.setChecked((repeatDays & day) != 0);
            toggle.setOnCheckedChangeListener(this);
            day <<= 1;
        }

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

        final int repeatDays = getCurrentRepeatDays();

        final Bundle bundle = getArguments();
        final long alarmId = bundle.getLong(ARG_ALARM_ID);
        final AlarmEntry entry = bundle.getParcelable(ARG_ALARM_ENTRY);
        entry.setRepeatDays(repeatDays);

        activity.onAlarmEntryChanged(alarmId, entry, true);
    }

    private int getCurrentRepeatDays() {
        int repeatDays = 0;
        int day = 1;
        for (final ToggleButton toggle : mRepeatDayToggles) {
            if (toggle.isChecked()) {
                repeatDays |= day;
            }
            day <<= 1;
        }
        return repeatDays;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (mUpdateUi) {
            return;
        }

        final int repeatDays = getCurrentRepeatDays();
        updateUi(repeatDays);
    }
}
