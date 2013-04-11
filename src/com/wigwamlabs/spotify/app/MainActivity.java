package com.wigwamlabs.spotify.app;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.wigwamlabs.spotify.NativeItem;
import com.wigwamlabs.spotify.Player;
import com.wigwamlabs.spotify.Playlist;
import com.wigwamlabs.spotify.PlaylistContainer;
import com.wigwamlabs.spotify.PlaylistQueue;
import com.wigwamlabs.spotify.Session;
import com.wigwamlabs.spotify.SpotifyError;
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
    private View mResumeButton;
    private View mPauseButton;
    private View mNextButton;
    private boolean mAutoLogin;
    private View mLoginOverlay;
    private EditText mLoginUsername;
    private EditText mLoginPassword;
    private TextView mLoginErrorMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLoginOverlay = findViewById(R.id.loginOverlay);
        mLoginErrorMessage = (TextView) findViewById(R.id.loginErrorMessage);
        mLoginUsername = (EditText) findViewById(R.id.loginName);
        mLoginPassword = (EditText) findViewById(R.id.loginPassword);
        mLoginPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == R.id.actionLogin || actionId == EditorInfo.IME_NULL) {
                    login();
                    return true;
                }
                return false;
            }
        });
        mLoginButton = findViewById(R.id.login);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

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
                onTrackClicked(((PlaylistAdapter) parent.getAdapter()).getItem(position));
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
        mAutoLogin = true;
        mSpotifySession.addCallback(this, true);
        mPlayer = mSpotifySession.getPlayer();
        mPlayer.addCallback(this, true);
    }

    @Override
    public void onServiceDisconnected(ComponentName arg0) {
        mService = null;
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mSpotifySession != null) {
            mSpotifySession.removeCallback(this);
        }
        if (mPlayer != null) {
            mPlayer.removeCallback(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mSpotifySession != null) {
            mSpotifySession.addCallback(this, true);
        }
        if (mPlayer != null) {
            mPlayer.addCallback(this, true);
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
            mPlayer.removeCallback(this);
            mPlayer = null;
        }
        if (mSpotifySession != null) {
            mSpotifySession.removeCallback(this);
            mSpotifySession = null;
        }

        unbindService(this);
        mService = null;
    }

    private void login() {
        final String username = mLoginUsername.getText().toString().trim();
        final String password = mLoginPassword.getText().toString().trim();

        EditText firstError = null;
        if (username.length() == 0) {
            mLoginUsername.setError(getString(R.string.loginUsernameRequired));
            if (firstError == null) {
                firstError = mLoginUsername;
            }
        } else {
            mLoginUsername.setError(null);
        }

        if (password.length() == 0) {
            mLoginPassword.setError(getString(R.string.loginPasswordRequired));
            if (firstError == null) {
                firstError = mLoginPassword;
            }
        } else {
            mLoginPassword.setError(null);
        }

        if (firstError == null) {
            ViewUtils.hideSoftInput(mLoginUsername);

            mLoginErrorMessage.setVisibility(View.GONE);
            mLoginUsername.setEnabled(false);
            mLoginPassword.setEnabled(false);
            mLoginButton.setEnabled(false);

            mSpotifySession.login(username, password, true);
        } else {
            firstError.requestFocus();
            ViewUtils.showSoftInput(firstError);
        }
    }

    @Override
    public void onLoggedIn(int error) {
        mLoginUsername.setEnabled(true);
        mLoginPassword.setEnabled(true);
        mLoginButton.setEnabled(true);

        final int errorResourceId;
        switch (error) {
        case SpotifyError.OK:
            errorResourceId = 0;
            break;
        case SpotifyError.UNABLE_TO_CONTACT_SERVER:
            errorResourceId = R.string.loginErrorUnableToContactServer;
            break;
        case SpotifyError.BAD_USERNAME_OR_PASSWORD:
            errorResourceId = R.string.loginErrorBadUsernameOrPassword;
            break;
        case SpotifyError.USER_BANNED:
            errorResourceId = R.string.loginErrorUserBanned;
            break;
        case SpotifyError.USER_NEEDS_PREMIUM:
            errorResourceId = R.string.loginErrorUserNeedsPremium;
            break;
        case SpotifyError.CLIENT_TOO_OLD:
            errorResourceId = R.string.loginErrorClientTooOld;
            break;
        case SpotifyError.OTHER_PERMANENT:
            errorResourceId = R.string.loginErrorPermanent;
            break;
        default:
        case SpotifyError.OTHER_TRANSIENT:
            errorResourceId = R.string.loginErrorTransient;
            break;
        }

        if (errorResourceId == 0) {
            mLoginOverlay.setVisibility(View.GONE);
            mLoginErrorMessage.setVisibility(View.GONE);
        } else {
            mLoginErrorMessage.setText(errorResourceId);
            mLoginErrorMessage.setVisibility(View.VISIBLE);
        }

        if (error == SpotifyError.BAD_USERNAME_OR_PASSWORD) {
            mLoginUsername.requestFocus();
            ViewUtils.showSoftInput(mLoginUsername);
        }
    }

    @Override
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

        boolean showLogin = false;
        switch (state) {
        case Session.CONNECTION_STATE_LOGGED_OUT:
        case Session.CONNECTION_STATE_UNDEFINED:
            showLogin = true;
            if (mAutoLogin) {
                mAutoLogin = false;
                if (mSpotifySession.relogin()) {
                    showLogin = false;
                }
            }
            break;
        case Session.CONNECTION_STATE_DISCONNECTED:
        case Session.CONNECTION_STATE_LOGGED_IN:
        case Session.CONNECTION_STATE_OFFLINE:
            showLogin = false;
            break;
        }

        final int oldLoginOverlayVisibility = mLoginOverlay.getVisibility();
        mLoginOverlay.setVisibility(showLogin ? View.VISIBLE : View.GONE);
        if (showLogin && oldLoginOverlayVisibility != mLoginOverlay.getVisibility()) {
            mLoginUsername.requestFocus();
            ViewUtils.showSoftInput(mLoginUsername);
        }

        if (state != Session.CONNECTION_STATE_LOGGED_OUT && mPlaylistContainer == null) {
            mPlaylistContainer = mSpotifySession.getPlaylistContainer();
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

            mService.setPlayIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0));
            mPlayer.play(new PlaylistQueue(mPlaylist));
        }
    }

    private void onTrackClicked(Track item) {
        //TODO start playlist queue at specific track
        mService.setPlayIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0));
        mPlayer.play(new TrackPlaylist(item));
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
