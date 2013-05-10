package com.wigwamlabs.spotify;

import android.app.PendingIntent;
import android.content.Context;
import android.media.AudioManager;
import android.os.Parcel;
import android.os.Parcelable;

import com.wigwamlabs.spotify.tts.TimeTtsProvider;

public class PendingPlayPlaylistAction extends PendingPlaylistAction {
    @SuppressWarnings("UnusedDeclaration")
    public static final Parcelable.Creator CREATOR =
            new Parcelable.Creator() {
                @Override
                public PendingPlayPlaylistAction createFromParcel(Parcel in) {
                    return new PendingPlayPlaylistAction(in);
                }

                @Override
                public PendingPlayPlaylistAction[] newArray(int size) {
                    return new PendingPlayPlaylistAction[size];
                }
            };
    private final PendingIntent mPlayIntent;
    private final int mVolume;
    private final boolean mShuffle;
    private final boolean mTellTime;
    private SpotifyService mService;
    private Player mPlayer;

    public PendingPlayPlaylistAction(String link, PendingIntent playIntent, Integer volume, boolean shuffle, boolean tellTime) {
        super(link, true);
        mPlayIntent = playIntent;
        mVolume = (volume == null ? -1 : volume.intValue());
        mShuffle = shuffle;
        mTellTime = tellTime;
    }

    private PendingPlayPlaylistAction(Parcel in) {
        super(in);

        mPlayIntent = in.readParcelable(PendingPlayPlaylistAction.class.getClassLoader());
        mVolume = in.readInt();
        final boolean[] flags = {false, false};
        in.readBooleanArray(flags);
        mShuffle = flags[0];
        mTellTime = flags[1];
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(mPlayIntent, flags);
        dest.writeInt(mVolume);
        dest.writeBooleanArray(new boolean[]{mShuffle, mTellTime});
    }

    public void start(SpotifyService service, Session session) {
        mService = service;
        mPlayer = session.getPlayer();
        super.start(session);
    }

    @Override
    protected void onPlaylistLoaded(Playlist playlist) {
        if (mVolume >= 0) {
            final AudioManager audioManager = (AudioManager) mService.getSystemService(Context.AUDIO_SERVICE);
            final int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume * mVolume / 100, 0);
        }

        mService.setPlayIntent(mPlayIntent);
        mPlayer.play(new PlaylistQueue(playlist, mShuffle ? -1 : 0, mShuffle));
        mPlayer.setTtsProvider(mTellTime ? new TimeTtsProvider() : null);
    }
}
