package com.wigwamlabs.spotify.ui;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.wigwamlabs.spotify.FolderEnd;
import com.wigwamlabs.spotify.FolderStart;
import com.wigwamlabs.spotify.NativeItem;
import com.wigwamlabs.spotify.Playlist;
import com.wigwamlabs.spotify.PlaylistContainer;

public class PlaylistContainerAdapter implements ListAdapter, PlaylistContainer.Callback, Playlist.Callback {
    private final Context mContext;
    private final PlaylistContainer mContainer;
    private DataSetObserver mObserver;

    public PlaylistContainerAdapter(Context context, PlaylistContainer container) {
        mContext = context;
        mContainer = container;
        mContainer.setCallback(this);
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
        return mContainer.getCount();
    }

    public NativeItem getItem(int position) {
        return mContainer.getItem(position);
    }

    public long getItemId(int position) {
        final NativeItem item = mContainer.getItem(position);
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
        final NativeItem item = getItem(position);
        if (item instanceof Playlist) {
            final Playlist playlist = (Playlist) item;
            view.setText(playlist.getName());
            playlist.setCallback(this);
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

    public int getItemViewType(int position) {
        return 0;
    }

    public int getViewTypeCount() {
        return 1; //TODO 2, playlist and folder
    }

    public boolean isEmpty() {
        return mContainer.getCount() == 0;
    }

    public void onContainerLoaded() {
        if (mObserver != null) {
            mObserver.onChanged();
        }
    }

    public void onPlaylistUpdateInProgress(boolean done) {
    }

    public void onPlaylistRenamed() {
        if (mObserver != null) {
            mObserver.onChanged();
        }
    }
}
