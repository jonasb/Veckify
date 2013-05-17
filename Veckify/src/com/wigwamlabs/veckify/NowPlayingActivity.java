package com.wigwamlabs.veckify;

import android.app.KeyguardManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.Window;
import android.widget.SeekBar;
import android.widget.TextView;

import com.wigwamlabs.spotify.ImageProvider;
import com.wigwamlabs.spotify.Session;
import com.wigwamlabs.spotify.Track;
import com.wigwamlabs.spotify.ui.SpotifyImageView;
import com.wigwamlabs.spotify.ui.SpotifyPlayerActivity;

import java.util.Calendar;

import static android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
import static android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
import static android.view.WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON;
import static android.view.WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD;
import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
import static android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
import static android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON;

public class NowPlayingActivity extends SpotifyPlayerActivity implements TimeUpdater.Callback {
    public static final String ACTION_ALARM = "alarm";
    public static final String EXTRA_ALARM_NAME = "name";
    private final Handler mHandler = new Handler();
    private Track mTrack;
    private boolean mAlarmLaunchedWithKeyguard;
    private boolean mAlarmIsDismissed;
    private Runnable mCheckKeyguardActivation;
    private TextView mCurrentTime;
    private TimeUpdater mTimeUpdater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Debug.logLifecycle("NowPlayingActivity.onCreate()");
        super.onCreate(savedInstanceState);
        handleIntent(getIntent());

        initUi();

        mTimeUpdater = new TimeUpdater(mHandler, this);

        bindSpotifyService();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Debug.logLifecycle("NowPlayingActivity.onNewIntent()");
        super.onNewIntent(intent);
        setIntent(intent);

        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent != null && ACTION_ALARM.equals(intent.getAction())) {
            Debug.logAlarmScheduling("New alarm, turn screen on etc.");
            mAlarmIsDismissed = false;
            mAlarmLaunchedWithKeyguard = isKeyguardActive();
            final String name = intent.getStringExtra(EXTRA_ALARM_NAME);
            getActionBar().setTitle(name);
        } else {
            mAlarmIsDismissed = true;
        }
    }

    @Override
    protected void onResume() {
        Debug.logLifecycle("NowPlayingActivity.onResume()");
        super.onResume();

        if (mAlarmLaunchedWithKeyguard && !mAlarmIsDismissed && !isKeyguardActive()) {
            Debug.logAlarmScheduling("Dismissing alarm since user deactivated keyguard");
            mAlarmIsDismissed = true;
        }

        updateWindowFlags();

        if (!isKeyguardActive()) {
            mCheckKeyguardActivation = new Runnable() {
                @Override
                public void run() {
                    if (isKeyguardActive()) {
                        onKeyguardActivated();
                    } else {
                        mHandler.postDelayed(this, 2000);
                    }
                }
            };
            mHandler.postDelayed(mCheckKeyguardActivation, 5000);
        }

        mTimeUpdater.start();
    }

    @Override
    protected void onPause() {
        Debug.logLifecycle("NowPlayingActivity.onPause()");
        super.onPause();

        if (!mAlarmIsDismissed && !mAlarmLaunchedWithKeyguard) {
            Debug.logAlarmScheduling("Dismissing alarm since user paused activity");
            mAlarmIsDismissed = true;
        }

        if (mCheckKeyguardActivation != null) {
            mHandler.removeCallbacks(mCheckKeyguardActivation);
            mCheckKeyguardActivation = null;
        }

        mTimeUpdater.stop();
    }

    @Override
    protected void onDestroy() {
        Debug.logLifecycle("NowPlayingActivity.onDestroy()");
        if (mTrack != null) {
            mTrack.destroy();
            mTrack = null;
        }

        super.onDestroy();
    }

    private void initUi() {
        setContentView(R.layout.activity_now_playing);

        mCurrentTime = (TextView) findViewById(R.id.currentTime);

        setTrackImage((SpotifyImageView) findViewById(R.id.trackImage), ImageProvider.SIZE_NORMAL);
        setTrackArtists((TextView) findViewById(R.id.trackArtists));
        setTrackName((TextView) findViewById(R.id.trackName));
        setTrackProgress((SeekBar) findViewById(R.id.seekBar));
        setResumeButton(findViewById(R.id.resumeButton));
        setPauseButton(findViewById(R.id.pauseButton));
        setNextButton(findViewById(R.id.nextButton));
    }

    @Override
    public void onTimeUpdated(Calendar cal) {
        final String time = String.format("%d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
        mCurrentTime.setText(time);
    }

    private boolean isKeyguardActive() {
        final KeyguardManager manager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        //TODO use manager.isKeyguardLocked() on API 16+?
        return manager.inKeyguardRestrictedInputMode();
    }

    private void onKeyguardActivated() {
        // no need to check anymore
        mHandler.removeCallbacks(mCheckKeyguardActivation);
        mCheckKeyguardActivation = null;

        // switch mode, treat it as if the alarm was launched with keyguard on
        if (!mAlarmIsDismissed) {
            mAlarmLaunchedWithKeyguard = true;
        }

        //
        updateWindowFlags();
    }

    private void updateWindowFlags() {
        final Window window = getWindow();
        int addFlags = 0;
        int clearFlags = 0;

        // we always want to keep the screen on
        addFlags |= FLAG_KEEP_SCREEN_ON
                | FLAG_ALLOW_LOCK_WHILE_SCREEN_ON;

        // hide all chrome if the keyguard is active
        if (!mAlarmIsDismissed && isKeyguardActive()) {
            addFlags |= FLAG_FULLSCREEN;
            getActionBar().hide();
        } else {
            clearFlags |= FLAG_FULLSCREEN;
            getActionBar().show();
        }

        // turn on screen brightness when the alarm is not dismissed
        window.getAttributes().screenBrightness = mAlarmIsDismissed ? BRIGHTNESS_OVERRIDE_NONE : BRIGHTNESS_OVERRIDE_FULL;

        // turn screen the screen if the alarm is not dismissed
        final int windowFlagsRunningAlarm = FLAG_TURN_SCREEN_ON
                | FLAG_SHOW_WHEN_LOCKED
                | FLAG_DISMISS_KEYGUARD;
        if (mAlarmIsDismissed) {
            clearFlags |= windowFlagsRunningAlarm;
        } else {
            addFlags |= windowFlagsRunningAlarm;
        }

        window.addFlags(addFlags);
        window.clearFlags(clearFlags);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            startActivity(getParentActivityIntent());
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSpotifySessionAttached(Session spotifySession) {
        super.onSpotifySessionAttached(spotifySession);
        setAutoLogin(true);
    }
}
