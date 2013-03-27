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
import com.wigwamlabs.spotify.Playlist;
import com.wigwamlabs.spotify.PlaylistContainer;
import com.wigwamlabs.spotify.PlaylistContainerItem;

public class PlaylistContainerAdapter implements ListAdapter {
    private final Context mContext;
    private final PlaylistContainer mContainer;
    private DataSetObserver mObserver;

    public PlaylistContainerAdapter(Context context, PlaylistContainer container) {
        mContext = context;
        mContainer = container;
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

    public PlaylistContainerItem getItem(int position) {
        return mContainer.getPlaylist(position);
    }

    public long getItemId(int position) {
        final PlaylistContainerItem item = mContainer.getPlaylist(position);
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
        final PlaylistContainerItem item = getItem(position);
        if (item instanceof Playlist) {
            view.setText(((Playlist) item).getName());
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
}
