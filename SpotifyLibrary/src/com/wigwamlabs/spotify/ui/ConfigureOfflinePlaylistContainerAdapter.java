package com.wigwamlabs.spotify.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import com.wigwamlabs.spotify.Playlist;
import com.wigwamlabs.spotify.PlaylistContainer;
import com.wigwamlabs.spotify.R;
import com.wigwamlabs.spotify.Session;

import static com.wigwamlabs.spotify.Playlist.OFFLINE_STATUS_DOWNLOADING;
import static com.wigwamlabs.spotify.Playlist.OFFLINE_STATUS_NO;

public class ConfigureOfflinePlaylistContainerAdapter extends PlaylistContainerAdapter {
    private final Session mSession;
    private boolean mUpdatesUi;

    public ConfigureOfflinePlaylistContainerAdapter(Context context, Session session, PlaylistContainer playlistContainer) {
        super(context, playlistContainer);
        mSession = session;
    }

    @Override
    protected View getPlaylistView(Playlist playlist, View convertView, ViewGroup parent, LayoutInflater layoutInflater) {
        // initialise view
        ViewGroup view = (ViewGroup) convertView;
        final ViewHolder viewHolder;
        if (view == null) {
            view = (ViewGroup) layoutInflater.inflate(R.layout.playlistcontainer_playlist_offline, parent, false);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);

            viewHolder.offlineSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (mUpdatesUi) {
                        return;
                    }
                    onOfflineStateChanged(viewHolder.playlist, isChecked);
                }
            });
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        // update view
        mUpdatesUi = true;

        viewHolder.playlist = playlist;
        viewHolder.name.setText(playlist.getName());

        final int status = playlist.getOfflineStatus(mSession);
        //TODO the status for OFFLINE_STATUS_WAITING is really unreliable, so treat waiting and yes as the same
        viewHolder.offlineSwitch.setChecked(status != OFFLINE_STATUS_NO);
        viewHolder.syncProgress.setVisibility(status == OFFLINE_STATUS_DOWNLOADING ? View.VISIBLE : View.GONE);
        if (status == OFFLINE_STATUS_DOWNLOADING) {
            viewHolder.syncProgress.setProgress(playlist.getOfflineDownloadComplete(mSession));
        }

        mUpdatesUi = false;

        return view;
    }

    private void onOfflineStateChanged(Playlist playlist, boolean offline) {
        playlist.setOfflineMode(mSession, offline);
    }

    private static class ViewHolder {
        final TextView name;
        final Switch offlineSwitch;
        final ProgressBar syncProgress;
        Playlist playlist;

        public ViewHolder(ViewGroup view) {
            name = (TextView) view.findViewById(R.id.name);
            offlineSwitch = (Switch) view.findViewById(R.id.offlineSwitch);
            syncProgress = (ProgressBar) view.findViewById(R.id.syncProgress);
        }
    }
}
