package com.itachi1706.cheesecakeutilities.Modules.LyricFinder;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.itachi1706.cheesecakeutilities.R;

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
            // TODO: Unregister broadcast receiver
        }
    }

    // Post Android 5
    private boolean notificationAccessEnabled() {
        ComponentName cn = new ComponentName(getApplication(), LyricNotificationListener.class);
        String flat = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        return flat != null && flat.contains(cn.flattenToString());
    }

    private void registerMediaController() {
        // TODO: Broadcast receiver register
        // TODO: Request for current metadata
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
