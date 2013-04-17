package com.wigwamlabs.spotify.app;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.wigwamlabs.spotify.NativeItem;
import com.wigwamlabs.spotify.Player;
import com.wigwamlabs.spotify.Playlist;
import com.wigwamlabs.spotify.PlaylistContainer;
import com.wigwamlabs.spotify.PlaylistQueue;
import com.wigwamlabs.spotify.Session;
import com.wigwamlabs.spotify.Track;
import com.wigwamlabs.spotify.ui.PlaylistAdapter;
import com.wigwamlabs.spotify.ui.PlaylistContainerAdapter;
import com.wigwamlabs.spotify.ui.SpotifyActivity;

public class MainActivity extends SpotifyActivity implements Player.Callback {
    private TextView mConnectionState;
    private PlaylistContainer mPlaylistContainer;
    private ListView mPlaylistsList;
    private Playlist mPlaylist;
    private ListView mPlaylistList;
    private Track mTrack;
    private TextView mTrackName;
    private TextView mTrackArtists;
    private Player mPlayer;
    private SeekBar mSeekBar;
    private View mResumeButton;
    private View mPauseButton;
    private View mNextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mConnectionState = (TextView) findViewById(R.id.connectionState);

        mPlaylistsList = (ListView) findViewById(R.id.playlists);
        mPlaylistsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onPlaylistClicked(((PlaylistContainerAdapter) parent.getAdapter()).getItem(position));
            }
        });

        mPlaylistList = (ListView) findViewById(R.id.playlist);
        mPlaylistList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onTrackClicked(position);
            }
        });

        mTrackName = (TextView) findViewById(R.id.trackName);
        mTrackArtists = (TextView) findViewById(R.id.trackArtists);

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

        bindSpotifyService();
    }

    @Override
    protected void onSpotifySessionAttached(Session spotifySession) {
        setAutoLogin(true);

        mPlayer = spotifySession.getPlayer();
        mPlayer.addCallback(this, true);
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
        if (mPlaylist != null) {
            mPlaylist.destroy();
            mPlaylist = null;
        }
        if (mPlaylistContainer != null) {
            mPlaylistContainer.destroy();
            mPlaylistContainer = null;
        }
        if (mPlayer != null) {
            mPlayer.removeCallback(this);
            mPlayer = null;
        }

        super.onDestroy();
    }

    @Override
    public void onConnectionStateUpdated(int state) {
        super.onConnectionStateUpdated(state);

        final int res;
        switch (state) {
        case Session.CONNECTION_STATE_LOGGED_OUT:
            res = R.string.connectionStateLoggedOut;
            break;
        case Session.CONNECTION_STATE_LOGGED_IN:
            res = R.string.connectionStateLoggedIn;
            break;
        case Session.CONNECTION_STATE_DISCONNECTED:
            res = R.string.connectionStateDisconnected;
            break;
        case Session.CONNECTION_STATE_OFFLINE:
            res = R.string.connectionStateOffline;
            break;
        case Session.CONNECTION_STATE_UNDEFINED:
        default:
            res = R.string.connectionStateUndefined;
            break;
        }
        mConnectionState.setText(res);

        if (state != Session.CONNECTION_STATE_LOGGED_OUT && mPlaylistContainer == null) {
            mPlaylistContainer = getSpotifySession().getPlaylistContainer();
            mPlaylistsList.setAdapter(new PlaylistContainerAdapter(this, mPlaylistContainer));
        }
    }

    private void onPlaylistClicked(NativeItem item) {
        if (item instanceof Playlist) {
            final Playlist playlist = (Playlist) item;

            if (mPlaylist != null) {
                mPlaylist.destroy();
                mPlaylist = null;
            }
            mPlaylist = playlist.clone();
            mPlaylistList.setAdapter(new PlaylistAdapter(this, mPlaylist));
        }
    }

    private void onTrackClicked(int position) {
        //TODO change queue if current queue is using the same playlist, instead of always creating a new queue
        getSpotifyService().setPlayIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0));
        mPlayer.play(new PlaylistQueue(mPlaylist, position));
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
