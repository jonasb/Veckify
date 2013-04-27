package com.wigwamlabs.spotify;

import android.app.PendingIntent;
import android.content.Context;
import android.media.AudioManager;

public class PendingAction implements Session.Callback, Playlist.Callback {
    private final SpotifyService mService;
    private final Session mSession;
    private final String mLink;
    private final PendingIntent mPlayIntent;
    private final int mVolume;
    private final boolean mShuffle;
    private Playlist mPlaylist;

    public PendingAction(SpotifyService service, Session session, String link, PendingIntent playIntent, int volume, boolean shuffle) {
        mService = service;
        mSession = session;
        mLink = link;
        mPlayIntent = playIntent;
        mVolume = volume;
        mShuffle = shuffle;
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

            if (mVolume >= 0) {
                final AudioManager audioManager = (AudioManager) mService.getSystemService(Context.AUDIO_SERVICE);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mVolume, 0);
            }

            mService.setPlayIntent(mPlayIntent);
            mSession.getPlayer().play(new PlaylistQueue(mPlaylist, mShuffle ? -1 : 0, mShuffle));
            mPlaylist = null;
        }
    }
}
