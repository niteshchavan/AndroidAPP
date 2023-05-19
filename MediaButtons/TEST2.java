package com.nitesh.myapplication;

import android.Manifest;
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
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;


public class MainActivity extends AppCompatActivity {
    private static ExoPlayer player;
    MediaSession mediaSession;
    PlaybackState.Builder stateBuilder;
    private static final String LOG_TAG = "MainActivity";
    private static final String CHANNEL_ID = "my_channel_id";
    private static final int NOTIFICATION_ID = 1;

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
        mediaSession.setCallback(new MySessionCallback(this));


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
        // Play button action
        builder.addAction(new NotificationCompat.Action(

                R.drawable.ic_play, "Play", mediaSession.getController().getSessionActivity()));
        //Log.d("asfsffdsaf", "onplay");
        // Pause button action
        builder.addAction(new NotificationCompat.Action(
                R.drawable.ic_pause, "Pause",mediaSession.getController().getSessionActivity()));
        //Log.d("asfsffdsaf", "onpause");


        // Play button action
        Intent playIntent = new Intent(this, MainActivity.class);
        playIntent.setAction("PLAY_ACTION");
        //PendingIntent playPendingIntent = PendingIntent.getActivity(this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent playPendingIntent = PendingIntent.getActivity(this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        builder.addAction(new NotificationCompat.Action(R.drawable.ic_play, "Play", playPendingIntent));

// Pause button action
        Intent pauseIntent = new Intent(this, MainActivity.class);
        pauseIntent.setAction("PAUSE_ACTION");
        //PendingIntent pausePendingIntent = PendingIntent.getActivity(this, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pausePendingIntent = PendingIntent.getActivity(this, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);


        builder.addAction(new NotificationCompat.Action(R.drawable.ic_pause, "Pause", pausePendingIntent));

        builder.setOngoing(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NOTIFICATION_POLICY) != PackageManager.PERMISSION_GRANTED) {

        }
        notificationManager.notify(NOTIFICATION_ID, builder.build());


        createNotificationChannel();
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent.getAction() != null) {
            if (intent.getAction().equals("PLAY_ACTION")) {
                // Handle play button click
                mediaSession.getController().getTransportControls().play();
            } else if (intent.getAction().equals("PAUSE_ACTION")) {
                // Handle pause button click
                mediaSession.getController().getTransportControls().pause();
            }
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
            if (mediaSession != null) {
                mediaSession.getController().getTransportControls().play();
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
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
        private final MainActivity activity;

        public MySessionCallback(MainActivity activity) {
            this.activity = activity;
        }
        @Override
        public void onPlay() {
            Log.d("MainActivity", "dispatchKeyEvent called onPlay");
            player.play();
            showToast("Play button clicked");
        }

        @Override
        public void onPause() {
            Log.d("MainActivity", "dispatchKeyEvent called onPause");
            player.pause();
            showToast("Pause button clicked");
        }

        private void showToast(String message) {
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
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
