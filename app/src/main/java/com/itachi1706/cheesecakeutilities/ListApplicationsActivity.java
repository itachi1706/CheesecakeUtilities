package com.itachi1706.cheesecakeutilities;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.itachi1706.cheesecakeutilities.Modules.ListApplications.BackupHelper;
import com.itachi1706.cheesecakeutilities.Modules.ListApplications.Broadcasts.ListAppBroadcast;
import com.itachi1706.cheesecakeutilities.Modules.ListApplications.Objects.AppsItem;
import com.itachi1706.cheesecakeutilities.Modules.ListApplications.RecyclerAdapters.AppsAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ListApplicationsActivity extends BaseActivity {

    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_applications);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_test);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        receiver = new ResponseReceiver();
        IntentFilter filter = new IntentFilter(ListAppBroadcast.LISTAPP_BROADCAST_BACKUP);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
        Log.i("Backup", "Initialized Broadcast Receiver");

        eval(false);
    }

    private void eval(boolean system) {
        PackageManager pm = getPackageManager();
        final List<ApplicationInfo> pkgAppsList = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        ArrayList<AppsItem> finalStr = new ArrayList<>();
        for (ApplicationInfo i : pkgAppsList) {
            if (isSystemApp(i)) {
                if (!system) continue;
            }
            String version = "Unknown";
            try {
                PackageInfo pInfo = getPackageManager().getPackageInfo(i.packageName, 0);
                version = pInfo.versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            AppsItem item = new AppsItem(this);
            item.setApiVersion(i.targetSdkVersion + "");
            item.setAppName(i.loadLabel(pm).toString());
            item.setAppPath(i.sourceDir);
            item.setPackageName(i.packageName);
            item.setIcon(i.loadIcon(pm));
            item.setVersion(version);
            finalStr.add(item);
        }
        AppsAdapter adapter = new AppsAdapter(finalStr);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        Log.i("Backup", "Destroyed Broadcast Receiver");
    }

    private boolean isSystemApp(ApplicationInfo i) {
        return (i.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM;
    }

    @Override
    String getHelpDescription() {
        return "List all applications installed on your device and their targeted API Levels\nComing Soon: " +
                "Showing a more understandable API level";
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.modules_applist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.systemapp: item.setChecked(!item.isChecked()); eval(item.isChecked()); return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    private void hasStoragePermissionCheck(String appName, String appPath, String packageName, String appVersion) {
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (rc != PackageManager.PERMISSION_GRANTED)
            requestStoragePermission();
        else
            processBackup(appName, appPath, packageName, appVersion);
    }

    private void processBackup(final String appName, final String appPath, String packageName, String appVersion) {
        final String filepath = appName + "_" + packageName + "-" + appVersion + ".apk";
        Log.i("Backup", "Starting Backup Process for " + packageName);
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setIndeterminate(true);
        dialog.setTitle("Backing up");
        dialog.setCancelable(false);
        dialog.setMessage("Backing up " + appName + "...");
        dialog.show();
        new BackupAppThread(this, dialog).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, appName, appPath, filepath);
        Log.i("Backup", "Stopping Backup Process for " + packageName);
    }

    private class BackupAppThread extends AsyncTask<String, Void, Void> {

        private Context context;
        private ProgressDialog dialog;

        public BackupAppThread(Context context, ProgressDialog dialog) {
            this.context = context;
            this.dialog = dialog;
        }

        @Override
        protected Void doInBackground(String... params) {
            final String appName = params[0];
            final String appPath = params[1];
            final String filepath = params[2];
            // Init
            try {
                if (!BackupHelper.backupApk(appPath, filepath)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "Unable to create folder! Backup failed", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "Backup of " + appName + " completed", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                        }
                    });
                }
            } catch (final IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "Error backuping app (" + e.getLocalizedMessage() + ")", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    }
                });
                e.printStackTrace();
            }
            return null;
        }
    }

    private static final int RC_HANDLE_REQUEST_STORAGE = 3;

    private void requestStoragePermission() {
        Log.w("PermMan", "Storage permission is not granted. Requesting permission");
        final String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_REQUEST_STORAGE);
            return;
        }

        final Activity thisActivity = this;

        new AlertDialog.Builder(this).setTitle("Requesting Storage Permission")
                .setMessage("This app requires ability to access your storage to backup/restore apps")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(thisActivity, permissions, RC_HANDLE_REQUEST_STORAGE);
                    }
                }).show();
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
                    Log.i("PermMan", "Storage Permission Granted. Allowing Utility Access");
                    new AlertDialog.Builder(this).setTitle("Permission Granted")
                            .setMessage("Please request the backup of the app again")
                            .setPositiveButton(android.R.string.ok, null).show();
                    return;
                }
                Log.e("PermMan", "Permission not granted: results len = " + grantResults.length +
                        " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));
                new AlertDialog.Builder(this).setTitle("Permission Denied")
                        .setMessage("You have denied the app ability to access your storage. Backup will not continue")
                        .setPositiveButton(android.R.string.ok, null)
                        .setCancelable(false)
                        .setNeutralButton("SETTINGS", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent permIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri packageURI = Uri.parse("package:" + thisActivity.getPackageName());
                                permIntent.setData(packageURI);
                                startActivity(permIntent);
                                finish();
                            }
                        }).show();
                break;
        }
    }

    /**
     * BROADCAST RECEIVER COMMUNICATION WITH SERVICE
     */
    ResponseReceiver receiver;

    private class ResponseReceiver extends BroadcastReceiver {
        private ResponseReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            // TODO Code Stub
            String appname = intent.getStringExtra(ListAppBroadcast.LISTAPP_BROADCAST_APPNAME);
            String apppath = intent.getStringExtra(ListAppBroadcast.LISTAPP_BROADCAST_APPPATH);
            String packageName = intent.getStringExtra(ListAppBroadcast.LISTAPP_BROADCAST_APPPACKAGE);
            String appversion = intent.getStringExtra(ListAppBroadcast.LISTAPP_BROADCAST_APPVERSION);
            if (appname == null || apppath == null || packageName == null) {
                Toast.makeText(context, "Unable to backup app", Toast.LENGTH_LONG).show();
                return;
            }

            if (appversion == null) {
                appversion = "0.0";
            }

            // Start backup
            hasStoragePermissionCheck(appname, apppath, packageName, appversion);
        }
    }
}
