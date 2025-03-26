package com.itachi1706.cheesecakeutilities.modules.listapplications;

import static com.itachi1706.cheesecakeutilities.util.CommonMethods.logPermError;
import static com.itachi1706.cheesecakeutilities.util.CommonVariables.PERM_MAN_TAG;
import static com.itachi1706.helperlib.helpers.ValidationHelper.bytesToHex;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
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
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.itachi1706.cheesecakeutilities.R;
import com.itachi1706.cheesecakeutilities.modules.listapplications.objects.LabelledColumn;
import com.itachi1706.cheesecakeutilities.modules.listapplications.recyclerAdapters.AppsAdapter;
import com.itachi1706.helperlib.deprecation.TextViewDep;
import com.itachi1706.helperlib.helpers.LogHelper;
import com.itachi1706.helperlib.helpers.PrefHelper;
import com.itachi1706.helperlib.helpers.ValidationHelper;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
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

    private boolean isSystem = false, isUpdate = false;
    private ApplicationInfo info;
    private String signature;
    private String version;
    private Signature[] signatures;

    private static final int INSTALL_UNKNOWN = -99;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_applications_detail);

        String packageName = getIntent().getStringExtra("packageName");
        if (packageName == null) {
            LogHelper.e("ListAppDetail", "Invalid Package Name. Exiting...");
            finish();
            return;
        }

        // Firebase Analytics Event Logging
        FirebaseAnalytics analytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, packageName);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "utility_listapp_viewdetail");
        analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        LogHelper.i("Firebase", "Logged Viewing of Detailed App Info Launched: " + packageName);

        PackageManager pm = getPackageManager();
        try {
            info = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            isSystem = ((info.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
            isUpdate = ((info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            LogHelper.e("ListAppDetail", "Failed to get info for " + packageName + ". Exiting");
            Toast.makeText(this, "Failed to get app info!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        version = "Unknown";
        int versionCode = 0, installLocation = INSTALL_UNKNOWN;
        long firstInstall = 0, lastUpdate = 0;
        String[] requestedPermissions = null;
        ActivityInfo[] activities = null;
        signatures = null;
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
            LogHelper.e("ListAppDetail", "Failed to get package info for " + packageName + ". Some info might not be available");
        }

        signature = generateSignatureList(signatures);

        appName = findViewById(R.id.appName);
        appVersion = findViewById(R.id.appVersion);
        icon = findViewById(R.id.iv_icon);
        grid = findViewById(R.id.gridLayout);
        creator = findViewById(R.id.layout_creator);
        backup = findViewById(R.id.btnBackup);
        launchApp = findViewById(R.id.btnLaunch);

        appName.setText(info.loadLabel(pm).toString());
        appVersion.setText(getString(R.string.list_app_version, " ", version));
        icon.setImageDrawable(info.loadIcon(pm));

        creator.addView(generateDualColumn("Basic Information", getBasicInformation(lastUpdate, versionCode, firstInstall, installLocation)));
        creator.addView(generateDualColumn("File Information", getFileLocation()));

        assert requestedPermissions != null;
        assert configurations != null;
        generateLists(requestedPermissions, activities, configurations, providerInfos, receivers, serviceInfos);

        // Add features to buttons
        // Start backup
        backup.setOnClickListener(v -> hasStoragePermissionCheck(appName.getText().toString(), info.sourceDir, info.packageName, version));

        launchApp.setOnClickListener(v -> {
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(info.packageName);
            if (launchIntent != null) startActivity(launchIntent);
            else {
                Toast.makeText(v.getContext(), "Unable to launch activity", Toast.LENGTH_SHORT).show();
                launchApp.setEnabled(false);
            }
        });

        if (error) {
            // Firebase Analytics Event Logging
            Bundle errorbundle = new Bundle();
            errorbundle.putString(FirebaseAnalytics.Param.ITEM_ID, packageName);
            errorbundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "utility_listapp_viewdetail");
            analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, errorbundle);
            LogHelper.i("Firebase", "Logged Error Processing Detailed App Info: " + packageName);
            creator.addView(generateSingleColumn("Error", "An error occurred while retrieving app information, Providing truncated results"));
        }
    }

    private void generateLists(String[] requestedPermissions, ActivityInfo[] activities, FeatureInfo[] configurations,
                               ProviderInfo[] providerInfos, ActivityInfo[] receivers, ServiceInfo[] serviceInfos) {
        String permissionList = generatePermissionsList(requestedPermissions);
        String activityList = generateActivitiesList(activities);
        String configList = generateRequiredFeaturesList(configurations);
        String providerList = generateProvidersList(providerInfos);
        String serviceList = generateServicesList(serviceInfos);
        String receiverList = generateReceiversList(receivers);

        if (!permissionList.isEmpty())
            creator.addView(generateSingleColumn("Permissions (" + requestedPermissions.length + ")", permissionList));
        if (!configList.isEmpty()) creator.addView(generateSingleColumn("Required Features (" + configurations.length + ")", configList));
        if (!activityList.isEmpty()) creator.addView(generateSingleColumn("Activities (" + activities.length + ")", activityList));
        if (!serviceList.isEmpty()) creator.addView(generateSingleColumn("Services (" + serviceInfos.length + ")", serviceList));
        if (!providerList.isEmpty()) creator.addView(generateSingleColumn("Providers (" + providerInfos.length + ")", providerList));
        if (!receiverList.isEmpty()) creator.addView(generateSingleColumn("Receivers (" + receivers.length + ")", receiverList));
    }

    private List<LabelledColumn> getBasicInformation(long lastUpdate, int versionCode, long firstInstall, int installLocation) {
        List<LabelledColumn> basicList = new ArrayList<>();

        basicList.add(new LabelledColumn("Package Name", info.packageName));
        basicList.add(new LabelledColumn("Version Code", versionCode));
        basicList.add(new LabelledColumn("Target SDK", info.targetSdkVersion));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) basicList.add(new LabelledColumn("Min SDK", info.minSdkVersion));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String insLoc;
            switch (installLocation) {
                case PackageInfo.INSTALL_LOCATION_INTERNAL_ONLY: insLoc = "Internal Memory Only"; break;
                case PackageInfo.INSTALL_LOCATION_PREFER_EXTERNAL: insLoc = "SD Card Preferred"; break;
                case PackageInfo.INSTALL_LOCATION_AUTO: insLoc = "Auto"; break;
                case INSTALL_UNKNOWN:
                default: insLoc = "Default (Auto)"; break;
            }
            basicList.add(new LabelledColumn("Install Location", insLoc));
        }
        basicList.add(new LabelledColumn("Signature",  signature));
        basicList.add(new LabelledColumn("Process", info.processName));
        basicList.add(new LabelledColumn("Min Width (DP)", info.largestWidthLimitDp));
        String installFrom;
        String installerPackageName = ValidationHelper.getInstallLocation(this, info.packageName);
        switch (ValidationHelper.checkInstallLocation(this, info.packageName)) {
            case ValidationHelper.GOOGLE_PLAY: installFrom = "Google Play (" + installerPackageName + ")"; break;
            case ValidationHelper.AMAZON: installFrom = "Amazon App Store (" + installerPackageName + ")"; break;
            case ValidationHelper.SIDELOAD:
            default: installFrom = "Package Installer";
                if (installerPackageName != null) installFrom += " (" + installerPackageName + ")"; break;
        }
        basicList.add(new LabelledColumn("Installed From", installFrom));
        basicList.add(new LabelledColumn("Installed On", generateDateFromLong(firstInstall)));
        basicList.add(new LabelledColumn("Updated On", generateDateFromLong(lastUpdate)));
        File file = new File(info.sourceDir);
        if (file.exists()) {
            basicList.add(new LabelledColumn("APK Size", humanReadableByteCount(file.length(), true)));
        }
        basicList.add(new LabelledColumn("UID", info.uid));
        return basicList;
    }

    private List<LabelledColumn> getFileLocation() {
        List<LabelledColumn> fileList = new ArrayList<>();
        fileList.add(new LabelledColumn("App Location", info.sourceDir));
        fileList.add(new LabelledColumn("Data Dir", info.dataDir));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            fileList.add(new LabelledColumn("Device Protected Dir", info.deviceProtectedDataDir));
        }
        fileList.add(new LabelledColumn("Library Dir", info.nativeLibraryDir));
        return fileList;
    }

    private String generateActivitiesList(ActivityInfo[] activities) {
        if (activities != null) {
            StringBuilder activityList = new StringBuilder();
            for (ActivityInfo s : activities) {
                activityList.append(s.name).append("\n");
            }
            return activityList.toString();
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
            StringBuilder providerList = new StringBuilder();
            for (ProviderInfo s : providers) {
                providerList.append(s.name).append("\n");
            }
            return providerList.toString();
        }
        return "";
    }

    private String generateReceiversList(ActivityInfo[] receivers) {
        if (receivers != null) {
            StringBuilder receiverList = new StringBuilder();
            for (ActivityInfo s : receivers) {
                receiverList.append(s.name).append("\n");
            }
            return receiverList.toString();
        }
        return "";
    }

    private String generateServicesList(ServiceInfo[] services) {
        if (services != null) {
            StringBuilder servicesList = new StringBuilder();
            for (ServiceInfo s : services) {
                servicesList.append(s.name).append("\n");
            }
            return servicesList.toString();
        }
        return "";
    }

    private String generatePermissionsList(String[] requestedPermissions) {
        if (requestedPermissions != null) {
            StringBuilder permissionList = new StringBuilder();
            for (String s : requestedPermissions) {
                permissionList.append(s).append("\n");
            }
            return permissionList.toString();
        }
        return "";
    }

    private String generateSignatureList(Signature[] signatures) {
        if (signatures != null) {
            StringBuilder signatureList = new StringBuilder();
            try {
                if (signatures.length == 1)
                    signatureList = new StringBuilder(ValidationHelper.getSignatureString(signatures[0]).trim());

                else {
                    for (Signature s : signatures) {
                        signatureList.append(ValidationHelper.getSignatureString(s).trim()).append("\n");
                    }
                }
                return signatureList.toString();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    private String generateRequiredFeaturesList(FeatureInfo[] configurations) {
        if (configurations!= null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            StringBuilder configList = new StringBuilder();
            for (FeatureInfo i : configurations) {
                if (i.name == null || i.name.isEmpty()) {
                    // OpenGLES
                    configList.append("OpenGL ES Version: ").append(i.getGlEsVersion()).append("\n");
                } else {
                    configList.append(i.name).append("\n");
                }
            }
            return configList.toString();
        }
        return "";
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
        TextViewDep.setTextAppearance(titleView, this, android.R.style.TextAppearance_Medium);
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
        TextViewDep.setTextAppearance(titleView, this, android.R.style.TextAppearance_Medium);
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
            labelView.setTextColor((PrefHelper.isNightModeEnabled(this)) ? Color.WHITE : Color.BLACK);
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
        hasStoragePermissionCheck(appName, appPath, packageName, appVersion, false);
    }

    private void hasStoragePermissionCheck(String appName, String appPath, String packageName, String appVersion, boolean shareApk) {
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (rc != PackageManager.PERMISSION_GRANTED)
            requestStoragePermission();
        else
            processBackup(appName, appPath, packageName, appVersion, shareApk);
    }

    private void processBackup(final String appName, final String appPath, String packageName, String appVersion, boolean shareApk) {
        final String filepath = appName + "_" + packageName + "-" + appVersion + ".apk";
        LogHelper.i("Backup", "Starting Backup Process for " + packageName);
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setIndeterminate(true);
        dialog.setTitle("Backing up");
        dialog.setCancelable(false);
        dialog.setMessage("Backing up " + appName + "...");
        dialog.show();
        new BackupAppThread(dialog, shareApk, this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, appName, appPath, filepath);
        LogHelper.i("Backup", "Stopping Backup Process for " + packageName);
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
                .setMessage("This app requires ability to access your storage to backup/restore apps")
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
                    new AlertDialog.Builder(this).setTitle("Permission Granted")
                            .setMessage("Please request the backup of the app again")
                            .setPositiveButton(android.R.string.ok, null).show();
                    return;
                }
                logPermError(grantResults);
                new AlertDialog.Builder(this).setTitle("Permission Denied")
                        .setMessage("You have denied the app ability to access your storage. Backup will not continue")
                        .setPositiveButton(android.R.string.ok, null)
                        .setCancelable(false)
                        .setNeutralButton("SETTINGS", (dialog, which) -> {
                            Intent permIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri packageURI = Uri.parse("package:" + thisActivity.getPackageName());
                            permIntent.setData(packageURI);
                            startActivity(permIntent);
                            finish();
                        }).show();
                break;
            default: super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    // Get primary signature only
    private void viewCertificate(Signature signature) {
        try {
            X509Certificate c = ValidationHelper.getCert(signature.toByteArray());
            byte[] sigCert = c.getEncoded();

            new AlertDialog.Builder(this).setTitle("Certificate Information for " + appName.getText())
                    .setMessage(c.getSubjectX500Principal().toString().replace(", ", ",\n")
                            + "\n\nCertificate Fingerprints:\nMD5: "
                            + bytesToHex(MessageDigest.getInstance("MD5").digest(sigCert)) + "\nSHA1: " + this.signature
                            + "\nSHA256: " + bytesToHex(MessageDigest.getInstance("SHA256").digest(sigCert)))
                    .setPositiveButton(com.itachi1706.appupdater.R.string.dialog_action_positive_close, null).show();
        } catch (CertificateException e) {
            Toast.makeText(this, "Unable to create certificate information screen", Toast.LENGTH_SHORT).show();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.modules_applist_detail, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (isUpdate) menu.findItem(R.id.uninstall).setTitle("Uninstall Updates");
        else if (isSystem) menu.findItem(R.id.uninstall).setEnabled(false);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Uri uri = Uri.fromParts("package", info.packageName, null);
        int id = item.getItemId();
        if (id == R.id.appsettings) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(uri);
            LogHelper.v(AppsAdapter.TAG, "Attempting to launch for " + appName.getText());
            startActivity(intent);
            return true;
        } else if (id == R.id.playstore) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + info.packageName)));
            return true;
        } else if (id == R.id.copysig) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Signature", signature);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_LONG).show();
            return true;
        } else if (id == R.id.viewcert) {
            if (signatures.length < 1) Toast.makeText(this, "No signature found!", Toast.LENGTH_SHORT).show();
            else viewCertificate(signatures[0]);
            return true;
        } else if (id == R.id.share_apk) {
            hasStoragePermissionCheck(appName.getText().toString(), info.sourceDir, info.packageName, version, true);
            return true;
        } else if (id == R.id.uninstall) {
            Intent uninstallIntent = new Intent();
            uninstallIntent.setAction(Intent.ACTION_UNINSTALL_PACKAGE);
            uninstallIntent.setData(uri);
            LogHelper.v(AppsAdapter.TAG, "Attempting to uninstall " + appName.getText());
            startActivity(uninstallIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
