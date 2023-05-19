package com.example.myapp;

import androidx.appcompat.app.AppCompatActivity;


import android.os.Bundle;

import android.media.session.MediaSession;
import android.media.session.PlaybackState;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import android.util.Log;

public class MainActivity extends AppCompatActivity {
    MediaSession mediaSession;
    PlaybackState.Builder stateBuilder;
    private static final String LOG_TAG = "MainActivity";
    private static final String CHANNEL_ID = "my_channel_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mediaSession = new MediaSession(this, LOG_TAG);
        createNotificationChannel();
        mediaSession.setFlags(
                MediaSession.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setMediaButtonReceiver(null);
        stateBuilder = new PlaybackState.Builder()
                .setActions(
                        PlaybackState.ACTION_PLAY |
                                PlaybackState.ACTION_PAUSE);
        mediaSession.setPlaybackState(stateBuilder.build());
        mediaSession.setCallback(new MySessionCallback());




    }
    //End of OnCreate
    private static class MySessionCallback extends MediaSession.Callback {
        @Override
        public void onPlay() {
            Log.d("MainActivity", "dispatchKeyEvent called onPlay");
        }

        @Override
        public void onPause() {
            Log.d("MainActivity", "dispatchKeyEvent called onPause");
        }
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "My Channel";
            String description = "My Channel Description";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            // Register the channel with the system
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
