package com.nitesh.myapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;


public class MainActivity extends AppCompatActivity {
    private static ExoPlayer player;
    MediaSession mediaSession;
    PlaybackState.Builder stateBuilder;
    private static final String LOG_TAG = "MainActivity";
    private static final String CHANNEL_ID = "my_channel_id";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PlayerView playerView = findViewById(R.id.player_view);
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);
        MediaItem mediaItem = MediaItem.fromUri("https://storage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4");
        player.setMediaItem(mediaItem);
        // Prepare the player
        player.prepare();
        //player.play();


        mediaSession = new MediaSession(this, LOG_TAG);

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


        createNotificationChannel();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mediaSession.setActive(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mediaSession.setActive(false);
    }
    //End of OnCreate
    private static class MySessionCallback extends MediaSession.Callback {
        @Override
        public void onPlay() {
            Log.d("MainActivity", "dispatchKeyEvent called onPlay");
            player.play();
        }

        @Override
        public void onPause() {
            Log.d("MainActivity", "dispatchKeyEvent called onPause");
            player.pause();
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
