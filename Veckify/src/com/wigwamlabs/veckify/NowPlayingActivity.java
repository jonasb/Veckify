package com.wigwamlabs.veckify;

import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.wigwamlabs.spotify.Player;
import com.wigwamlabs.spotify.Session;
import com.wigwamlabs.spotify.Track;
import com.wigwamlabs.spotify.ui.SpotifyActivity;

public class NowPlayingActivity extends SpotifyActivity implements Player.Callback {
    private Player mPlayer;
    private Track mTrack;

    private TextView mTrackArtists;
    private TextView mTrackName;
    private SeekBar mSeekBar;
    private View mResumeButton;
    private View mPauseButton;
    private View mNextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initUi();

        bindSpotifyService();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mPlayer != null) {
            mPlayer.removeCallback(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mPlayer != null) {
            mPlayer.addCallback(this, true);
        }
    }

    @Override
    protected void onDestroy() {
        if (mTrack != null) {
            mTrack.destroy();
            mTrack = null;
        }
        if (mPlayer != null) {
            mPlayer.removeCallback(this);
            mPlayer = null;
        }

        super.onDestroy();
    }

    private void initUi() {
        setContentView(R.layout.activity_now_playing);

        mTrackArtists = (TextView) findViewById(R.id.trackArtists);
        mTrackName = (TextView) findViewById(R.id.trackName);

        mSeekBar = (SeekBar) findViewById(R.id.seekBar);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                onSeekToPosition(seekBar.getProgress());
            }
        });

        mResumeButton = findViewById(R.id.resumeButton);
        mResumeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayer.resume();
            }
        });
        mPauseButton = findViewById(R.id.pauseButton);
        mPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayer.pause();
            }
        });
        mNextButton = findViewById(R.id.nextButton);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayer.next();
            }
        });
    }

    @Override
    protected void onSpotifySessionAttached(Session spotifySession) {
        setAutoLogin(true);

        mPlayer = spotifySession.getPlayer();
        mPlayer.addCallback(this, true);
    }

    private void onSeekToPosition(int progressSeconds) {
        mPlayer.seek(progressSeconds * 1000);
    }

    @Override
    public void onStateChanged(int state) {
        switch (state) {
        case Player.STATE_STARTED:
        case Player.STATE_STOPPED:
            mPauseButton.setVisibility(View.GONE);
            mResumeButton.setVisibility(View.GONE);
            mNextButton.setVisibility(View.GONE);
            break;
        case Player.STATE_PLAYING:
            mPauseButton.setVisibility(View.VISIBLE);
            mResumeButton.setVisibility(View.GONE);
            mNextButton.setVisibility(View.VISIBLE);
            break;
        case Player.STATE_PAUSED_USER:
        case Player.STATE_PAUSED_AUDIOFOCUS:
        case Player.STATE_PAUSED_NOISY:
            mPauseButton.setVisibility(View.GONE);
            mResumeButton.setVisibility(View.VISIBLE);
            mNextButton.setVisibility(View.VISIBLE);
            break;
        }
    }

    @Override
    public void onTrackProgress(int secondsPlayed, int secondsDuration) {
        mSeekBar.setMax(secondsDuration);
        mSeekBar.setProgress(secondsPlayed);
    }

    @Override
    public void onCurrentTrackUpdated(Track track) {
        if (mTrack != null) {
            mTrack.destroy();
        }
        mTrack = (track != null ? track.clone() : null);

        if (mTrack != null) {
            mTrackName.setText(mTrack.getName());
            mTrackArtists.setText(mTrack.getArtistsString());
        } else {
            //TODO
        }
    }
}
