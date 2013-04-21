package com.wigwamlabs.veckify;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.wigwamlabs.spotify.Playlist;
import com.wigwamlabs.spotify.PlaylistContainer;
import com.wigwamlabs.spotify.ui.PlaylistContainerAdapter;

@SuppressWarnings("WeakerAccess")
public class PlaylistPickerFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final MainActivity activity = (MainActivity) getActivity();
        final PlaylistContainer playlistContainer = activity.getPlaylistContainer();
        final PlaylistContainerAdapter adapter = new PlaylistContainerAdapter(activity, playlistContainer);
        return new AlertDialog.Builder(activity)
                .setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activity.onPlaylistPicked((Playlist) adapter.getItem(which));
                    }
                })
                .create();
    }

    @Override
    public void onPause() {
        super.onDestroy();

        // workaround: dismiss dialog when paused since the activity's playlist container will be null when created,
        // so only support when the dialog is launched from the UI
        dismiss();
    }
}
