package com.itachi1706.cheesecakeutilities.Modules.LyricFinder;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@SuppressLint("OverrideAbstract")
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class LyricNotificationListener extends NotificationListenerService {

    private static MediaSessionManager mm;
    private static final String TAG = "LyricService";
    private static boolean processing = false;

    public LyricNotificationListener() {
        // Constructor that is needed just for show
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mm = (MediaSessionManager) this.getSystemService(Context.MEDIA_SESSION_SERVICE);

        scanForControllers();
        if (receiver == null) receiver = new UpdateReceiver();
        IntentFilter filter = new IntentFilter(NowPlaying.Companion.getLYRIC_UPDATE());
        this.registerReceiver(receiver, filter);
    }

    private void scanForControllers() {
        List<MediaController> controllers = mm.getActiveSessions(
                new ComponentName(this, LyricNotificationListener.class));
        Log.d(TAG, "Found " + controllers.size() + " controllers");
        if (controllers.size() >= 1) processController(controllers.get(0));
        else processing = false;
        //noinspection UnusedAssignment
        controllers = null; // GC
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (receiver != null) this.unregisterReceiver(receiver);
    }

    private void processController(MediaController controller) {
        // Retrieve data
        if (nowPlaying == null) nowPlaying = new NowPlaying();
        processMetadata(controller.getMetadata());
        if (controller.getPlaybackState() != null) {
            processPlaybackState(controller.getPlaybackState());
        } else
            nowPlaying.setState(NowPlaying.Companion.getSTOP());
        updateData();
        processing = false;

        Log.d(TAG, "Data Retrival Complete");
    }

    private static NowPlaying nowPlaying = null;

    private void updateData() {
        Log.i(TAG, nowPlaying.getArtist() + " | " + nowPlaying.getTitle() + " | " + nowPlaying.getAlbum()
                + " | " + nowPlaying.getStateString());
        sendBroadcastData();
    }

    private void processPlaybackState(@NonNull PlaybackState state) {
        switch (state.getState()) {
            case PlaybackState.STATE_PAUSED:
                nowPlaying.setState(NowPlaying.Companion.getPAUSE());
                break;
            case PlaybackState.STATE_PLAYING:
                nowPlaying.setState(NowPlaying.Companion.getPLAY());
                break;
            case PlaybackState.STATE_NONE:
            case PlaybackState.STATE_STOPPED:
            default:
                nowPlaying.setState(NowPlaying.Companion.getSTOP());
                break;
        }
    }

    private void processMetadata(@Nullable MediaMetadata metadata) {
        nowPlaying.setAlbum(metadata.getString(MediaMetadata.METADATA_KEY_ALBUM));
        nowPlaying.setTitle(metadata.getString(MediaMetadata.METADATA_KEY_TITLE));
        nowPlaying.setArtist(metadata.getString(MediaMetadata.METADATA_KEY_ARTIST));
        nowPlaying.setAlbumart(metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART));
    }

    // Unused
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        processNotification(sbn, "Posted");
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        processNotification(sbn, "Removed");
    }

    private void processNotification(StatusBarNotification sbn, String state) {
        Log.i(TAG,"Notification " + state + " ID: " + sbn.getId() + " | Time: " + sbn.getPostTime() + " | " + sbn.getPackageName());
        if (sbn.getPackageName().equals(getPackageName())) return; // Don't process own notifications
        if (!processing) {
            processing = true;
            scanForControllers();
        }
    }

    private class UpdateReceiver extends BroadcastReceiver {

        UpdateReceiver(){}

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Receieved broadcast, updating metadata");
            sendBroadcastData();
        }
    }

    private void sendBroadcastData() {
        if (nowPlaying == null) nowPlaying = new NowPlaying();
        Intent intent = nowPlaying.generateIntent();
        if (nowPlaying.getAlbumart() != null) {
            intent.putExtra(NowPlaying.Companion.getLYRIC_ALBUMART(), saveImageTmpAndGetUri() != null);
        }
        sendBroadcast(intent);
        Log.i(TAG, "Metadata Update Broadcast Sent");
    }

    private Uri saveImageTmpAndGetUri() {
        //noinspection ConstantConditions
        File cache = new File(getExternalCacheDir(), "images_cache");
        //noinspection ResultOfMethodCallIgnored
        cache.mkdirs();
        try {
            FileOutputStream s = new FileOutputStream(cache + "/albumart.png");
            nowPlaying.getAlbumart().compress(Bitmap.CompressFormat.PNG, 100, s);
            s.close();
        } catch (IOException e) {
            Log.e(TAG, "Failed to create temp barcode file");
            e.printStackTrace();
            return null;
        }

        File shareFile = new File(cache, "albumart.png");
        Uri contentUri = FileProvider.getUriForFile(this, this.getPackageName()
                + ".appupdater.provider", shareFile);

        if (contentUri == null) {
            Log.e(TAG, "Failed to share file, invalid contentUri");
            return null;
        }

        return contentUri;
    }

    UpdateReceiver receiver = null;
}
