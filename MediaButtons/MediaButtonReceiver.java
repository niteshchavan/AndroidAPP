package com.example.myapp ;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

public class MediaButtonReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
            KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (event != null && event.getAction() == KeyEvent.ACTION_DOWN) {
                int keyCode = event.getKeyCode();
                if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE ||
                        keyCode == KeyEvent.KEYCODE_MEDIA_PLAY ||
                        keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
                    // Handle media play/pause key event
                    // You can dispatch the event to your activity or service here
                    // For example:
                    Intent playPauseIntent = new Intent(context, MainActivity.class);
                    playPauseIntent.setAction("PLAY_PAUSE");
                    context.startActivity(playPauseIntent);
                }
            }
        }
    }
}