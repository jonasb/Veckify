package com.wigwamlabs.veckify;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;

import com.wigwamlabs.veckify.db.AlarmEntry;

@SuppressWarnings("WeakerAccess")
public class VolumePickerFragment extends DialogFragment {
    private static final String ARG_ALARM_ID = "alarmid";
    private static final String ARG_ALARM_ENTRY = "alarmentry";
    private SeekBar mSeekbar;

    static VolumePickerFragment create(long alarmId, AlarmEntry entry) {
        final VolumePickerFragment fragment = new VolumePickerFragment();
        final Bundle bundle = new Bundle();
        bundle.putLong(ARG_ALARM_ID, alarmId);
        bundle.putParcelable(ARG_ALARM_ENTRY, entry);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Bundle bundle = getArguments();
        final AlarmEntry entry = bundle.getParcelable(ARG_ALARM_ENTRY);

        final Activity activity = getActivity();

        final View view = LayoutInflater.from(activity).inflate(R.layout.dialog_volume, null, false);
        mSeekbar = (SeekBar) view.findViewById(R.id.seekbar);

        final Integer volume = entry.getVolume();
        if (volume != null) {
            mSeekbar.setProgress(volume.intValue());
        } else {
            mSeekbar.setProgress(100);
        }

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
        entry.setVolume(mSeekbar.getProgress());

        activity.onAlarmEntryChanged(alarmId, entry, true);
    }
}
