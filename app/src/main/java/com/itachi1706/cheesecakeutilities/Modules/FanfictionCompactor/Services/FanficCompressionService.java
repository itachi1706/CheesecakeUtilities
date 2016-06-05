package com.itachi1706.cheesecakeutilities.Modules.FanfictionCompactor.Services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.itachi1706.cheesecakeutilities.Modules.FanfictionCompactor.Broadcasts.FanficBroadcast;
import com.itachi1706.cheesecakeutilities.Modules.FanfictionCompactor.Objects.FanficNotificationObject;
import com.itachi1706.cheesecakeutilities.R;

/**
 * Created by Kenneth on 1/6/2016.
 * for com.itachi1706.cheesecakeutilities.Modules.FanfictionCompactor.Services in CheesecakeUtilities
 */
public class FanficCompressionService extends IntentService{

    private static final int NOTIFY_ID = 201;
    private static final String FANFIC_COMPRESSION_TAG = "FanficCompressionSvc";

    private static FanficNotificationObject fanficObj;

    private ResponseReceiver receiver;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public FanficCompressionService() {
        super(FANFIC_COMPRESSION_TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // Create Response Handler
        receiver = new ResponseReceiver();
        IntentFilter filter = new IntentFilter(FanficBroadcast.BROADCAST_NOTIFY);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
        Log.i(FANFIC_COMPRESSION_TAG, "Registered Broadcast Receiver");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Destroy Response Handler
        Log.i(FANFIC_COMPRESSION_TAG, "Unregistering Broadcast Receiver");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        updateNotification("Starting in 10 seconds...", "Lorum Ipsum", false, 0, 0, true);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Log.e(FANFIC_COMPRESSION_TAG, "Cannot sleep D:");
        }

        for (int i = 0; i < 50; i++) {
            try {
                Thread.sleep(1000);
                updateNotification("Round " + (i+1), "Processing...", false, i, 50, false);
            } catch (InterruptedException e) {
                Log.e(FANFIC_COMPRESSION_TAG, "Cannot sleep D:");
            }
        }

        updateNotification("Done!", "DONE", true, 0, 0, false);
        // Send a local broadcast to close any existing dialogs
        Intent completeIntent = new Intent(FanficBroadcast.BROADCAST_ACTION);
        completeIntent.putExtra(FanficBroadcast.BROADCAST_DATA_DONE, true);
        LocalBroadcastManager.getInstance(this).sendBroadcast(completeIntent);
    }

    protected void updateNotification(String message, String title, boolean cancellable, int progress, int max, boolean indeterminate) {
        fanficObj = new FanficNotificationObject(message,title,cancellable,progress,max,indeterminate);
        updateNotification();
    }

    protected void updateNotification() {
        FanficNotificationObject obj = fanficObj;
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle(obj.getTitle()).setContentText(obj.getMessage()).setOngoing(!obj.isCancellable())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setProgress(obj.getMax(), obj.getProgress(), obj.isIndeterminate());

        notificationManager.notify(NOTIFY_ID, builder.build());

        Intent localIntent = new Intent(FanficBroadcast.BROADCAST_ACTION);
        localIntent.putExtra(FanficBroadcast.BROADCAST_DATA_MAX, obj.getMax());
        localIntent.putExtra(FanficBroadcast.BROADCAST_DATA_MSG, obj.getMessage());
        localIntent.putExtra(FanficBroadcast.BROADCAST_DATA_PROGRESS, obj.getProgress());
        localIntent.putExtra(FanficBroadcast.BROADCAST_DATA_TITLE, obj.getTitle());
        localIntent.putExtra(FanficBroadcast.BROADCAST_DATA_INDETERMINATE, obj.isIndeterminate());

        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    private class ResponseReceiver extends BroadcastReceiver {
        private ResponseReceiver() {
        }

        public void onReceive(Context context, Intent intent) {

            int status = intent.getIntExtra(FanficBroadcast.BROADCAST_STATUS, -99);

            switch (status) {
                case 1: Log.i(FANFIC_COMPRESSION_TAG, "Activity Connected, sending dialog..."); updateNotification(); break;
                case 0: Log.i(FANFIC_COMPRESSION_TAG, "Activity Disconnected"); break;
                default: Log.e(FANFIC_COMPRESSION_TAG, "Weird Status Code (" + status + "), ignoring"); break;
            }
        }
    }
}
