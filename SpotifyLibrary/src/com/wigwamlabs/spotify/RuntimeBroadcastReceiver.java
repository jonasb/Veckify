package com.wigwamlabs.spotify;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;

public class RuntimeBroadcastReceiver extends StaticBroadcastReceiver {
    private final Player mPlayer;

    public RuntimeBroadcastReceiver(Player player) {
        mPlayer = player;
    }

    public IntentFilter getFilter() {
        final IntentFilter filter = new IntentFilter();
        filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        return filter;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(action)) {
            mPlayer.pauseNoisy();
        }
    }
}
