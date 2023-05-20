package com.nitesh.myapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

public class MainActivity extends AppCompatActivity {
    private static final String CHANNEL_ID = "play_channel";
    private static final String LOG_TAG = "MainActivity";
    private static final int NOTIFICATION_ID = 1;

    private MediaSession mediaSession;
    private ExoPlayer exoplayer;
    private boolean isPlaying;

    private Intent toggleIntent;
    private PendingIntent togglePendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createNotificationChannel();
        createPlayer();

        mediaSession = new MediaSession(this, LOG_TAG);
        mediaSession.setFlags(
                MediaSession.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setMediaButtonReceiver(null);
        PlaybackState.Builder stateBuilder = new PlaybackState.Builder()
                .setActions(
                        PlaybackState.ACTION_PLAY |
                                PlaybackState.ACTION_PAUSE);
        mediaSession.setPlaybackState(stateBuilder.build());
        mediaSession.setCallback(new MySessionCallback());

        // Create an intent for the play action
        Intent playIntent = new Intent(this, MainActivity.class);
        playIntent.setAction("PLAY_ACTION");
        togglePendingIntent = PendingIntent.getActivity(this, 0, playIntent, PendingIntent.FLAG_IMMUTABLE);

        // Create an intent for the pause action
        Intent pauseIntent = new Intent(this, MainActivity.class);
        pauseIntent.setAction("PAUSE_ACTION");
        togglePendingIntent = PendingIntent.getActivity(this, 0, pauseIntent, PendingIntent.FLAG_IMMUTABLE);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if ("TOGGLE_ACTION".equals(intent.getAction())) {
            Log.d("Logged", "got intent");

        }
    }


    private void createPlayer() {
        if (exoplayer == null) {
            exoplayer = new ExoPlayer.Builder(this).build();
            PlayerView playerView = findViewById(R.id.player_view);
            playerView.setPlayer(exoplayer);
            MediaItem mediaItem = MediaItem.fromUri("https://storage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4");
            exoplayer.setMediaItem(mediaItem);
            exoplayer.prepare();
            exoplayer.addListener(new Player.Listener() {
                @Override
                public void onPlayWhenReadyChanged(boolean playWhenReady, int reason) {
                    if (playWhenReady) {
                        exoplayer.play();
                        isPlaying = true;

                        updateNotification();
                        Log.d("MainActivity", "Playing");
                    } else {
                        isPlaying = false;
                        exoplayer.pause();
                        updateNotification();
                        Log.d("MainActivity", "Paused");
                    }
                }
            });
        }
    }

    private class MySessionCallback extends MediaSession.Callback {
        @Override
        public void onPlay() {
            isPlaying = true;
            exoplayer.play();
            updateNotification();
        }

        @Override
        public void onPause() {
            isPlaying = false;
            exoplayer.pause();
            updateNotification();
        }
    }

    private void updateNotification() {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .setDeleteIntent(mediaSession.getController().getSessionActivity())
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(MediaSessionCompat.Token.fromToken(mediaSession.getSessionToken()))
                        .setShowActionsInCompactView(0)); // Position of the play/pause button
        builder.setOngoing(true);


        if (exoplayer.isPlaying()) {
            builder.addAction(R.drawable.ic_pause, "Pause", togglePendingIntent);
            Log.d("messaging", "updateNotification_is_play" + togglePendingIntent);

        } else if (!exoplayer.isPlaying()){
            builder.addAction(R.drawable.ic_play, "Play", togglePendingIntent);
            Log.d("messaging", "updateNotification_is_pause" + togglePendingIntent);

        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(NOTIFICATION_ID, builder.build());

    }

    private void releasePlayer() {
        if (exoplayer != null) {
            exoplayer.stop();
            exoplayer.release();
            exoplayer = null;
        }

    }




    private void createNotificationChannel() {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
