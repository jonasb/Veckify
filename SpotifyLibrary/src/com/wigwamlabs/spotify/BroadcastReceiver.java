package com.wigwamlabs.spotify;

import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

public class BroadcastReceiver extends android.content.BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (Intent.ACTION_MEDIA_BUTTON.equals(action)) {
            final KeyEvent keyEvent = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (keyEvent == null) {
                return;
            }

            handleMediaButton(context, keyEvent);
        }
    }

    private void handleMediaButton(Context context, KeyEvent keyEvent) {
        if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyEvent.getKeyCode()) {
            case KeyEvent.KEYCODE_HEADSETHOOK:
                Debug.logMediaButton("headset hook");
                sendActionToService(context, SpotifyService.ACTION_TOGGLE_PAUSE);
                break;
            case KeyEvent.KEYCODE_MEDIA_NEXT:
                Debug.logMediaButton("next");
                sendActionToService(context, SpotifyService.ACTION_NEXT);
                break;
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
                Debug.logMediaButton("pause");
                sendActionToService(context, SpotifyService.ACTION_PAUSE);
                break;
            case KeyEvent.KEYCODE_MEDIA_PLAY:
                Debug.logMediaButton("play");
                sendActionToService(context, SpotifyService.ACTION_RESUME);
                break;
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                Debug.logMediaButton("play/pause");
                sendActionToService(context, SpotifyService.ACTION_TOGGLE_PAUSE);
                break;
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                Debug.logMediaButton("previous");
                break;
            case KeyEvent.KEYCODE_MEDIA_STOP:
                Debug.logMediaButton("stop");
                sendActionToService(context, SpotifyService.ACTION_PAUSE); //TODO add support for stop
                break;
            default:
                Debug.logMediaButton("unknown: " + keyEvent);
                break;
            }
        }
    }

    private void sendActionToService(Context context, String action) {
        final Intent intent = new Intent(context, SpotifyService.class);
        intent.setAction(action);
        context.startService(intent);
    }
}
