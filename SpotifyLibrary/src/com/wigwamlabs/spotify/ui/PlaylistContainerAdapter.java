package com.wigwamlabs.spotify.ui;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.wigwamlabs.spotify.FolderEnd;
import com.wigwamlabs.spotify.FolderStart;
import com.wigwamlabs.spotify.NativeItem;
import com.wigwamlabs.spotify.Playlist;
import com.wigwamlabs.spotify.PlaylistContainer;

public class PlaylistContainerAdapter implements ListAdapter, SpinnerAdapter, PlaylistContainer.Callback, Playlist.Callback {
    private final Context mContext;
    private final PlaylistContainer mContainer;
    private DataSetObserver mObserver;

    public PlaylistContainerAdapter(Context context, PlaylistContainer container) {
        mContext = context;
        mContainer = container;
        mContainer.setCallback(this, false);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        final NativeItem item = getItem(position);
        return item instanceof Playlist;
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
        return mContainer.getCount();
    }

    @Override
    public NativeItem getItem(int position) {
        return mContainer.getItem(position);
    }

    @Override
    public long getItemId(int position) {
        final NativeItem item = mContainer.getItem(position);
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
        final NativeItem item = getItem(position);
        if (item instanceof Playlist) {
            final Playlist playlist = (Playlist) item;
            view.setText(playlist.getName());
            playlist.setCallback(this, false);
        } else if (item instanceof FolderStart) {
            view.setText(((FolderStart) item).getName());
        } else if (item instanceof FolderEnd) {
            view.setText("/>");
        } else {
            view.setText("---"); //Placeholder
        }
        view.setVisibility(item == null ? View.GONE : View.VISIBLE);
        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        TextView view = (TextView) convertView;
        if (view == null) {
            view = new TextView(mContext);
            view.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        final NativeItem item = getItem(position);
        if (item instanceof Playlist) {
            final Playlist playlist = (Playlist) item;
            view.setText(playlist.getName());
            playlist.setCallback(this, false);
        } else if (item instanceof FolderStart) {
            view.setText(((FolderStart) item).getName());
        } else if (item instanceof FolderEnd) {
            view.setText("/>");
        } else {
            view.setText("---"); //Placeholder
        }
        view.setVisibility(item == null ? View.GONE : View.VISIBLE);
        return view;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1; //TODO 2, playlist and folder
    }

    @Override
    public boolean isEmpty() {
        return mContainer.getCount() == 0;
    }

    @Override
    public void onContainerLoaded() {
        if (mObserver != null) {
            mObserver.onChanged();
        }
    }

    @Override
    public void onPlaylistUpdateInProgress(boolean done) {
    }

    @Override
    public void onPlaylistRenamed() {
        if (mObserver != null) {
            mObserver.onChanged();
        }
    }

    @Override
    public void onPlaylistStateChanged() {
    }
}
