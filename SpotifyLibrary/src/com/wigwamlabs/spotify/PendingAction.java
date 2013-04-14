package com.wigwamlabs.spotify;

public class PendingAction implements Session.Callback, Playlist.Callback {
    private final Session mSession;
    private final String mLink;
    private Playlist mPlaylist;

    public PendingAction(Session session, String link) {
        mSession = session;
        mLink = link;
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
            mPlaylist = new Playlist(mSession, mLink);
            mPlaylist.setCallback(this, true);
        }
    }

    @Override
    public void onConnectionStateUpdated(int state) {
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
