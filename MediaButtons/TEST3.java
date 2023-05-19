package com.nitesh.myapplication;

import static androidx.core.content.PackageManagerCompat.LOG_TAG;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class MainActivity extends AppCompatActivity {
    MediaSession mediaSession;
    PlaybackState.Builder stateBuilder;
    private static final String CHANNEL_ID = "play_channel";
    private static final String LOG_TAG = "MainActivity";
    private static final int NOTIFICATION_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createNotificationChannel();

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

        if (getIntent() != null && getIntent().getAction() != null && getIntent().getAction().equals("PLAY_ACTION")) {
            Log.d("sdfaksbfd", "asdfasdf");

        }



        // Create custom notification layout
        //RemoteViews notificationLayout = new RemoteViews(getPackageName(), R.layout.notification_layout);

        // Set up the play button click event
        // notificationLayout.setOnClickPendingIntent(R.id.play_button, getPendingIntent());

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

        Intent playIntent = new Intent(this, MainActivity.class);
        playIntent.setAction("PLAY_ACTION");
        PendingIntent playPendingIntent = PendingIntent.getActivity(this, 0, playIntent, PendingIntent.FLAG_IMMUTABLE);
        builder.addAction(R.drawable.ic_play, "Play", playPendingIntent);

        builder.setOngoing(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_NOTIFICATION_POLICY) != PackageManager.PERMISSION_GRANTED) {

        }
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private static class MySessionCallback extends MediaSession.Callback {
        private final MainActivity activity;

        public MySessionCallback(MainActivity activity) {
            this.activity = activity;
        }
        @Override
        public void onPlay() {
            Log.d("MainActivity", "dispatchKeyEvent called onPlay");

            showToast("Play button clicked");
        }

        @Override
        public void onPause() {
            Log.d("MainActivity", "dispatchKeyEvent called onPause");

            showToast("Pause button clicked");
        }

        private void showToast(String message) {
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
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
