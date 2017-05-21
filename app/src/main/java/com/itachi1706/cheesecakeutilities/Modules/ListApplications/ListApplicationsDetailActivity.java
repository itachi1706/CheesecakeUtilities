package com.itachi1706.cheesecakeutilities.Modules.ListApplications;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.Signature;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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

import com.google.firebase.analytics.FirebaseAnalytics;
import com.itachi1706.cheesecakeutilities.Modules.ListApplications.Helpers.BackupHelper;
import com.itachi1706.cheesecakeutilities.Modules.ListApplications.Objects.LabelledColumn;
import com.itachi1706.cheesecakeutilities.R;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ListApplicationsDetailActivity extends AppCompatActivity {

    TextView appName, appVersion;
    ImageView icon;
    GridLayout grid;
    LinearLayout creator;
    Button backup, launchApp;

    private ApplicationInfo info;
    private String signature;

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

        // Firebase Analytics Event Logging
        FirebaseAnalytics analytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, packageName);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "utility_listapp_viewdetail");
        analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        Log.i("Firebase", "Logged Viewing of Detailed App Info Launched: " + packageName);

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
        final int INSTALL_UNKNOWN = -99;
        int versionCode = 0, installLocation = INSTALL_UNKNOWN;
        long firstInstall = 0, lastUpdate = 0;
        String[] requestedPermissions = null;
        ActivityInfo[] activities = null;
        Signature[] signatures = null;
        FeatureInfo[] configurations = null;

        ProviderInfo[] providerInfos = null;
        ActivityInfo[] receivers = null;
        ServiceInfo[] serviceInfos = null;
        boolean error = false;
        try {
            int flags = PackageManager.GET_ACTIVITIES | PackageManager.GET_SIGNATURES | PackageManager.GET_PERMISSIONS |
                    PackageManager.GET_PROVIDERS | PackageManager.GET_RECEIVERS | PackageManager.GET_CONFIGURATIONS | PackageManager.GET_SERVICES;
            PackageInfo pInfo;
            try {
                pInfo = pm.getPackageInfo(info.packageName, flags);
            } catch (RuntimeException e) {
                if (e.getMessage().contains("Package manager has died")) {
                    pInfo = pm.getPackageInfo(info.packageName, PackageManager.GET_PERMISSIONS); // Do a basic version
                    error = true;
                } else throw e;
            }
            requestedPermissions = pInfo.requestedPermissions;
            version = pInfo.versionName;
            versionCode = pInfo.versionCode;
            signatures = pInfo.signatures;
            activities = pInfo.activities;
            firstInstall = pInfo.firstInstallTime;
            lastUpdate = pInfo.lastUpdateTime;
            providerInfos = pInfo.providers;
            receivers = pInfo.receivers;
            serviceInfos = pInfo.services;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                installLocation = pInfo.installLocation;
                configurations = pInfo.reqFeatures;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Log.e("ListAppDetail", "Failed to get package info for " + packageName + ". Some info might not be available");
        }

        String permissionList = generatePermissionsList(requestedPermissions);
        signature = generateSignatureList(signatures);
        String activityList = generateActivitiesList(activities);
        String configList = generateRequiredFeaturesList(configurations);
        String providerList = generateProvidersList(providerInfos);
        String serviceList = generateServicesList(serviceInfos);
        String receiverList = generateReceiversList(receivers);

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            testList.add(new LabelledColumn("Min SDK", info.minSdkVersion));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String insLoc;
            switch (installLocation) {
                case PackageInfo.INSTALL_LOCATION_INTERNAL_ONLY: insLoc = "Internal Memory Only"; break;
                case PackageInfo.INSTALL_LOCATION_PREFER_EXTERNAL: insLoc = "SD Card Preferred"; break;
                case PackageInfo.INSTALL_LOCATION_AUTO: insLoc = "Auto"; break;
                case INSTALL_UNKNOWN:
                    default: insLoc = "Default (Auto)"; break;
            }
            testList.add(new LabelledColumn("Install Location", insLoc));
        }
        testList.add(new LabelledColumn("Signature",  signature));
        testList.add(new LabelledColumn("Process", info.processName));
        testList.add(new LabelledColumn("Min Width (DP)", info.largestWidthLimitDp));
        testList.add(new LabelledColumn("Installed On", generateDateFromLong(firstInstall)));
        testList.add(new LabelledColumn("Updated On", generateDateFromLong(lastUpdate)));
        File file = new File(info.sourceDir);
        if (file.exists()) {
            testList.add(new LabelledColumn("APK Size", humanReadableByteCount(file.length(), true)));
        }
        testList.add(new LabelledColumn("UID", info.uid));
        creator.addView(generateDualColumn("Basic Information", testList));

        testList.clear();
        testList.add(new LabelledColumn("App Location", info.sourceDir));
        testList.add(new LabelledColumn("Data Dir", info.dataDir));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            testList.add(new LabelledColumn("Device Protected Dir", info.deviceProtectedDataDir));
        }
        testList.add(new LabelledColumn("Library Dir", info.nativeLibraryDir));
        creator.addView(generateDualColumn("File Information", testList));

        assert requestedPermissions != null;
        assert configurations != null;
        if (!permissionList.isEmpty())
            creator.addView(generateSingleColumn("Permissions (" + requestedPermissions.length + ")", permissionList));
        if (!configList.isEmpty()) creator.addView(generateSingleColumn("Required Features (" + configurations.length + ")", configList));
        if (!activityList.isEmpty()) creator.addView(generateSingleColumn("Activities (" + activities.length + ")", activityList));
        if (!serviceList.isEmpty()) creator.addView(generateSingleColumn("Services (" + serviceInfos.length + ")", serviceList));
        if (!providerList.isEmpty()) creator.addView(generateSingleColumn("Providers (" + providerInfos.length + ")", providerList));
        if (!receiverList.isEmpty()) creator.addView(generateSingleColumn("Receivers (" + receivers.length + ")", receiverList));
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

        if (error) {
            // Firebase Analytics Event Logging
            Bundle errorbundle = new Bundle();
            errorbundle.putString(FirebaseAnalytics.Param.ITEM_ID, packageName);
            errorbundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "utility_listapp_viewdetail");
            analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, errorbundle);
            Log.i("Firebase", "Logged Error Processing Detailed App Info: " + packageName);
            creator.addView(generateSingleColumn("Error", "An error occurred while retrieving app information, Providing truncated results"));
        }
    }

    private String generateActivitiesList(ActivityInfo[] activities) {
        if (activities != null) {
            String activityList = "";
            for (ActivityInfo s : activities) {
                activityList += s.name + "\n";
            }
            return activityList;
        }
        return "";
    }

    private String generateDateFromLong(long date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault());
        Date resultdate = new Date(date);
        return sdf.format(resultdate);
    }

    private String generateProvidersList(ProviderInfo[] providers) {
        if (providers != null) {
            String providerList = "";
            for (ProviderInfo s : providers) {
                providerList += s.name + "\n";
            }
            return providerList;
        }
        return "";
    }

    private String generateReceiversList(ActivityInfo[] receivers) {
        if (receivers != null) {
            String receiverList = "";
            for (ActivityInfo s : receivers) {
                receiverList += s.name + "\n";
            }
            return receiverList;
        }
        return "";
    }

    private String generateServicesList(ServiceInfo[] services) {
        if (services != null) {
            String servicesList = "";
            for (ServiceInfo s : services) {
                servicesList += s.name + "\n";
            }
            return servicesList;
        }
        return "";
    }

    private String generatePermissionsList(String[] requestedPermissions) {
        if (requestedPermissions != null) {
            String permissionList = "";
            for (String s : requestedPermissions) {
                permissionList += s + "\n";
            }
            return permissionList;
        }
        return "";
    }

    private String generateSignatureList(Signature[] signatures) {
        if (signatures != null) {
            String signatureList = "";
            try {
                if (signatures.length == 1)
                    signatureList = getSignatureString(signatures[0]).trim();

                else {
                    for (Signature s : signatures) {
                        signatureList += getSignatureString(s).trim()+ "\n";
                    }
                }
                return signatureList;
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    private String generateRequiredFeaturesList(FeatureInfo[] configurations) {
        if (configurations!= null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String configList = "";
            for (FeatureInfo i : configurations) {
                if (i.name == null || i.name.isEmpty()) {
                    // OpenGLES
                    configList += "OpenGL ES Version: " + i.getGlEsVersion() + "\n";
                } else {
                    configList += i.name + "\n";
                }
            }
            return configList;
        }
        return "";
    }

    private String getSignatureString(Signature sig) throws NoSuchAlgorithmException {
        byte[] cert = sig.toByteArray();
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(cert);
            CertificateFactory cf = CertificateFactory.getInstance("X509");
            X509Certificate c = (X509Certificate) cf.generateCertificate(inputStream);
            byte[] sigCert = c.getEncoded();
            MessageDigest md = MessageDigest.getInstance("SHA1");
            byte[] publicKey = md.digest(sigCert);
            return bytesToHex(publicKey);
        } catch (CertificateException e) {
            Log.e("Signature", "Cannot Create Signature, Falling back");
            Log.e("Signature", "Error: " + e.getLocalizedMessage());
            MessageDigest md = MessageDigest.getInstance("SHA1");
            md.update(sig.toByteArray());
            return bytesToHex(md.digest());
        }
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < hexChars.length; i++) {
            if (i % 2 == 0 && i != 0) sb.append(":");
            sb.append(hexChars[i]);
        }
        return sb.toString();
    }

    private String humanReadableByteCount(long bytes, boolean si) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format(Locale.US, "%.2f %sB", bytes / Math.pow(1024, exp), pre);
    }

    private LinearLayout generateSingleColumn(String title, String... message) {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextAppearance(this, android.R.style.TextAppearance_Medium);
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
        titleView.setTextAppearance(this, android.R.style.TextAppearance_Medium);
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
            labelView.setMaxWidth(350);
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
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=" + info.packageName)));
                return true;
            case R.id.copysig:
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Signature", signature);
                clipboard.setPrimaryClip(clip); Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_LONG).show(); return true;
            default: return super.onOptionsItemSelected(item);
        }
    }
}
