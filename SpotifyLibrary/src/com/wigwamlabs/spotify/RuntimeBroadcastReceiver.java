package com.wigwamlabs.spotify;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class RuntimeBroadcastReceiver extends BroadcastReceiver {
    private final Session mSession;

    public RuntimeBroadcastReceiver(Session session) {
        mSession = session;
    }

    public IntentFilter getFilter() {
        final IntentFilter filter = new IntentFilter();
        filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        return filter;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(action)) {
            mSession.getPlayer().pauseNoisy();
        } else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
            mSession.updateConnectionType();
        }
    }
}
