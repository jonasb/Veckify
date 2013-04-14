package com.wigwamlabs.spotify;

public class PendingAction implements Session.Callback, PlaylistContainer.Callback, Playlist.Callback {
    private final Session mSession;
    private PlaylistContainer mPlaylistContainer;
    private Playlist mPlaylist;

    public PendingAction(Session session) {
        mSession = session;
        switch (session.getConnectionState()) {
        case Session.CONNECTION_STATE_LOGGED_OUT:
        case Session.CONNECTION_STATE_UNDEFINED:
            session.addCallback(this, false);
            if (!session.relogin()) {
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
            mPlaylistContainer = mSession.getPlaylistContainer();
            mPlaylistContainer.setCallback(this, true);
        }
    }

    @Override
    public void onConnectionStateUpdated(int state) {
    }

    @Override
    public void onContainerLoaded() {
        mPlaylistContainer.setCallback(null, false);
        mPlaylist = null;
        final int count = mPlaylistContainer.getCount();
        for (int i = 0; i < count; i++) {
            final NativeItem item = mPlaylistContainer.getItem(i);
            if (item instanceof Playlist) {
                mPlaylist = (Playlist) item;
                break;
            }
        }
        if (mPlaylist != null) {
            mPlaylist.setCallback(this, true);
        }
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

            mSession.getPlayer().play(new PlaylistQueue(mPlaylist, 0));
            mPlaylist = null;
        }
    }
}
