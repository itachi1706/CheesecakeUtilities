package com.itachi1706.cheesecakeutilities.modules.LyricFinder;

import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.palette.graphics.Palette;

import com.google.gson.Gson;
import com.itachi1706.cheesecakeutilities.BaseBroadcastReceiver;
import com.itachi1706.cheesecakeutilities.BaseModuleActivity;
import com.itachi1706.cheesecakeutilities.objects.ApiResult;
import com.itachi1706.cheesecakeutilities.R;
import com.itachi1706.cheesecakeutilities.util.CommonMethods;
import com.itachi1706.cheesecakeutilities.util.LogHelper;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

public class LyricFinderActivity extends BaseModuleActivity {

    private TextView title, album, artist, state, lyrics;
    private ImageView albumart;
    private RelativeLayout nowPlayingLayout, mainLayout;

    private ColorStateList medColor, smallColor;
    private int windowColor, systemui;

    private static final String TAG = "LyricFinder";

    @Override
    @NonNull
    public String getHelpDescription() {
        String msg = "Shows the lyrics (if available) of the currently playing media";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            msg += "\n\nRequires the Notification Listener permission to use. " +
                    "Access the notification listener settings page from the options menu";
        return msg;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lyric_finder);

        title = findViewById(R.id.now_playing_title);
        album = findViewById(R.id.now_playing_album);
        artist = findViewById(R.id.now_playing_artist);
        lyrics = findViewById(R.id.lyrics_view);

        // Reduce variables initialized pre lollipop
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            albumart = findViewById(R.id.now_playing_album_art);
            state = findViewById(R.id.now_playing_state);
            nowPlayingLayout = findViewById(R.id.now_playing_layout);
            mainLayout = findViewById(R.id.main_lyrics_layout);

            medColor = title.getTextColors();
            smallColor = artist.getTextColors();
            windowColor = getWindow().getNavigationBarColor();
            systemui = getWindow().getDecorView().getSystemUiVisibility();
        }
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
                    if (activity == null)
                        break; // Activity died for some reason, dont do anything
                    if (!msg.getData().getBoolean("result")) {
                        String error = msg.getData().getString("error");
                        activity.lyrics.setText("Error Retrieving Lyrics: " + error);
                        return;
                    }
                    String data = msg.getData().getString("data");
                    Gson gson = new Gson();
                    ApiResult result = gson.fromJson(data, ApiResult.class);
                    if (result == null) {
                        activity.lyrics.setText("Error occurred communicating with the server. Try again later");
                        break;
                    }

                    switch (result.getError()) {
                        case 0:
                            activity.lyrics.setText(result.getMsg().replace("<br>", "\n").trim());
                            break;
                        case 2:
                            activity.lyrics.setText(result.getMsg().replace("<br>", "\n").trim() + "\n\nCached on server");
                            break;
                        case 1:
                            activity.lyrics.setText("No Lyrics Found");
                            break;
                        case -1:
                        default:
                            activity.lyrics.setText("Error occurred communicating with the server");
                            break;
                    }
                    break;
            }
        }
    }

    private void findLyrics(String artist, String title) {
        if (!title.equals("Unknown Title") && !artist.equals("Unknown Artist"))
            new RetrieveLyricTask(new LyricHandler(LyricFinderActivity.this)).execute(title, artist);
        else
            lyrics.setText("No Lyrics Available for this media");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) getMenuInflater().inflate(R.menu.modules_lyrics, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.notification_listener:
                startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Post Android 5
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private boolean notificationAccessEnabled() {
        ComponentName cn = new ComponentName(getApplication(), LyricNotificationListener.class);
        String flat = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        return flat != null && flat.contains(cn.flattenToString());
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private void registerMediaController() {
        if (receiver == null) receiver = new DataReceiver();
        IntentFilter filter = new IntentFilter(NowPlaying.Companion.getLYRIC_DATA());
        this.registerReceiver(receiver, filter);
        LogHelper.i(TAG, "Request metadata update");
        sendBroadcast(new Intent(NowPlaying.Companion.getLYRIC_UPDATE()));
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private class DataReceiver extends BaseBroadcastReceiver {

        DataReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            super.onReceive(context, intent);
            LogHelper.i(TAG, "Receieved broadcast");
            NowPlaying obj = new NowPlaying(intent);
            album.setText(obj.getAlbum());
            artist.setText(obj.getArtist());
            title.setText(obj.getTitle());
            state.setText(obj.getStateString());
            if (intent.hasExtra(NowPlaying.Companion.getLYRIC_ALBUMART())) {
                // Retrieve bitmap #hardcoded yay :D
                String uri = "content://com.itachi1706.cheesecakeutilities.provider/image/albumart.png";
                if (intent.getBooleanExtra(NowPlaying.Companion.getLYRIC_ALBUMART(), false)) {
                    Uri bitmapUri = Uri.parse(uri);
                    Drawable newImage, defaultImage = new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_old));
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(bitmapUri);
                        newImage = BitmapDrawable.createFromStream(inputStream, bitmapUri.toString());
                    } catch (FileNotFoundException e) {
                        newImage = defaultImage;
                    }
                    Drawable[] layers = new Drawable[2];
                    layers[0] = albumart.getDrawable();
                    layers[1] = newImage;
                    if (layers[0] == null) layers[0] = defaultImage;
                    if (layers[1] == null) layers[1] = defaultImage;
                    TransitionDrawable transition = new TransitionDrawable(layers);
                    albumart.setImageDrawable(transition);
                    transition.startTransition(100);

                    // Palette API LOL (Do all layout item checks see if they exist first before using this CPU intensive task)
                    if (albumart != null && nowPlayingLayout != null && album != null && title != null && state != null && artist != null
                            && albumart.getDrawable() != null && newImage instanceof BitmapDrawable && lyrics != null
                            && mainLayout != null) {
                        Bitmap toUseForPalette = ((BitmapDrawable) newImage).getBitmap();
                        new Palette.Builder(toUseForPalette)
                                .maximumColorCount(32).generate(palette -> {
                            if (getApplicationContext() == null)
                                return; // Dont bother animating without a context
                            Palette.Swatch selectedColors = palette.getDominantSwatch();
                            int bgColor = ContextCompat.getColor(getApplicationContext(), android.R.color.white);
                            if (nowPlayingLayout.getBackground() != null && nowPlayingLayout.getBackground() instanceof ColorDrawable)
                                bgColor = ((ColorDrawable) nowPlayingLayout.getBackground()).getColor();

                            if (selectedColors == null) {
                                LogHelper.e(TAG, "Unable to get colors, defaulting");
                                selectedColors = new Palette.Swatch(Color.rgb(255, 255, 255), 1);
                            }

                            Integer backgroundCFrom = bgColor, backgroundCTo = selectedColors.getRgb();
                            Integer textCFrom = title.getCurrentTextColor(), textCTo = selectedColors.getTitleTextColor();
                            Integer textSFrom = album.getCurrentTextColor(), textSTo = selectedColors.getBodyTextColor();
                            ValueAnimator animateBg = ValueAnimator.ofArgb(backgroundCFrom, backgroundCTo);

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                if (!CommonMethods.isColorDark(backgroundCTo))
                                    getWindow().getDecorView().setSystemUiVisibility(systemui | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
                                else
                                    getWindow().getDecorView().setSystemUiVisibility(systemui);
                            }
                            animateBg.addUpdateListener(animation -> {
                                nowPlayingLayout.setBackground(new ColorDrawable((int) animateBg.getAnimatedValue()));
                                mainLayout.setBackgroundColor((int) animateBg.getAnimatedValue());
                                getWindow().setNavigationBarColor((int) animateBg.getAnimatedValue());
                            });
                            animateBg.start();

                            ValueAnimator animateMain = ValueAnimator.ofArgb(textCFrom, textCTo);
                            animateMain.addUpdateListener(animation -> {
                                title.setTextColor((int) animateMain.getAnimatedValue());
                                lyrics.setTextColor((int) animateMain.getAnimatedValue());
                            });
                            animateMain.start();

                            ValueAnimator animateSub = ValueAnimator.ofArgb(textSFrom, textSTo);
                            animateSub.addUpdateListener(animation -> {
                                album.setTextColor((int) animateSub.getAnimatedValue());
                                artist.setTextColor((int) animateSub.getAnimatedValue());
                                state.setTextColor((int) animateSub.getAnimatedValue());
                            });
                            animateSub.start();
                        });
                    }
                }
            } else {
                albumart.setImageResource(R.mipmap.ic_launcher_old);
                // Reset palette API no animation cause lazy
                album.setTextColor(smallColor);
                title.setTextColor(medColor);
                lyrics.setTextColor(medColor);
                mainLayout.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), android.R.color.white));
                state.setTextColor(smallColor);
                artist.setTextColor(smallColor);
                nowPlayingLayout.setBackground(new ColorDrawable(ContextCompat.getColor(getApplicationContext(), android.R.color.white)));
                getWindow().setNavigationBarColor(windowColor);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    getWindow().getDecorView().setSystemUiVisibility(systemui);
            }

            // Request for lyrics
            findLyrics(obj.getArtist(), obj.getTitle());
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

    // Pre Android 5 (LOLLIPOP) implementation
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
            LogHelper.v(TAG, action + " / " + cmd);
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
            LogHelper.v(TAG, artists + ":" + albums + ":" + tracks);
            Toast.makeText(getApplicationContext(), tracks, Toast.LENGTH_SHORT).show();
            artist.setText(artists);
            title.setText(tracks);
            album.setText(albums);
            findLyrics(artists, tracks);
        }
    };
}
