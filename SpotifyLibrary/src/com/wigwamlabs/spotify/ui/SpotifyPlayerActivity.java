package com.wigwamlabs.spotify.ui;

import android.content.Context;
import android.media.AudioManager;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.wigwamlabs.spotify.Player;
import com.wigwamlabs.spotify.Session;
import com.wigwamlabs.spotify.Track;

public abstract class SpotifyPlayerActivity extends SpotifyActivity implements Player.Callback {
    private Player mPlayer;
    private SpotifyImageView mTrackImage;
    private TextView mTrackArtists;
    private TextView mTrackName;
    private View mResumeButton;
    private View mPauseButton;
    private View mNextButton;
    private ProgressBar mTrackProgress;
    private boolean mTrackProgressIsBeingManipulated;
    private int mTrackImageSize;

    @Override
    protected void onResume() {
        super.onResume();

        if (mPlayer != null) {
            mPlayer.addCallback(this, true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mPlayer != null) {
            mPlayer.removeCallback(this);
        }
    }

    @Override
    protected void onDestroy() {
        if (mPlayer != null) {
            mPlayer.removeCallback(this);
            mPlayer = null;
        }

        super.onDestroy();
    }

    protected void setTrackImage(SpotifyImageView image, int size) {
        mTrackImageSize = size;
        if (image != null) {
            final Session session = getSpotifySession();
            if (session != null) {
                image.setImageProvider(session.getImageProvider());
            }
        }
        mTrackImage = image;
    }

    protected void setTrackArtists(TextView trackArtists) {
        mTrackArtists = trackArtists;
    }

    protected void setTrackName(TextView trackName) {
        mTrackName = trackName;
    }

    protected void setResumeButton(View button) {
        mResumeButton = button;
        mResumeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayer.resume();
            }
        });
    }

    protected void setPauseButton(View button) {
        mPauseButton = button;
        mPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayer.pause();
            }
        });
    }

    protected void setNextButton(View button) {
        mNextButton = button;
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayer.next();
            }
        });
    }

    protected void setTrackProgress(ProgressBar progressBar) {
        mTrackProgress = progressBar;

        if (progressBar != null && progressBar instanceof SeekBar) {
            final SeekBar seekBar = (SeekBar) progressBar;
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    mTrackProgressIsBeingManipulated = true;
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    mTrackProgressIsBeingManipulated = false;
                    onSeekToPosition(seekBar.getProgress());
                }
            });
        }
    }

    protected Player getPlayer() {
        return mPlayer;
    }

    @Override
    protected void onSpotifySessionAttached(Session spotifySession) {
        mPlayer = spotifySession.getPlayer();
        mPlayer.addCallback(this, true);

        if (mTrackImage != null) {
            mTrackImage.setImageProvider(spotifySession.getImageProvider());
        }
    }

    @Override
    public void onStateChanged(int state) {
        //TODO update seekbar
        switch (state) {
        case Player.STATE_STARTED:
        case Player.STATE_STOPPED:
            if (mPauseButton != null) {
                mPauseButton.setVisibility(View.GONE);
            }
            if (mResumeButton != null) {
                mResumeButton.setVisibility(View.GONE);
            }
            if (mNextButton != null) {
                mNextButton.setVisibility(View.GONE);
            }
            break;
        case Player.STATE_PLAYING:
            if (mPauseButton != null) {
                mPauseButton.setVisibility(View.VISIBLE);
            }
            if (mResumeButton != null) {
                mResumeButton.setVisibility(View.GONE);
            }
            if (mNextButton != null) {
                mNextButton.setVisibility(View.VISIBLE);
            }
            break;
        case Player.STATE_PAUSED_USER:
        case Player.STATE_PAUSED_AUDIOFOCUS:
        case Player.STATE_PAUSED_NOISY:
            if (mPauseButton != null) {
                mPauseButton.setVisibility(View.GONE);
            }
            if (mResumeButton != null) {
                mResumeButton.setVisibility(View.VISIBLE);
            }
            if (mNextButton != null) {
                mNextButton.setVisibility(View.VISIBLE);
            }
            break;
        }
    }

    @Override
    public void onCurrentTrackUpdated(Track track) {
        if (track != null) {
            if (mTrackImage != null) {
                mTrackImage.setImageLink(track.getImageLink(mTrackImageSize));
            }
            if (mTrackArtists != null) {
                mTrackArtists.setText(track.getArtistsString());
            }
            if (mTrackName != null) {
                mTrackName.setText(track.getName());
            }
        }
    }

    @Override
    public void onTrackProgress(int secondsPlayed, int secondsDuration) {
        if (mTrackProgress != null && !mTrackProgressIsBeingManipulated) {
            mTrackProgress.setMax(secondsDuration);
            mTrackProgress.setProgress(secondsPlayed);
        }
    }

    private void onSeekToPosition(int progressSeconds) {
        mPlayer.seek(progressSeconds * 1000);
    }

    protected AudioManager getAudioManager() {
        return (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }
}
