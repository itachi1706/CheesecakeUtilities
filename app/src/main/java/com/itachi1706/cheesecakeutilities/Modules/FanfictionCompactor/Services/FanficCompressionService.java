package com.itachi1706.cheesecakeutilities.Modules.FanfictionCompactor.Services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.itachi1706.cheesecakeutilities.FanfictionCompactorActivity;
import com.itachi1706.cheesecakeutilities.Modules.FanfictionCompactor.Broadcasts.FanficBroadcast;
import com.itachi1706.cheesecakeutilities.Modules.FanfictionCompactor.FileHelper;
import com.itachi1706.cheesecakeutilities.Modules.FanfictionCompactor.Objects.FanficNotificationObject;
import com.itachi1706.cheesecakeutilities.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by Kenneth on 1/6/2016.
 * for com.itachi1706.cheesecakeutilities.Modules.FanfictionCompactor.Services in CheesecakeUtilities
 */
public class FanficCompressionService extends IntentService{

    private static final int NOTIFY_ID = 201;
    private static final String FANFIC_COMPRESSION_TAG = "FanficCompressionSvc";

    private static FanficNotificationObject fanficObj;
    private int lastStatusCode = 0;

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
        lastStatusCode = 1;

        // TODO: Start of test code
        updateNotification("Starting in 10 seconds...", "Lorum Ipsum", false, 0, 0, true);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Log.e(FANFIC_COMPRESSION_TAG, "Cannot sleep D:");
        }

        for (int i = 0; i < 10; i++) {
            try {
                Thread.sleep(1000);
                updateNotification("Round " + (i+1), "Processing...", false, i + 1, 10, true);
            } catch (InterruptedException e) {
                Log.e(FANFIC_COMPRESSION_TAG, "Cannot sleep D:");
            }
        }
        // TODO: End of test code

        updateNotification("Backing Up Existing Files...", "Backup", false, 0, 0, true);

        // TODO: Make sure to check if the user has enough space on the device for the zip file
        try {
            zipFiles();
        } catch (IOException e) {
            Log.e(FANFIC_COMPRESSION_TAG, "Failed to zip files!");
            updateNotification("File Backup failed", "File Backup Error", true, 0, 0, false);
            sendCompleteIntent();
            return;
        }

        updateNotification("Cleanup Completed", "Task Completed", true, 0, 0, false);
        sendCompleteIntent();
    }

    private void sendCompleteIntent() {
        // Send a local broadcast to close any existing dialogs
        Intent completeIntent = new Intent(FanficBroadcast.BROADCAST_ACTION);
        completeIntent.putExtra(FanficBroadcast.BROADCAST_DATA_DONE, true);
        LocalBroadcastManager.getInstance(this).sendBroadcast(completeIntent);
    }

    private String zipfile;

    private void zipFiles() throws IOException {
        File backupFolder = FileHelper.getBackupFolder();
        File toBackup = FileHelper.getDefaultFolder();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss.SSS", Locale.US);
        Date date = new Date();
        String dateString = sdf.format(date);
        zipfile = "fanfic-backup-" + dateString + ".zip";
        String filepath = backupFolder.getAbsolutePath() + File.separator + zipfile;
        updateNotification("Backing up to " + zipfile, "Backup", true, 0, 0, true);
        ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(filepath));

        mainfile = new File(toBackup.getAbsolutePath().substring(0, toBackup.getAbsolutePath().lastIndexOf("/")));
        addDir(toBackup, zipOutputStream);
        zipOutputStream.close();
    }

    private int totalFiles = 0, currentFile = 1;
    private static File mainfile;

    private void addDir(File dirObj, ZipOutputStream out) throws IOException {
        File[] files = dirObj.listFiles();
        totalFiles += files.length;
        byte[] tmpBuf = new byte[1024];

        for (File file : files) {
            if (file.isDirectory()) {
                Log.i(FANFIC_COMPRESSION_TAG, "Zipping folder " + file.getAbsolutePath());
                addDir(file, out);
                continue;
            }
            FileInputStream in = new FileInputStream(file.getAbsolutePath());
            fanficObj = new FanficNotificationObject("Backing Up to " + zipfile + ":\n " + file.getAbsolutePath(), "Backup", true, currentFile, totalFiles, false, "Backing up files...");
            updateNotification();
            Log.d(FANFIC_COMPRESSION_TAG, "Zipping: " + file.getAbsolutePath());
            out.putNextEntry(new ZipEntry(mainfile.toURI().relativize(file.toURI()).getPath()));
            int len;
            while ((len = in.read(tmpBuf)) > 0) {
                out.write(tmpBuf, 0, len);
            }
            out.closeEntry();
            currentFile++;
            in.close();
        }
    }

    private void deleteFiles() {
        // TODO: Code Stub
    }

    protected void updateNotification(String message, String title, boolean cancellable, int progress, int max, boolean indeterminate) {
        fanficObj = new FanficNotificationObject(message,title,cancellable,progress,max,indeterminate);

        updateNotification();
    }

    private static int percentdone = 0;
    private static String lasttitle = "", lastmessage = "";
    private static boolean laststate = false;

    protected void updateNotification() {
        FanficNotificationObject obj = fanficObj;
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);

        int percent = 0;
        if (obj.getProgress() != 0 && obj.getMax() != 0) {
            percent = (int) Math.round(((double) obj.getProgress()) / ((double) obj.getMax()) * 100);
        }

        if (percent != percentdone || !lasttitle.equals(obj.getTitle()) || !lastmessage.equals(obj.getNotificationMessage()) || laststate != obj.isCancellable()) {

            percentdone = percent;
            laststate = obj.isCancellable();
            lasttitle = obj.getTitle();
            lastmessage = obj.getNotificationMessage();

            Intent fanficActivity = new Intent(this, FanfictionCompactorActivity.class);
            fanficActivity.putExtra("launchNotification", true);
            fanficActivity.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, NOTIFY_ID, fanficActivity, 0);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setContentTitle(obj.getTitle()).setContentText(obj.getNotificationMessage()).setOngoing(!obj.isCancellable())
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(pendingIntent)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(obj.getNotificationMessage()))
                    .setProgress(obj.getMax(), obj.getProgress(), obj.isIndeterminate());

            // TODO: Remove update cause
            Log.d(FANFIC_COMPRESSION_TAG, "Updating notification");
            notificationManager.notify(NOTIFY_ID, builder.build());
        }



        Intent localIntent = new Intent(FanficBroadcast.BROADCAST_ACTION);
        localIntent.putExtra(FanficBroadcast.BROADCAST_DATA_MAX, obj.getMax());
        localIntent.putExtra(FanficBroadcast.BROADCAST_DATA_MSG, obj.getMessage());
        localIntent.putExtra(FanficBroadcast.BROADCAST_DATA_PROGRESS, obj.getProgress());
        localIntent.putExtra(FanficBroadcast.BROADCAST_DATA_TITLE, obj.getTitle());
        localIntent.putExtra(FanficBroadcast.BROADCAST_DATA_INDETERMINATE, obj.isIndeterminate());

        if (lastStatusCode != 2 && lastStatusCode != 0) {
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
        }
    }

    private class ResponseReceiver extends BroadcastReceiver {
        private ResponseReceiver() {
        }

        public void onReceive(Context context, Intent intent) {

            lastStatusCode = intent.getIntExtra(FanficBroadcast.BROADCAST_STATUS, -99);

            switch (lastStatusCode) {
                case 1: Log.i(FANFIC_COMPRESSION_TAG, "Activity Connected, sending dialog..."); updateNotification(); break;
                case 0: Log.i(FANFIC_COMPRESSION_TAG, "Activity Disconnected"); break;
                case 2: Log.i(FANFIC_COMPRESSION_TAG, "Activity Requested No Reminder"); break;
                default: Log.e(FANFIC_COMPRESSION_TAG, "Weird Status Code (" + lastStatusCode + "), ignoring"); break;
            }
        }
    }
}
