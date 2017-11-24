package com.itachi1706.cheesecakeutilities.Modules.ListApplications;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.itachi1706.cheesecakeutilities.BaseActivity;
import com.itachi1706.cheesecakeutilities.Modules.ListApplications.Objects.AppsItem;
import com.itachi1706.cheesecakeutilities.Modules.ListApplications.RecyclerAdapters.AppsAdapter;
import com.itachi1706.cheesecakeutilities.R;
import com.turingtechnologies.materialscrollbar.CustomIndicator;
import com.turingtechnologies.materialscrollbar.TouchScrollBar;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ListApplicationsActivity extends BaseActivity {

    RecyclerView recyclerView;
    ProgressBar bar;
    TextView label;
    TouchScrollBar scrollBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_applications);

        recyclerView = findViewById(R.id.list_app_recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        scrollBar = findViewById(R.id.scrollBar);
        scrollBar.setIndicator(new CustomIndicator(this), true);

        bar = findViewById(R.id.list_app_pb);
        label = findViewById(R.id.list_app_pb_label);

        eval(false);
    }

    private void eval(boolean system) {
        AppsAdapter adapter = new AppsAdapter(new AppsItem[0]);
        recyclerView.setAdapter(adapter);
        bar.setVisibility(View.VISIBLE);
        label.setVisibility(View.VISIBLE);
        new LoadAppThread().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, system);
    }

    private boolean isSystemApp(ApplicationInfo i) {
        return (i.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM;
    }

    @Override
    public String getHelpDescription() {
        return "List all applications installed on your device and their targeted API Levels\nComing Soon: " +
                "Showing a more understandable API level";
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.modules_applist, menu);
        return true;
    }

    private boolean checkSystem = false;
    private boolean sortByApi = false;
    private String appCountString = "";
    private List<String> appPackageNamesInstalled = new ArrayList<>();

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.systemapp: item.setChecked(!item.isChecked()); checkSystem = item.isChecked();
                eval(checkSystem); return true;
            case R.id.sortapi: item.setChecked(!item.isChecked()); sortByApi = item.isChecked();
                eval(checkSystem); return true;
            case R.id.graph:
                Intent i = new Intent(this, ListApplicationsApiGraphActivity.class);
                i.putExtra("appCount", appCountString);
                startActivity(i);
                return true;
            case R.id.scan_ghost:
                new AlertDialog.Builder(this).setTitle("Scan Ghost Directories")
                        .setMessage("This will scan your external application data folder (/sdcard/Android) for any ghost directories " +
                                "left behind by applications no longer installed on your device")
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton("Scan", (dialog, which) -> hasStoragePermissionCheck()).show();
                return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    private void hasStoragePermissionCheck() {
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (rc != PackageManager.PERMISSION_GRANTED)
            requestStoragePermission();
        else
            scanGhostDir();
    }

    private static final int RC_HANDLE_REQUEST_STORAGE = 4;

    private void requestStoragePermission() {
        Log.w("PermMan", "Storage permission is not granted. Requesting permission");
        final String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_REQUEST_STORAGE);
            return;
        }

        final Activity thisActivity = this;

        new AlertDialog.Builder(this).setTitle("Requesting Storage Permission")
                .setMessage("This app requires ability to access your storage to scan your internal storage and " +
                        "perform cleanup activities for you")
                .setPositiveButton(android.R.string.ok, (dialog, which) -> ActivityCompat.requestPermissions(thisActivity, permissions, RC_HANDLE_REQUEST_STORAGE)).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        final Activity thisActivity = this;
        switch (requestCode) {
            case RC_HANDLE_REQUEST_STORAGE:
                if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i("PermMan", "Storage Permission Granted. Allowing Utility Access");
                    scanGhostDir();
                    return;
                }
                Log.e("PermMan", "Permission not granted: results len = " + grantResults.length +
                        " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));
                new AlertDialog.Builder(this).setTitle("Permission Denied")
                        .setMessage("You have denied the app ability to access your storage. This app will not be able to scan" +
                                " for ghost directories or perform ghost directories cleanup for you")
                        .setPositiveButton(android.R.string.ok, null)
                        .setCancelable(false)
                        .setNeutralButton("SETTINGS", (dialog, which) -> {
                            Intent permIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri packageURI = Uri.parse("package:" + thisActivity.getPackageName());
                            permIntent.setData(packageURI);
                            startActivity(permIntent);
                        }).show();
                break;
        }
    }

    private void scanGhostDir() {
        String androidDirString = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data";
        File androidDir = new File(androidDirString);
        if (!androidDir.exists() || !androidDir.isDirectory() || appPackageNamesInstalled.isEmpty()) {
            Toast.makeText(getApplicationContext(), "An error occurred when attempting to do a scan", Toast.LENGTH_LONG).show();
            return;
        }

        File[] subDir = androidDir.listFiles();
        final ArrayMap<String, String> listOfGhostDir = new ArrayMap<>();
        if (subDir.length == 0) {
            new AlertDialog.Builder(this).setTitle("Ghost Directory Scanning Complete")
                    .setMessage("You do not have any folders in the Android Application Data directory of your device")
                    .setPositiveButton(android.R.string.ok, null).show();
            return;
        }
        for (File f : subDir) {
            if (!f.isDirectory()) continue;
            if (!appPackageNamesInstalled.contains(f.getName())) {
                listOfGhostDir.put(f.getName(), f.getAbsolutePath());
            }
        }

        if (listOfGhostDir.isEmpty()) {
            // Empty
            new AlertDialog.Builder(this).setTitle("Ghost Directory Scanning Complete")
                    .setMessage("You have no ghost directories found in your device")
                    .setPositiveButton(android.R.string.ok, null).show();
        } else {
            new MaterialDialog.Builder(this).title("Ghost Directory Scanning Complete")
                    .items(listOfGhostDir.keySet()).itemsCallbackMultiChoice(null, (dialog, which, text) -> {
                        int count = 0;
                        String removeMan = "";
                        for (CharSequence t : text) {
                            String path = listOfGhostDir.get(t);
                            File del = new File(path);
                            try {
                                FileUtils.deleteDirectory(del);
                                count++;
                            } catch (IOException e) {
                                Log.e("GhostCleanup", "Unable to remove " + t);
                                removeMan += t + "\n";
                            }
                        }
                        if (!removeMan.isEmpty()) {
                            new AlertDialog.Builder(ListApplicationsActivity.this).setTitle("Unable to remove some directories")
                                    .setMessage("Some directories cannot be removed. Please remove them manually.\n\n" + removeMan)
                                    .setPositiveButton(android.R.string.ok, null).show();
                        }
                        Toast.makeText(getApplicationContext(), "Cleaned up " + count + " directories", Toast.LENGTH_LONG).show();
                        return true;
                    }).positiveText("Clean Up").negativeText(android.R.string.cancel).show();
        }
    }

    private class LoadAppThread extends AsyncTask<Boolean, Void, Void> {

        private ArrayList<AppsItem> finalStr;

        @Override
        protected Void doInBackground(Boolean... params) {
            boolean system = params[0];
            PackageManager pm = getPackageManager();
            final List<ApplicationInfo> pkgAppsList = pm.getInstalledApplications(PackageManager.GET_META_DATA);
            finalStr = new ArrayList<>();
            for (ApplicationInfo i : pkgAppsList) {
                appPackageNamesInstalled.add(i.packageName);
                if (isSystemApp(i)) {
                    if (!system) continue;
                }
                String version = "Unknown";
                try {
                    PackageInfo pInfo = getPackageManager().getPackageInfo(i.packageName, PackageManager.GET_PERMISSIONS);
                    version = pInfo.versionName;
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

                AppsItem item = new AppsItem(getApplicationContext());
                item.setApiVersion(i.targetSdkVersion);
                item.setAppName(i.loadLabel(pm).toString());
                item.setPackageName(i.packageName);
                item.setIcon(i.loadIcon(pm));
                item.setVersion(version);
                finalStr.add(item);
            }

            finalAdapter = new AppsAdapter(finalStr);
            finalAdapter.sort();
            // Further sort if by API
            if (sortByApi) finalAdapter.sort(AppsAdapter.SORT_API);

            // Generate app count by API List
            appCountString = generateApiAppCountList();

            // Done
            runOnUiThread(() -> {
                recyclerView.setAdapter(finalAdapter);
                bar.setVisibility(View.GONE);
                label.setVisibility(View.GONE);
            });
            return null;
        }

        private String generateApiAppCountList() {
            ArrayMap<Integer, Integer> tmp = new ArrayMap<>();
            for (AppsItem appsItem : finalStr) {
                int count = 0;
                if (tmp.containsKey(appsItem.getApiVersion())) {
                    count = tmp.get(appsItem.getApiVersion());
                }
                count++;
                tmp.put(appsItem.getApiVersion(), count);
            }

            String appCount = "";
            for (Map.Entry<Integer, Integer> object : tmp.entrySet()) {
                appCount += object.getKey() + ":" + object.getValue() + "-";
            }
            return appCount.substring(0, appCount.length() - 1);
        }

        private AppsAdapter finalAdapter;
    }
}
