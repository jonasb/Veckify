package com.wigwamlabs.spotify.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.wigwamlabs.spotify.Playlist;
import com.wigwamlabs.spotify.PlaylistContainer;
import com.wigwamlabs.spotify.R;
import com.wigwamlabs.spotify.Session;

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
        LinearLayout view = (LinearLayout) convertView;
        final ViewHolder viewHolder;
        if (view == null) {
            view = (LinearLayout) layoutInflater.inflate(R.layout.playlistcontainer_playlist_offline, parent, false);
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
        viewHolder.playlist = playlist;
        mUpdatesUi = true;
        viewHolder.name.setText(playlist.getName());
        final int offlineStatus = playlist.getOfflineStatus(mSession);
        viewHolder.offlineSwitch.setChecked(offlineStatus != Playlist.OFFLINE_STATUS_NO);
        mUpdatesUi = false;

        return view;
    }

    private void onOfflineStateChanged(Playlist playlist, boolean offline) {
        playlist.setOfflineMode(mSession, offline);
    }

    private static class ViewHolder {
        public final TextView name;
        public final Switch offlineSwitch;
        public Playlist playlist;

        public ViewHolder(LinearLayout view) {
            name = (TextView) view.findViewById(R.id.name);
            offlineSwitch = (Switch) view.findViewById(R.id.offlineSwitch);
        }
    }
}
