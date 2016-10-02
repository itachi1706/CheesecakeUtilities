package com.itachi1706.cheesecakeutilities.Modules.ListApplications;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.itachi1706.cheesecakeutilities.Modules.ListApplications.Helpers.BackupHelper;
import com.itachi1706.cheesecakeutilities.Modules.ListApplications.Objects.LabelledColumn;
import com.itachi1706.cheesecakeutilities.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ListApplicationsDetailActivity extends AppCompatActivity {

    TextView appName, appVersion;
    ImageView icon;
    GridLayout grid;
    LinearLayout creator;
    Button backup, launchApp;

    private ApplicationInfo info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_applications_detail);

        String packageName = getIntent().getStringExtra("packageName");
        if (packageName == null) {
            Log.e("ListAppDetail", "Invalid Package Name. Exiting...");
            finish();
            return;
        }

        PackageManager pm = getPackageManager();
        try {
            info = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Log.e("ListAppDetail", "Failed to get info for " + packageName + ". Exiting");
            Toast.makeText(this, "Failed to get app info!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String version = "Unknown";
        int versionCode = 0;
        String[] requestedPermissions = null;
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(info.packageName, PackageManager.GET_PERMISSIONS);
            requestedPermissions = pInfo.requestedPermissions;
            version = pInfo.versionName;
            versionCode = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        String permissionList = "";
        if (requestedPermissions != null) {
            for (String s : requestedPermissions) {
                permissionList += s + "\n";
            }
        }

        appName = (TextView) findViewById(R.id.appName);
        appVersion = (TextView) findViewById(R.id.appVersion);
        icon = (ImageView) findViewById(R.id.iv_icon);
        grid = (GridLayout) findViewById(R.id.gridLayout);
        creator = (LinearLayout) findViewById(R.id.layout_creator);
        backup = (Button) findViewById(R.id.btnBackup);
        launchApp = (Button) findViewById(R.id.btnLaunch);


        appName.setText(info.loadLabel(pm).toString());
        appVersion.setText("Version " + version);
        icon.setImageDrawable(info.loadIcon(pm));

        List<LabelledColumn> testList = new ArrayList<>();

        testList.add(new LabelledColumn("Package Name", info.packageName));
        testList.add(new LabelledColumn("Version Code", versionCode));
        testList.add(new LabelledColumn("Target SDK", info.targetSdkVersion));
        //testList.add(new LabelledColumn("Signature", "Coming Soon")); // TODO: Implement it
        testList.add(new LabelledColumn("Data Dir", info.dataDir));
        testList.add(new LabelledColumn("App Location", info.sourceDir));
        //testList.add(new LabelledColumn("Installed On", "Coming Soon")); // TODO: Implement it
        //testList.add(new LabelledColumn("Updated On", "Coming Soon")); // TODO: Implement it
        testList.add(new LabelledColumn("UID", info.uid));
        creator.addView(generateDualColumn("Basic Information", testList));
        creator.addView(generateSingleColumn("Permissions", permissionList));
        final String versionString = version;

        // Add features to buttons
        backup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start backup
                hasStoragePermissionCheck(appName.getText().toString(), info.sourceDir, info.packageName, versionString);
            }
        });

        launchApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage(info.packageName);
                startActivity(launchIntent);
            }
        });
    }

    private LinearLayout generateSingleColumn(String title, String... message) {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextAppearance(this, android.R.style.TextAppearance_Material_Medium);
        titleView.setPadding(0,20,0,20);
        l.addView(titleView);
        TextView detailView;
        for (String s : message) {
            detailView = new TextView(this);
            detailView.setText(s);
            detailView.setPadding(0, 0, 0, 3);
            l.addView(detailView);
        }
        return l;
    }

    private LinearLayout generateDualColumn(String title, List<LabelledColumn> fields) {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextAppearance(this, android.R.style.TextAppearance_Material_Medium);
        titleView.setPadding(0,20,0,20);
        l.addView(titleView);
        TextView labelView;
        LinearLayout detailLayout;
        TextView detailView;
        for (LabelledColumn lc : fields) {
            detailLayout = new LinearLayout(this);
            detailLayout.setOrientation(LinearLayout.HORIZONTAL);
            labelView = new TextView(this);
            labelView.setTypeface(Typeface.DEFAULT_BOLD);
            labelView.setMinWidth(350);
            labelView.setPadding(0,0,10,0);
            labelView.setTextColor(Color.BLACK);
            detailView = new TextView(this);

            labelView.setText(lc.getLabel());
            detailView.setText(lc.getField());
            detailLayout.setPadding(0, 0, 0, 3);
            detailLayout.addView(labelView);
            detailLayout.addView(detailView);
            l.addView(detailLayout);
        }
        return l;
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
        new BackupAppThread(dialog).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, appName, appPath, filepath);
        Log.i("Backup", "Stopping Backup Process for " + packageName);
    }

    private class BackupAppThread extends AsyncTask<String, Void, Void> {

        private ProgressDialog dialog;

        BackupAppThread(ProgressDialog dialog) {
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
                            Toast.makeText(getApplicationContext(), "Unable to create folder! Backup failed", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Backup of " + appName + " completed", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                        }
                    });
                }
            } catch (final IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Error backuping app (" + e.getLocalizedMessage() + ")", Toast.LENGTH_LONG).show();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.modules_applist_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.appsettings: Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", info.packageName, null);
                intent.setData(uri);
                Log.v("AppsAdapter", "Attempting to launch for " + appName.getText());
                startActivity(intent); return true;
            case R.id.playstore:
            case R.id.copysig: Toast.makeText(this, "Feature coming soon", Toast.LENGTH_LONG).show(); return true;
            default: return super.onOptionsItemSelected(item);
        }
    }
}
