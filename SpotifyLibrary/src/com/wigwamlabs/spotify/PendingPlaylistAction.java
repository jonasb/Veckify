package com.wigwamlabs.spotify;

import android.os.Parcel;
import android.os.Parcelable;

public abstract class PendingPlaylistAction implements Parcelable, Session.Callback, Playlist.Callback {
    private final String mLink;
    private final boolean mLoginIfNeeded;
    private Session mSession;
    private Playlist mPlaylist;

    public PendingPlaylistAction(String link, boolean loginIfNeeded) {
        mLink = link;
        mLoginIfNeeded = loginIfNeeded;
    }

    PendingPlaylistAction(Parcel in) {
        mLink = in.readString();
        final boolean[] flags = {false};
        in.readBooleanArray(flags);
        mLoginIfNeeded = flags[0];
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mLink);
        dest.writeBooleanArray(new boolean[]{mLoginIfNeeded});
    }

    public void start(Session session) {
        mSession = session;
        switch (mSession.getConnectionState()) {
        case Session.CONNECTION_STATE_LOGGED_OUT:
        case Session.CONNECTION_STATE_UNDEFINED:
            mSession.addCallback(this, false);
            if (mLoginIfNeeded && !mSession.relogin()) {
                onLoggedIn(SpotifyError.NO_CREDENTIALS);
            }
            break;
        default:
            onLoggedIn(SpotifyError.OK);
            break;
        }
    }

    @Override
    public void onLoggedIn(int error) {
        mSession.removeCallback(this);
        if (error == SpotifyError.OK) {
            mPlaylist = Playlist.create(mSession, mLink);
            if (mPlaylist != null) {
                mPlaylist.setCallback(this, true);
            }
            //TODO deal with invalid link
        }
    }

    @Override
    public void onConnectionStateUpdated(int state) {
    }

    @Override
    public void onOfflineTracksToSyncChanged(boolean syncing, int remainingTracks, int approxTotalTracks) {
    }

    @Override
    public void onPlaylistUpdateInProgress(boolean done) {
    }

    @Override
    public void onPlaylistRenamed() {
    }

    @Override
    public void onPlaylistStateChanged() {
        if (mPlaylist.isLoaded()) {
            mPlaylist.setCallback(null, false);

            onPlaylistLoaded(mPlaylist);

            mPlaylist = null;
        }
    }

    protected abstract void onPlaylistLoaded(Playlist playlist);
}
