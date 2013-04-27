package com.wigwamlabs.spotify;

import android.app.PendingIntent;
import android.content.Context;
import android.media.AudioManager;

public class PendingPlayPlaylistAction extends PendingPlaylistAction {
    private final SpotifyService mService;
    private final Session mSession;
    private final PendingIntent mPlayIntent;
    private final int mVolume;
    private final boolean mShuffle;

    public PendingPlayPlaylistAction(SpotifyService service, Session session, String link, PendingIntent playIntent, int volume, boolean shuffle) {
        super(session, link, true);
        mService = service;
        mSession = session;
        mPlayIntent = playIntent;
        mVolume = volume;
        mShuffle = shuffle;
    }

    @Override
    protected void onPlaylistLoaded(Playlist playlist) {
        if (mVolume >= 0) {
            final AudioManager audioManager = (AudioManager) mService.getSystemService(Context.AUDIO_SERVICE);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mVolume, 0);
        }

        mService.setPlayIntent(mPlayIntent);
        mSession.getPlayer().play(new PlaylistQueue(playlist, mShuffle ? -1 : 0, mShuffle));
    }
}
