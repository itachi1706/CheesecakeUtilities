package com.itachi1706.cheesecakeutilities.Modules.FanfictionCompactor;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.collection.ArrayMap;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.perf.metrics.AddTrace;
import com.itachi1706.appupdater.Util.DeprecationHelper;
import com.itachi1706.cheesecakeutilities.BaseBroadcastReceiver;
import com.itachi1706.cheesecakeutilities.BaseModuleActivity;
import com.itachi1706.cheesecakeutilities.Modules.FanfictionCompactor.Broadcasts.FanficBroadcast;
import com.itachi1706.cheesecakeutilities.Modules.FanfictionCompactor.Helpers.FileHelper;
import com.itachi1706.cheesecakeutilities.Modules.FanfictionCompactor.Objects.FanficStories;
import com.itachi1706.cheesecakeutilities.Modules.FanfictionCompactor.Services.FanficCompressionService;
import com.itachi1706.cheesecakeutilities.Modules.FanfictionCompactor.Storage.FanfictionDatabase;
import com.itachi1706.cheesecakeutilities.Modules.FanfictionCompactor.Tasks.ScanStorageDetails;
import com.itachi1706.cheesecakeutilities.R;
import com.itachi1706.cheesecakeutilities.Util.CommonMethods;
import com.itachi1706.cheesecakeutilities.Util.LogHelper;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import static com.itachi1706.cheesecakeutilities.Util.CommonVariables.PERM_MAN_TAG;

@Deprecated
public class FanfictionCompactorActivity extends BaseModuleActivity {

    TextView folder, database, folderSize, storyCount;
    Button startServiceBtn, storyButton, duplicateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fanfiction_compactor);

        folder = findViewById(R.id.tvFolder);
        database = findViewById(R.id.tvDB);
        folderSize = findViewById(R.id.tvSize);
        storyCount = findViewById(R.id.tvStories);
        startServiceBtn = findViewById(R.id.btnStartPruning);
        storyButton = findViewById(R.id.btnStories);
        duplicateButton = findViewById(R.id.btnRemoveDuplicates);

        startServiceBtn.setEnabled(false);
        startServiceBtn.setOnClickListener(v -> startPreCompactingService());

        storyButton.setOnClickListener(v -> getStoryList());
        duplicateButton.setOnClickListener(v -> getDuplicateList());

        folder.setText(FileHelper.getDefaultFolder().getAbsolutePath());
        database.setText(FanfictionDatabase.getDbFilePath());

        // Check Permission Exist, else exit if not granted
        hasStoragePermissionCheck();

        receiver = new ResponseReceiver();
        IntentFilter filter = new IntentFilter(FanficBroadcast.BROADCAST_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);

        if (getIntent().hasExtra("launchNotification") && getIntent().getBooleanExtra("launchNotification", false))
            notifyService(true);
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent.hasExtra("launchNotification") && intent.getBooleanExtra("launchNotification", false))
            notifyService(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        notifyService(false);
    }

    @AddTrace(name = "read_fanfic_db")
    private void getStoryList() {
        FanfictionDatabase db = new FanfictionDatabase();
        ArrayList<FanficStories> stories = db.getAllStories();
        StringBuilder builder = new StringBuilder();
        for (FanficStories story :  stories) {
            builder.append("[").append(story.getId()).append("] ").append(story.getTitle()).append(" (").append(story.getChapters()).append(")\n");
        }

        new AlertDialog.Builder(this).setTitle("Story List (" + stories.size() + ")").setMessage(builder.toString()).setPositiveButton(android.R.string.ok, null).show();
    }

    private void getDuplicateList() {
        FanfictionDatabase db = new FanfictionDatabase();
        ArrayList<FanficStories> stories = db.getAllStories();
        StringBuilder builder = new StringBuilder();
        int count = 0;
        // Parse out duplicates
        ArrayMap<Integer, FanficStories> parser = new ArrayMap<>();
        for (FanficStories s : stories) {
            if (parser.containsKey(s.getPage_id())) {
                // Duplicates
                FanficStories dupe = parser.get(s.getPage_id());
                builder.append(s.getTitle()).append(" &lt;==&gt; ").append(dupe.getTitle()).append("\n(").append(s.getPage_id()).append(")\n\n");
                count++;
            } else
                parser.put(s.getPage_id(), s);
        }

        if (count <= 0) builder.append("You have no stories that are duplicates in the database!");
        new AlertDialog.Builder(this).setTitle("Duplicates Scanner (" + count + ")").setMessage(DeprecationHelper.Html.fromHtml(builder.toString().replace("\n", "<br>"))).setPositiveButton(android.R.string.ok, null).show();
    }

    long totalSize = 9999999;

    private void startPreCompactingService() {
        new AlertDialog.Builder(this).setTitle(DeprecationHelper.Html.fromHtml("<font color='red'>WARNING!</font>"))//"WARNING!")
                .setMessage(DeprecationHelper.Html.fromHtml("This service will clean up Fanfiction Stories from your device in <font color='blue'>" +
                FileHelper.getDefaultFolder().getAbsolutePath() + "</font>.<br> A backup will be made in <font color='blue'>" +
                FileHelper.getBackupFolder().getAbsolutePath() + "</font>. <br><br><br><font color='red'>Please make sure you have " +
                        "already done a database export from the Fanfiction Application before continuing. " +
                        "Failure to do so may result in a loss of story data!</font>"))
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss())
                .setPositiveButton(android.R.string.ok, (dialog, which) -> startCompactingService()).show();
    }

    private void startCompactingService() {
        File file = FileHelper.getDefaultFolder();

        // Make sure DB and folder exists
        if (!file.exists() || !FanfictionDatabase.databaseExists()) {
            Toast.makeText(this, "Unable to compact. No stories", Toast.LENGTH_LONG).show();
            return;
        }

        float freespace = FileHelper.megabytesAvailable(file);
        LogHelper.i("MemoryCheck", "Free Space: " + freespace + " | Total Size: " + CommonMethods.readableFileSize(totalSize) + " (" + totalSize + ")");
        freespace = freespace * 1024 * 1024; // Convert from MB to Bytes
        if (freespace < totalSize) {
            // Not enough memory
            new AlertDialog.Builder(this).setTitle("Insufficient Memory")
                    .setMessage("You do not have enough memory, you need " +
                            CommonMethods.readableFileSize(totalSize - (long)freespace) + " more.\n\n" +
                            "Recommended Space: " + CommonMethods.readableFileSize(totalSize) + "\n" +
                            "Available Space: " + CommonMethods.readableFileSize((long)freespace) + "\n\n" +
                            "Available Space: " + CommonMethods.readableFileSize((long)freespace) + "\n\n" +
                            "If you wish, you can choose to continue, this is not recommended though!")
                    .setPositiveButton(android.R.string.ok, null)
                    .setNeutralButton("Continue Anyway", (dialog, which) -> launchService()).show();
            return;
        }
        launchService();
    }

    private void launchService() {
        // Test Intent
        if (isMyServiceRunning()) {
            Toast.makeText(this, "Service is already running", Toast.LENGTH_SHORT).show();
            notifyService(true);
            return;
        }
        Intent serviceIntent = new Intent(this, FanficCompressionService.class);
        startService(serviceIntent);
        notifyService(true);
        Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();
    }

    private void notifyService(boolean connected) {
        notifyService((connected) ? 1 : 0);
    }

    private void notifyService(int connectVal) {
        Intent intent = new Intent(FanficBroadcast.BROADCAST_NOTIFY);
        intent.putExtra(FanficBroadcast.BROADCAST_STATUS, connectVal);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.itachi1706.cheesecakeutilities.Modules.FanfictionCompactor.Services.FanficCompressionService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void hasStoragePermissionCheck() {
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (rc != PackageManager.PERMISSION_GRANTED)
            requestStoragePermission();
        else
            processPruningDetails();
    }

    private void processPruningDetails() {
        // Make sure the file and folder exists
        if (!FanfictionDatabase.databaseExists()) {
            new AlertDialog.Builder(this).setTitle("No Database Found").setCancelable(false)
                    .setMessage("Unable to find stories.db file in " + FanfictionDatabase.getDbFileFolder()
                            + ". Please export database to use this utility").setPositiveButton(android.R.string.ok, (dialog, which) -> finish())
                    .show();
            return;
        }
        LogHelper.i("FanficCompactor", "Database exists. Continuing...");
        new ScanStorageDetails(new FanficHandler(this)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private static final int RC_HANDLE_REQUEST_STORAGE = 3;

    private void requestStoragePermission() {
        LogHelper.w(PERM_MAN_TAG, "Storage permission is not granted. Requesting permission");
        final String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_REQUEST_STORAGE);
            return;
        }

        final Activity thisActivity = this;

        new AlertDialog.Builder(this).setTitle("Requesting Storage Permission")
                .setMessage("This app requires ability to access your storage to compact fanfictions")
                .setPositiveButton(android.R.string.ok, (dialog, which) -> ActivityCompat.requestPermissions(thisActivity, permissions, RC_HANDLE_REQUEST_STORAGE)).show();
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        final Activity thisActivity = this;
        switch (requestCode) {
            case RC_HANDLE_REQUEST_STORAGE:
                if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    LogHelper.i(PERM_MAN_TAG, "Storage Permission Granted. Allowing Utility Access");
                    processPruningDetails();
                    return;
                }
                CommonMethods.logPermError(grantResults);
                new AlertDialog.Builder(this).setTitle("Permission Denied")
                        .setMessage("You have denied the app ability to access your storage. This app will not be able to calculate" +
                                " file size or compact Fanfictions and will now exit")
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> finish())
                        .setCancelable(false)
                        .setNeutralButton("SETTINGS", (dialog, which) -> {
                            Intent permIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri packageURI = Uri.parse("package:" + thisActivity.getPackageName());
                            permIntent.setData(packageURI);
                            startActivity(permIntent);
                            finish();
                        }).show();
                break;
        }
    }

    @Override
    public String getHelpDescription() {
        return "Compacts fanfiction folders by removing unneeded folders/files.";
    }

    /**
     * HANDLER RECEIVER COMMUNICATING WITH TASK
     */
    static class FanficHandler extends Handler {
        WeakReference<FanfictionCompactorActivity> mActivity;

        FanficHandler(FanfictionCompactorActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            FanfictionCompactorActivity activity = mActivity.get();

            super.handleMessage(msg);

            switch (msg.what) {
                case ScanStorageDetails.SCAN_STORAGE_RESULT:
                    if (activity == null) break; // Activity died for some reason, dont do anything
                    activity.totalSize = msg.getData().getLong("filesize");
                    long totalSize = activity.totalSize;
                    int dbstories = msg.getData().getInt("dbcount");
                    int storyCount = msg.getData().getInt("filecount");

                    activity.folderSize.setText(String.format(activity.getString(R.string.ff_compat_result_fanfic_folder_size),
                            CommonMethods.readableFileSize(totalSize), totalSize));
                    activity.storyCount.setText(String.format(activity.getString(R.string.ff_compat_result_fanfic_story_count),
                            dbstories, storyCount));

                    // Retrival done, allow button press
                    activity.startServiceBtn.setEnabled(true);
                    break;
            }
        }
    }

    /**
     * BROADCAST RECEIVER COMMUNICATION WITH SERVICE
     */

    ProgressDialog progressDialog;
    ResponseReceiver receiver;

    private class ResponseReceiver extends BaseBroadcastReceiver {
        private ResponseReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            super.onReceive(context, intent);
            if (intent.getBooleanExtra(FanficBroadcast.BROADCAST_DATA_DONE, false)) {
                if (progressDialog != null)
                    progressDialog.dismiss();
                Toast.makeText(context, "Task Completed", Toast.LENGTH_SHORT).show();
                return;
            }

            if (progressDialog == null)
                progressDialog = new ProgressDialog(FanfictionCompactorActivity.this);
                progressDialog.setMax(intent.getIntExtra(FanficBroadcast.BROADCAST_DATA_MAX, 0));
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setProgress(intent.getIntExtra(FanficBroadcast.BROADCAST_DATA_PROGRESS, 0));
                progressDialog.setTitle(intent.getStringExtra(FanficBroadcast.BROADCAST_DATA_TITLE));
                progressDialog.setMessage(intent.getStringExtra(FanficBroadcast.BROADCAST_DATA_MSG));
                progressDialog.setIndeterminate(intent.getBooleanExtra(FanficBroadcast.BROADCAST_DATA_INDETERMINATE, false));
                // Dont restart the service
                progressDialog.setOnDismissListener(dialog -> notifyService(2));
                progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Hide", (dialog, which) -> notifyService(2));
                progressDialog.show();
        }
    }
}
