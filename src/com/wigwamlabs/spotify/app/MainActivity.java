package com.wigwamlabs.spotify.app;

import android.app.Activity;
import android.os.Bundle;
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
import com.wigwamlabs.spotify.Session;
import com.wigwamlabs.spotify.Track;
import com.wigwamlabs.spotify.ui.PlaylistAdapter;
import com.wigwamlabs.spotify.ui.PlaylistContainerAdapter;

public class MainActivity extends Activity implements Session.Callback {

    private Session mSpotifySession;
    private TextView mConnectionState;
    private View mLoginButton;
    private PlaylistContainer mPlaylistContainer;
    private View mGetPlaylistsButton;
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

        mGetPlaylistsButton = findViewById(R.id.getPlaylists);
        mGetPlaylistsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getPlaylists();
            }
        });

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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mSpotifySession != null) {
            mSpotifySession.destroy();
            mSpotifySession = null;
        }
    }

    private void login() {
        mLoginButton.setEnabled(false);

        if (mSpotifySession != null) {
            mSpotifySession.destroy();
        }
        mSpotifySession = new Session(this, null, null, null);
        mSpotifySession.setCallback(this);
        if (!mSpotifySession.relogin()) {
            mSpotifySession.login(TempPrivateSettings.username, TempPrivateSettings.password, true);
        }
        mPlayer = mSpotifySession.getPlayer();
        mPlayer.setCallback(new Player.Callback() {
            public void onTrackProgress(int secondsPlayed, int secondsDuration) {
                mSeekBar.setMax(secondsDuration);
                mSeekBar.setProgress(secondsPlayed);
            }
        });
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

        mGetPlaylistsButton.setVisibility(state == Session.CONNECTION_STATE_LOGGED_OUT ? View.GONE : View.VISIBLE);
    }

    private void getPlaylists() {
        if (mPlaylistContainer != null) {
            mPlaylistContainer.destroy();
            mPlaylistContainer = null;
        }
        mPlaylistContainer = mSpotifySession.getPlaylistContainer();
        mPlaylistsList.setAdapter(new PlaylistContainerAdapter(this, mPlaylistContainer));
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
        }
    }

    private void onTrackClicked(Track item) {
        if (mTrack != null) {
            mTrack.destroy();
            mTrack = null;
        }
        mTrack = item.clone();

        mTrackName.setText(mTrack.getName());
        final StringBuilder sb = new StringBuilder();
        for (Artist artist : mTrack.getArtists()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(artist.getName());
        }
        mTrackArtists.setText(sb.toString());

        mPlayer.play(mTrack);
    }

    private void onSeekToPosition(int progressSeconds) {
        mPlayer.seek(progressSeconds * 1000);
    }
}
