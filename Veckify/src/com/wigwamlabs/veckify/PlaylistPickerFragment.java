package com.wigwamlabs.veckify;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.wigwamlabs.spotify.Playlist;
import com.wigwamlabs.spotify.ui.PlaylistContainerAdapter;

class PlaylistPickerFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final MainActivity context = (MainActivity) getActivity();
        final PlaylistContainerAdapter adapter = new PlaylistContainerAdapter(context, context.getPlaylistContainer());
        return new AlertDialog.Builder(context)
                .setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        context.onPlaylistPicked((Playlist) adapter.getItem(which));
                    }
                })
                .create();
    }
}
