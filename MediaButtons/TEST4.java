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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

public class MainActivity extends AppCompatActivity {
    private static final String CHANNEL_ID = "play_channel";
    private static final String LOG_TAG = "MainActivity";
    private static final int NOTIFICATION_ID = 1;
    private ExoPlayer player;
    private MediaSession mediaSession;
    private boolean isPlaying;

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

        updateNotification();
    }

    private class MySessionCallback extends MediaSession.Callback {
        @Override
        public void onPlay() {
            Log.d("MainActivity", "dispatchKeyEvent called onPlay");
            showToast("Play button clicked");
            isPlaying = true;
            updateNotification();
        }

        @Override
        public void onPause() {
            Log.d("MainActivity", "dispatchKeyEvent called onPause");
            player.pause();
            showToast("Pause button clicked");
            isPlaying = false;
            updateNotification();
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void updateNotification() {
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
        toggleIntent.setAction("TOGGLE_ACTION");
        PendingIntent togglePendingIntent = PendingIntent.getActivity(this, 0, toggleIntent, PendingIntent.FLAG_IMMUTABLE);
        if (isPlaying) {
            builder.addAction(R.drawable.ic_pause, "Pause", togglePendingIntent);
            player.play();
        } else {
            builder.addAction(R.drawable.ic_play, "Play", togglePendingIntent);
            player.pause();
        }

        builder.setOngoing(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(NOTIFICATION_ID, builder.build());
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
