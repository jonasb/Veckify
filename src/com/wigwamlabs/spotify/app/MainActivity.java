package com.wigwamlabs.spotify.app;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.wigwamlabs.spotify.Artist;
import com.wigwamlabs.spotify.NativeItem;
import com.wigwamlabs.spotify.Player;
import com.wigwamlabs.spotify.Playlist;
import com.wigwamlabs.spotify.PlaylistContainer;
import com.wigwamlabs.spotify.PlaylistQueue;
import com.wigwamlabs.spotify.Session;
import com.wigwamlabs.spotify.SpotifyService;
import com.wigwamlabs.spotify.Track;
import com.wigwamlabs.spotify.TrackPlaylist;
import com.wigwamlabs.spotify.ui.PlaylistAdapter;
import com.wigwamlabs.spotify.ui.PlaylistContainerAdapter;

public class MainActivity extends Activity implements Session.Callback, Player.Callback, ServiceConnection {

    private SpotifyService mService;
    private Session mSpotifySession;
    private TextView mConnectionState;
    private View mLoginButton;
    private PlaylistContainer mPlaylistContainer;
    private ListView mPlaylistsList;
    private Playlist mPlaylist;
    private ListView mPlaylistList;
    private Track mTrack;
    private TextView mTrackName;
    private TextView mTrackArtists;
    private Player mPlayer;
    private SeekBar mSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLoginButton = findViewById(R.id.login);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                login();
            }
        });

        mConnectionState = (TextView) findViewById(R.id.connectionState);

        mPlaylistsList = (ListView) findViewById(R.id.playlists);
        mPlaylistsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onPlaylistClicked(((PlaylistContainerAdapter) parent.getAdapter()).getItem(position));
            }
        });

        mPlaylistList = (ListView) findViewById(R.id.playlist);
        mPlaylistList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onTrackClicked(((PlaylistAdapter) parent.getAdapter()).getItem(position));
            }
        });

        mTrackName = (TextView) findViewById(R.id.trackName);
        mTrackArtists = (TextView) findViewById(R.id.trackArtists);

        mSeekBar = (SeekBar) findViewById(R.id.seekBar);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                onSeekToPosition(seekBar.getProgress());
            }
        });

        bindSpotifyService();
    }

    private void bindSpotifyService() {
        final Intent intent = new Intent(this, SpotifyService.class);
        startService(intent);
        bindService(intent, this, BIND_AUTO_CREATE);
    }

    @Override
    public void onServiceConnected(ComponentName className, IBinder service) {
        final SpotifyService.LocalBinder binder = (SpotifyService.LocalBinder) service;
        mService = binder.getService();

        mSpotifySession = mService.getSession();
        mSpotifySession.setCallback(this, true); //TODO add support for multiple callbacks, methinks
        mPlayer = mSpotifySession.getPlayer();
        mPlayer.setCallback(this, true);
    }

    @Override
    public void onServiceDisconnected(ComponentName arg0) {
        mService = null;
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mSpotifySession != null) {
            mSpotifySession.setCallback(null, false);
        }
        if (mPlayer != null) {
            mPlayer.setCallback(null, false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mSpotifySession != null) {
            mSpotifySession.setCallback(this, true);
        }
        if (mPlayer != null) {
            mPlayer.setCallback(this, true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

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
            mPlayer.setCallback(null, false);
            mPlayer = null;
        }
        if (mSpotifySession != null) {
            mSpotifySession.setCallback(null, false);
            mSpotifySession = null;
        }

        unbindService(this);
        mService = null;
    }

    private void login() {
        mLoginButton.setEnabled(false);

        if (!mSpotifySession.relogin()) {
            mSpotifySession.login(TempPrivateSettings.username, TempPrivateSettings.password, true);
        }
    }

    public void onConnectionStateUpdated(int state) {
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

        mLoginButton.setVisibility(state == Session.CONNECTION_STATE_LOGGED_OUT ? View.VISIBLE : View.GONE);
        mLoginButton.setEnabled(true);

        if (state != Session.CONNECTION_STATE_LOGGED_OUT && mPlaylistContainer == null) {
            mPlaylistContainer = mSpotifySession.getPlaylistContainer();
            mPlaylistsList.setAdapter(new PlaylistContainerAdapter(this, mPlaylistContainer));
        }
    }

    private void onPlaylistClicked(NativeItem item) {
        Log.d("XXX", "Clicked: " + item);
        if (item instanceof Playlist) {
            final Playlist playlist = (Playlist) item;

            if (mPlaylist != null) {
                mPlaylist.destroy();
                mPlaylist = null;
            }
            mPlaylist = playlist.clone();
            mPlaylistList.setAdapter(new PlaylistAdapter(this, mPlaylist));

            mPlayer.play(new PlaylistQueue(mPlaylist));
        }
    }

    private void onTrackClicked(Track item) {
        //TODO start playlist queue at specific track
        mPlayer.play(new TrackPlaylist(item));
    }

    private void onSeekToPosition(int progressSeconds) {
        mPlayer.seek(progressSeconds * 1000);
    }

    public void onTrackProgress(int secondsPlayed, int secondsDuration) {
        mSeekBar.setMax(secondsDuration);
        mSeekBar.setProgress(secondsPlayed);
    }

    public void onCurrentTrackUpdated(Track track) {
        if (mTrack != null) {
            mTrack.destroy();
        }
        mTrack = (track != null ? track.clone() : null);

        if (mTrack != null) {
            mTrackName.setText(mTrack.getName());
            final StringBuilder sb = new StringBuilder();
            for (Artist artist : mTrack.getArtists()) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(artist.getName());
            }
            mTrackArtists.setText(sb.toString());
        } else {
            //TODO
        }
    }
}
