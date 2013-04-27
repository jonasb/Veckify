package com.wigwamlabs.spotify;

public abstract class PendingPlaylistAction implements Session.Callback, Playlist.Callback {
    private final Session mSession;
    private final String mLink;
    private final boolean mLoginIfNeeded;
    private Playlist mPlaylist;

    public PendingPlaylistAction(Session session, String link, boolean loginIfNeeded) {
        mSession = session;
        mLink = link;
        mLoginIfNeeded = loginIfNeeded;
    }

    public void start() {
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
