package com.itachi1706.cheesecakeutilities.Modules.LyricFinder;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.itachi1706.cheesecakeutilities.Objects.ApiResult;
import com.itachi1706.cheesecakeutilities.R;

import java.lang.ref.WeakReference;

import static com.itachi1706.cheesecakeutilities.Modules.LyricFinder.NowPlaying.LYRIC_ALBUMART;
import static com.itachi1706.cheesecakeutilities.Modules.LyricFinder.NowPlaying.LYRIC_DATA;

public class LyricFinderActivity extends AppCompatActivity {

    private TextView title, album, artist, state, lyrics;
    private ImageView albumart;

    private static final String TAG = "LyricFinder";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lyric_finder);

        title = findViewById(R.id.now_playing_title);
        album = findViewById(R.id.now_playing_album);
        artist = findViewById(R.id.now_playing_artist);
        albumart = findViewById(R.id.now_playing_album_art);
        state = findViewById(R.id.now_playing_state);
        lyrics = findViewById(R.id.lyrics_view);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            registerReceiver(mReceiver, registerActions());
        else requestAndRegisterMediaController();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            unregisterReceiver(mReceiver);
        else {
            if (receiver != null) {
                this.unregisterReceiver(receiver);
                receiver = null;
            }
        }
    }

    // Lyrics Handling
    static class LyricHandler extends Handler {
        WeakReference<LyricFinderActivity> mActivity;

        LyricHandler(LyricFinderActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            LyricFinderActivity activity = mActivity.get();

            super.handleMessage(msg);

            switch (msg.what) {
                case RetrieveLyricTask.LYRIC_TASK_COMPLETE:
                    if (activity == null) break; // Activity died for some reason, dont do anything
                    if (!msg.getData().getBoolean("result")) {
                        String error = msg.getData().getString("error");
                        activity.lyrics.setText("Error Retrieving Lyrics: " + error);
                        return;
                    }
                    String data = msg.getData().getString("data");
                    Gson gson = new Gson();
                    ApiResult result = gson.fromJson(data, ApiResult.class);

                    switch (result.getError()) {

                        case 0:
                            activity.lyrics.setText(result.getMsg().replace("<br>","\n").trim()); break;
                        case 1: activity.lyrics.setText("No Lyrics Found"); break;
                        case -1:
                        default: activity.lyrics.setText("Error occurred communicating with the server"); break;
                    }
                    break;
            }
        }
    }

    // Post Android 5
    private boolean notificationAccessEnabled() {
        ComponentName cn = new ComponentName(getApplication(), LyricNotificationListener.class);
        String flat = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        return flat != null && flat.contains(cn.flattenToString());
    }

    private void registerMediaController() {
        if (receiver == null) receiver = new DataReceiver();
        IntentFilter filter = new IntentFilter(LYRIC_DATA);
        this.registerReceiver(receiver, filter);
        Log.i(TAG, "Request metadata update");
        sendBroadcast(new Intent(NowPlaying.LYRIC_UPDATE));
    }

    private class DataReceiver extends BroadcastReceiver {

        DataReceiver(){}

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Receieved broadcast");
            NowPlaying obj = new NowPlaying(intent);
            album.setText(obj.getAlbum());
            artist.setText(obj.getArtist());
            title.setText(obj.getTitle());
            state.setText(obj.getStateString());
            if (intent.hasExtra(LYRIC_ALBUMART)) {
                // Retrieve bitmap #hardcoded yay :D
                String uri = "content://com.itachi1706.cheesecakeutilities.appupdater.provider/image/albumart.png";
                if (intent.getBooleanExtra(LYRIC_ALBUMART, false)) {
                    Uri bitmapUri = Uri.parse(uri);
                    albumart.setImageDrawable(null);
                    albumart.setImageURI(bitmapUri);
                }
            }
            else albumart.setImageResource(R.mipmap.ic_launcher_old);

            // Request for lyrics
            if (!obj.getTitle().equals("Unknown Title") && !obj.getArtist().equals("Unknown Artist"))
                new RetrieveLyricTask(new LyricHandler(LyricFinderActivity.this)).execute(obj.getTitle(), obj.getArtist());
            else
                lyrics.setText("No Lyrics Available for this media");
        }
    }

    DataReceiver receiver = null;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void requestAndRegisterMediaController() {
        if (notificationAccessEnabled()) registerMediaController();
        else {
            new AlertDialog.Builder(this).setTitle("Notification Listener Permission Required")
                    .setMessage("In order to retrieve track information, we require access to view the now playing notification")
                    .setNeutralButton(android.R.string.cancel, (dialog, which) -> {
                        Toast.makeText(getApplicationContext(), "Permission not granted, exiting utility", Toast.LENGTH_LONG).show();
                        finish();
                    }).setPositiveButton("GRANT PERMISSION", (dialog, which) ->
                    startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))).setCancelable(false).show();
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
            String artists;
            String tracks;
            if (action.equals("com.amazon.mp3.metachanged")) {
                artists = intent.getStringExtra("com.amazon.mp3.artist");
                tracks = intent.getStringExtra("com.amazon.mp3.track");
            } else {
                artists = intent.getStringExtra("artist");
                tracks = intent.getStringExtra("track");

            }
            String albums = intent.getStringExtra("album");
            Log.v(TAG, artists + ":" + albums + ":" + tracks);
            Toast.makeText(getApplicationContext(), tracks, Toast.LENGTH_SHORT).show();
            artist.setText(artists);
            title.setText(tracks);
            album.setText(albums);
        }
    };
}
