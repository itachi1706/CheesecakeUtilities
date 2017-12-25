package com.itachi1706.cheesecakeutilities.Modules.LyricFinder;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.util.List;

@SuppressLint("OverrideAbstract")
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class LyricNotificationListener extends NotificationListenerService {
    public LyricNotificationListener() {
    }

    private static MediaSessionManager mm;
    private static final String TAG = "LyricService";
    private static boolean processing = false;

    @Override
    public void onCreate() {
        super.onCreate();
        mm = (MediaSessionManager) this.getSystemService(Context.MEDIA_SESSION_SERVICE);

        scanForControllers();
    }

    private void scanForControllers() {
        List<MediaController> controllers = mm.getActiveSessions(
                new ComponentName(this, LyricNotificationListener.class));
        Log.i(TAG, "Found " + controllers.size() + " controllers");
        if (controllers.size() >= 1) processController(controllers.get(0));
        else processing = false;
        //noinspection UnusedAssignment
        controllers = null; // GC
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void processController(MediaController controller) {
        // Retrieve data
        if (nowPlaying == null) nowPlaying = new NowPlaying();
        processMetadata(controller.getMetadata());
        if (controller.getPlaybackState() != null) {
            processPlaybackState(controller.getPlaybackState());
        } else
            nowPlaying.setState(NowPlaying.STOP);
        updateData();
        processing = false;

        Log.i(TAG, "Data retrieved");
    }

    private static NowPlaying nowPlaying = null;

    private void updateData() {
        Log.i(TAG, nowPlaying.getArtist() + " | " + nowPlaying.getTitle() + " | " + nowPlaying.getAlbum()
                + " | " + nowPlaying.getStateString());
    }

    private void processPlaybackState(@NonNull PlaybackState state) {
        switch (state.getState()) {
            case PlaybackState.STATE_PAUSED:
                nowPlaying.setState(NowPlaying.PAUSE);
                break;
            case PlaybackState.STATE_PLAYING:
                nowPlaying.setState(NowPlaying.PLAY);
                break;
            case PlaybackState.STATE_NONE:
            case PlaybackState.STATE_STOPPED:
                nowPlaying.setState(NowPlaying.STOP);
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
        Log.i(TAG, "Notification Posted ID : " + sbn.getId() + " | Time: " + sbn.getPostTime());
        if (!processing) {
            processing = true;
            scanForControllers();
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.i(TAG,"Notification Removed ID : " + sbn.getId() + " | Time: " + sbn.getPostTime());
        if (!processing) {
            processing = true;
            scanForControllers();
        }
    }
}
