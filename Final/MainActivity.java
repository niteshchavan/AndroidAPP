package com.nitesh.myapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
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
    private static MediaSession mediaSession;
    PlaybackState.Builder stateBuilder;
    private static final String CHANNEL_ID = "play_channel";
    private static final String LOG_TAG = "MainActivity";
    private static final int NOTIFICATION_ID = 1;
    private static ExoPlayer player;
    private static boolean isPlaying;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createNotificationChannel();

        PlayerView playerView = findViewById(R.id.player_view);
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);
        MediaItem mediaItem = MediaItem.fromUri("https://storage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4");
        player.setMediaItem(mediaItem);
        // Prepare the player
        player.prepare();

        player.addListener(new Player.Listener() {
            @Override
            public void onPlayWhenReadyChanged(boolean playWhenReady, int reason) {
                updatePlayerState(playWhenReady);
            }
        });

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
        updateNotification();
    }
    private class MySessionCallback extends MediaSession.Callback {
        @Override
        public void onPlay() {
            updatePlayerState(true);
        }
        @Override
        public void onPause() {
            updatePlayerState(false);
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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        if (getIntent() != null && getIntent().getAction() != null && getIntent().getAction().equals("PLAY_ACTION")) {
            updatePlayerState(!isPlaying);
        }
    }
    private void updateNotification(){

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Song Title")
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .setDeleteIntent(mediaSession.getController().getSessionActivity())
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(MediaSessionCompat.Token.fromToken(mediaSession.getSessionToken()))
                        .setShowActionsInCompactView(0)); // Position of the play/pause button

        Intent toggleIntent = new Intent(this, MainActivity.class);
        toggleIntent.setAction("PLAY_ACTION");
        toggleIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent togglePendingIntent = PendingIntent.getActivity(this, 0, toggleIntent, PendingIntent.FLAG_IMMUTABLE);
        if (isPlaying) {
            builder.addAction(R.drawable.ic_pause, "Pause", togglePendingIntent);
        } else {
            builder.addAction(R.drawable.ic_play, "Play", togglePendingIntent);
        }
        builder.setOngoing(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_NOTIFICATION_POLICY);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }


    private void updatePlayerState(boolean playWhenReady) {
        if (playWhenReady) {
            player.play();
            isPlaying = true;
        } else {
            player.pause();
            isPlaying = false;
        }
        updateNotification();
    }
}