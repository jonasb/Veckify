package com.wigwamlabs.spotify.ui;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.wigwamlabs.spotify.Playlist;
import com.wigwamlabs.spotify.Track;

public class PlaylistAdapter implements ListAdapter, Playlist.Callback {
    private final Context mContext;
    private final Playlist mPlaylist;
    private DataSetObserver mObserver;

    public PlaylistAdapter(Context context, Playlist playlist) {
        mContext = context;
        mPlaylist = playlist;
        mPlaylist.setCallback(this);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        mObserver = observer;
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        if (mObserver == observer) {
            mObserver = null;
        }
    }

    @Override
    public int getCount() {
        return mPlaylist.getCount();
    }

    @Override
    public Track getItem(int position) {
        return mPlaylist.getItem(position);
    }

    @Override
    public long getItemId(int position) {
        final Track item = mPlaylist.getItem(position);
        return item.getId();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView view = (TextView) convertView;
        if (view == null) {
            view = new TextView(mContext);
            view.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        final Track item = getItem(position);
        view.setText(item.getName() + ": " + item.getArtistsString());
        return view;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return mPlaylist.getCount() == 0;
    }

    @Override
    public void onPlaylistUpdateInProgress(boolean done) {
        if (done) {
            if (mObserver != null) {
                mObserver.onChanged();
            }
        }
    }

    @Override
    public void onPlaylistRenamed() {
    }
}
