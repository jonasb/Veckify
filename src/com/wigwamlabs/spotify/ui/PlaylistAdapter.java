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

    public boolean areAllItemsEnabled() {
        return true;
    }

    public boolean isEnabled(int position) {
        return true;
    }

    public void registerDataSetObserver(DataSetObserver observer) {
        mObserver = observer;
    }

    public void unregisterDataSetObserver(DataSetObserver observer) {
        if (mObserver == observer) {
            mObserver = null;
        }
    }

    public int getCount() {
        return mPlaylist.getCount();
    }

    public Track getItem(int position) {
        return mPlaylist.getItem(position);
    }

    public long getItemId(int position) {
        final Track item = mPlaylist.getItem(position);
        return item.getId();
    }

    public boolean hasStableIds() {
        return true;
    }

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

    public int getItemViewType(int position) {
        return 0;
    }

    public int getViewTypeCount() {
        return 1;
    }

    public boolean isEmpty() {
        return mPlaylist.getCount() == 0;
    }

    public void onPlaylistUpdateInProgress(boolean done) {
        if (done) {
            if (mObserver != null) {
                mObserver.onChanged();
            }
        }
    }

    public void onPlaylistRenamed() {
    }
}
