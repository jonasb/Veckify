package com.wigwamlabs.spotify.ui;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.wigwamlabs.spotify.FolderStart;
import com.wigwamlabs.spotify.NativeItem;
import com.wigwamlabs.spotify.Playlist;
import com.wigwamlabs.spotify.PlaylistContainer;
import com.wigwamlabs.spotify.R;

public class PlaylistContainerAdapter implements ListAdapter, PlaylistContainer.Callback, Playlist.Callback {
    private final Context mContext;
    private final LayoutInflater mLayoutInflater;
    private final PlaylistContainer mContainer;
    private DataSetObserver mObserver;

    public PlaylistContainerAdapter(Context context, PlaylistContainer container) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
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
        final NativeItem item = getItem(position);
        if (item != null) {
            if (item instanceof Playlist) {
                final Playlist playlist = (Playlist) item;

                TextView view = (TextView) convertView;
                if (view == null) {
                    view = (TextView) mLayoutInflater.inflate(R.layout.playlistcontainer_playlist, parent, false);
                }
                view.setText(playlist.getName());
                playlist.setCallback(this, false);
                return view;
            } else if (item instanceof FolderStart) {
                final FolderStart folderStart = (FolderStart) item;
                TextView view = (TextView) convertView;
                if (view == null) {
                    view = (TextView) mLayoutInflater.inflate(R.layout.playlistcontainer_folderstart, parent, false);
                }
                view.setText(folderStart.getName());
                return view;
            }
        }
        // null, FolderEnd or Placeholder
        final View view = (convertView != null ? convertView : new View(mContext));
        view.setVisibility(View.GONE);
        return view;
    }

    @Override
    public int getItemViewType(int position) {
        final NativeItem item = getItem(position);
        if (item != null) {
            if (item instanceof Playlist) {
                return 0;
            }
            if (item instanceof FolderStart) {
                return 1;
            }
        }
        // null, FolderEnd or Placeholder
        return 2;
    }

    @Override
    public int getViewTypeCount() {
        return 3;
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
