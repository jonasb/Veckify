package com.wigwamlabs.veckify;

import android.app.KeyguardManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.Window;
import android.widget.SeekBar;
import android.widget.TextView;

import com.wigwamlabs.spotify.Session;
import com.wigwamlabs.spotify.Track;
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

public class NowPlayingActivity extends SpotifyPlayerActivity {
    public static final String ACTION_ALARM = "alarm";
    private final Handler mHandler = new Handler();
    private Track mTrack;
    private boolean mAlarmLaunchedWithKeyguard;
    private boolean mAlarmIsDismissed;
    private Runnable mCheckKeyguardActivation;
    private Runnable mUpdateCurrentTimeRunnable;
    private TextView mCurrentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Debug.logLifecycle("NowPlayingActivity.onCreate()");
        super.onCreate(savedInstanceState);
        handleIntent(getIntent());

        initUi();

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

        mUpdateCurrentTimeRunnable = new Runnable() {
            @Override
            public void run() {
                final long timeToNextMinuteMs = updateCurrentTime();
                mHandler.postDelayed(this, timeToNextMinuteMs);
            }
        };
        mHandler.postDelayed(mUpdateCurrentTimeRunnable, updateCurrentTime());
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

        if (mUpdateCurrentTimeRunnable != null) {
            mHandler.removeCallbacks(mUpdateCurrentTimeRunnable);
            mUpdateCurrentTimeRunnable = null;
        }
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

        setTrackArtists((TextView) findViewById(R.id.trackArtists));
        setTrackName((TextView) findViewById(R.id.trackName));
        setTrackProgress((SeekBar) findViewById(R.id.seekBar));
        setResumeButton(findViewById(R.id.resumeButton));
        setPauseButton(findViewById(R.id.pauseButton));
        setNextButton(findViewById(R.id.nextButton));
    }

    private long updateCurrentTime() {
        final Calendar cal = Calendar.getInstance();

        // calculate current time and time to next minute
        final int secs = cal.get(Calendar.SECOND);
        int secsToNextMinute = 60 - secs;
        if (secsToNextMinute < 2) { // allow for some slack in scheduling
            cal.add(Calendar.MINUTE, 1);
            secsToNextMinute += 60;
        }

        final String time = String.format("%d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
        mCurrentTime.setText(time);

        return secsToNextMinute * 1000;
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
