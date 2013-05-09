package com.wigwamlabs.veckify;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.wigwamlabs.spotify.Playlist;
import com.wigwamlabs.spotify.PlaylistContainer;
import com.wigwamlabs.spotify.ui.PlaylistContainerAdapter;
import com.wigwamlabs.veckify.db.AlarmEntry;

@SuppressWarnings("WeakerAccess")
public class PlaylistPickerFragment extends DialogFragment {
    private static final String ARG_ALARM_ID = "alarm id";
    private static final String ARG_SELECTED_PLAYLIST = "selected playlist";

    static PlaylistPickerFragment create(long alarmId, String selectedPlaylistLink) {
        final PlaylistPickerFragment fragment = new PlaylistPickerFragment();
        final Bundle bundle = new Bundle();
        bundle.putLong(ARG_ALARM_ID, alarmId);
        bundle.putString(ARG_SELECTED_PLAYLIST, selectedPlaylistLink);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final MainActivity activity = (MainActivity) getActivity();
        final PlaylistContainer playlistContainer = activity.getPlaylistContainer();
        final Bundle bundle = getArguments();
        final long alarmId = bundle.getLong(ARG_ALARM_ID);
        final String selectedPlaylist = bundle.getString(ARG_SELECTED_PLAYLIST);
        final int selectedPlaylistIndex = playlistContainer.findPlaylistIndex(selectedPlaylist);

        final PlaylistContainerAdapter adapter = new PlaylistContainerAdapter(activity, playlistContainer);
        return new AlertDialog.Builder(activity)
                .setSingleChoiceItems(adapter, selectedPlaylistIndex, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onPicked(activity, alarmId, (Playlist) adapter.getItem(which));
                        dialog.dismiss();
                    }
                })
                .create();
    }

    private void onPicked(MainActivity activity, long alarmId, Playlist playlist) {
        final AlarmEntry entry = new AlarmEntry();
        if (playlist != null) {
//            entry.setEnabled(true); //TODO is enablable?
            entry.setPlaylistLink(playlist.getLink());
            entry.setPlaylistName(playlist.getName());
        } else {
            entry.setPlaylistLink(null);
            entry.setPlaylistName(null);
        }

        activity.onAlarmEntryChanged(alarmId, entry);
    }

    @Override
    public void onPause() {
        super.onDestroy();

        // workaround: dismiss dialog when paused since the activity's playlist container will be null when created,
        // so only support when the dialog is launched from the UI
        dismiss();
    }
}
