package com.itachi1706.cheesecakeutilities.Modules.LyricFinder;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.itachi1706.cheesecakeutilities.R;

import java.util.List;

public class LyricFinderActivity extends AppCompatActivity {

    private TextView nowplaying;

    private static final String TAG = "LyricFinder";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lyric_finder);

        nowplaying = findViewById(R.id.now_playing);


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            registerReceiver(mReceiver, registerActions());
        else requestAndRegisterMediaController();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            unregisterReceiver(mReceiver);
        else {
            if (mm != null) mm.removeOnActiveSessionsChangedListener(listener);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    // Post Android 5
    private MediaSessionManager mm;

    private boolean notificationAccessEnabled() {
        ComponentName cn = new ComponentName(getApplication(), LyricNotificationListener.class);
        String flat = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        return flat != null && flat.contains(cn.flattenToString());
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void registerMediaController() {
        mm = (MediaSessionManager) this.getSystemService(
                Context.MEDIA_SESSION_SERVICE);
        List<MediaController> controllers = mm.getActiveSessions(
                new ComponentName(this, LyricNotificationListener.class));
        Log.i(TAG, "Found " + controllers.size() + " controllers");
        if (controllers.size() >= 1) processController(controllers.get(0));
        // Add a listener
        mm.addOnActiveSessionsChangedListener(listener, new ComponentName(this, LyricNotificationListener.class));
    }

    private MediaSessionManager.OnActiveSessionsChangedListener listener = controllers -> {
        Log.i(TAG, "Found " + controllers.size() + " controllers");
        if (controllers.size() >= 1)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                processController(controllers.get(0));
            }
    };

    private MediaController.Callback callback = new MediaController.Callback() {
        @Override
        public void onSessionDestroyed() {
            super.onSessionDestroyed();
            Log.i(TAG, "Controller session destroyed");
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onPlaybackStateChanged(@NonNull PlaybackState state) {
            super.onPlaybackStateChanged(state);
            Log.i(TAG, "Controller state changed");
            switch (state.getState()) {
                case PlaybackState.STATE_PAUSED:
                    l_state = "Paused";
                    break;
                case PlaybackState.STATE_PLAYING:
                    l_state = "Playing";
                    break;
                case PlaybackState.STATE_NONE:
                case PlaybackState.STATE_STOPPED:
                    l_state = "Stopped";
                    break;
            }
            updateData();
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onMetadataChanged(@Nullable MediaMetadata metadata) {
            super.onMetadataChanged(metadata);
            Log.i(TAG, "Controller metadata changed");
            l_album = metadata.getString(MediaMetadata.METADATA_KEY_ALBUM);
            l_title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE);
            l_display_title = metadata.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE);
            l_artist = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST);
            updateData();
        }
    };

    private static String l_album = "", l_title = "", l_display_title = "", l_artist = "", l_state = "Retrieving";

    private void updateData() {
        nowplaying.setText("Artist: " + l_artist + "\nTrack: " + l_title + "\nAlbum: " + l_album + "\nDisplay Title: " + l_display_title +
                "\nState: " + l_state);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void processController(MediaController controller) {
        controller.registerCallback(callback);
        Log.i(TAG, "Controller callback registered");
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void requestAndRegisterMediaController() {
        if (notificationAccessEnabled()) registerMediaController();
        else {
            new AlertDialog.Builder(this).setTitle("Notification Listener Permission Required")
                    .setMessage("In order to retrieve track information, we require access to view the now playing notification")
                    .setNeutralButton(android.R.string.cancel, (dialog, which) -> {
                        Toast.makeText(getApplicationContext(), "Permission not granted, exiting utility", Toast.LENGTH_LONG).show();
                        finish();
                    }).setPositiveButton("GRANT PERMISSION", (dialog, which) -> {
                startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
            }).setCancelable(false).show();
        }
    }

    // Pre Android 5
    private IntentFilter registerActions() {
        IntentFilter iF = new IntentFilter();
        iF.addAction("com.android.music.metachanged");
        iF.addAction("com.android.music.playstatechanged");
        iF.addAction("com.android.music.playbackcomplete");
        iF.addAction("com.android.music.queuechanged");

        iF.addAction("com.android.music.metachanged");

        iF.addAction("com.htc.music.metachanged");

        iF.addAction("fm.last.android.metachanged");
        iF.addAction("com.sec.android.app.music.metachanged");
        iF.addAction("com.nullsoft.winamp.metachanged");
        iF.addAction("com.amazon.mp3.metachanged");
        iF.addAction("com.miui.player.metachanged");
        iF.addAction("com.real.IMP.metachanged");
        iF.addAction("com.sonyericsson.music.metachanged");
        iF.addAction("com.rdio.android.metachanged");
        iF.addAction("com.samsung.sec.android.MusicPlayer.metachanged");
        iF.addAction("com.andrew.apollo.metachanged");
        return iF;
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String cmd = intent.getStringExtra("command");
            Log.v(TAG, action + " / " + cmd);
            String artist;
            String track;
            if (action.equals("com.amazon.mp3.metachanged")) {
                artist = intent.getStringExtra("com.amazon.mp3.artist");
                track = intent.getStringExtra("com.amazon.mp3.track");
            } else {
                artist = intent.getStringExtra("artist");
                track = intent.getStringExtra("track");

            }
            String album = intent.getStringExtra("album");
            Log.v(TAG, artist + ":" + album + ":" + track);
            Toast.makeText(getApplicationContext(), track, Toast.LENGTH_SHORT).show();
            nowplaying.setText("Artist: " + artist + "\nTrack: " + track + "\nAlbum: " + album);
        }
    };
}
