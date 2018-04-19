package com.itachi1706.cheesecakeutilities.Modules.ListApplications.Fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.itachi1706.cheesecakeutilities.Modules.ListApplications.ListApplicationsApiGraphActivity;
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

import static com.itachi1706.cheesecakeutilities.Util.CommonMethods.displayPermErrorMessage;
import static com.itachi1706.cheesecakeutilities.Util.CommonVariables.PERM_MAN_TAG;

/**
 * Created by Kenneth on 18/4/2018.
 * for com.itachi1706.cheesecakeutilities.Modules.ListApplications.Fragments in CheesecakeUtilities
 */
public class ListApplicationFragment extends Fragment {

    RecyclerView recyclerView;
    ProgressBar bar;
    TextView label;
    TouchScrollBar scrollBar;

    public ListApplicationFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
         View v = inflater.inflate(R.layout.fragment_list_applications, container, false);

        recyclerView = v.findViewById(R.id.list_app_recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        scrollBar = v.findViewById(R.id.scrollBar);
        scrollBar.setIndicator(new CustomIndicator(getActivity()), true);

        bar = v.findViewById(R.id.list_app_pb);
        label = v.findViewById(R.id.list_app_pb_label);

        eval(false);

        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.modules_applist, menu);
        menu.findItem(R.id.systemapp).setChecked(checkSystem);
        menu.findItem(R.id.sortapi).setChecked(sortByApi);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private boolean checkSystem = false;
    private boolean sortByApi = false;
    private String appCountString = "";
    private List<String> appPackageNamesInstalled = new ArrayList<>();

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.systemapp: item.setChecked(!item.isChecked()); checkSystem = item.isChecked();
                eval(checkSystem); break;
            case R.id.sortapi: item.setChecked(!item.isChecked()); sortByApi = item.isChecked();
                eval(checkSystem); break;
            case R.id.graph:
                Intent i = new Intent(getActivity(), ListApplicationsApiGraphActivity.class);
                i.putExtra("appCount", appCountString);
                startActivity(i);
                break;
            case R.id.scan_ghost:
                new AlertDialog.Builder(getActivity()).setTitle("Scan Ghost Directories")
                        .setMessage("This will scan your external application data folder (/sdcard/Android) for any ghost directories " +
                                "left behind by applications no longer installed on your device")
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton("Scan", (dialog, which) -> hasStoragePermissionCheck()).show();
                break;
            default: return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void hasStoragePermissionCheck() {
        assert getActivity() != null;
        int rc = ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (rc != PackageManager.PERMISSION_GRANTED)
            requestStoragePermission();
        else
            scanGhostDir();
    }

    private static final int RC_HANDLE_REQUEST_STORAGE = 4;

    private void requestStoragePermission() {
        Log.w(PERM_MAN_TAG, "Storage permission is not granted. Requesting permission");
        final String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        assert getActivity() != null;
        if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(getActivity(), permissions, RC_HANDLE_REQUEST_STORAGE);
            return;
        }

        final Activity thisActivity = getActivity();

        new AlertDialog.Builder(getActivity()).setTitle("Requesting Storage Permission")
                .setMessage("This app requires ability to access your storage to scan your internal storage and " +
                        "perform cleanup activities for you")
                .setPositiveButton(android.R.string.ok, (dialog, which) -> ActivityCompat.requestPermissions(thisActivity, permissions, RC_HANDLE_REQUEST_STORAGE)).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        assert getActivity() != null;
        switch (requestCode) {
            case RC_HANDLE_REQUEST_STORAGE:
                if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(PERM_MAN_TAG, "Storage Permission Granted. Allowing Utility Access");
                    scanGhostDir();
                    return;
                }
                displayPermErrorMessage("You have denied the app ability to access your storage. This app will not be able to scan" +
                        " for ghost directories or perform ghost directories cleanup for you", grantResults, getActivity());
                break;
        }
    }

    private void scanGhostDir() {
        String androidDirString = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data";
        File androidDir = new File(androidDirString);
        if (!androidDir.exists() || !androidDir.isDirectory() || appPackageNamesInstalled.isEmpty()) {
            Toast.makeText(getContext(), "An error occurred when attempting to do a scan", Toast.LENGTH_LONG).show();
            return;
        }
        final String GHOST_DIR_TITLE = "Ghost Directory Scanning Complete";

        File[] subDir = androidDir.listFiles();
        final ArrayMap<String, String> listOfGhostDir = new ArrayMap<>();
        if (subDir.length == 0) {
            new AlertDialog.Builder(getContext()).setTitle(GHOST_DIR_TITLE)
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
            new AlertDialog.Builder(getContext()).setTitle(GHOST_DIR_TITLE)
                    .setMessage("You have no ghost directories found in your device")
                    .setPositiveButton(android.R.string.ok, null).show();
        } else {
            assert getContext() != null;
            new MaterialDialog.Builder(getContext()).title(GHOST_DIR_TITLE)
                    .items(listOfGhostDir.keySet()).itemsCallbackMultiChoice(null, (dialog, which, text) -> {
                int count = 0;
                StringBuilder removeMan = new StringBuilder();
                for (CharSequence t : text) {
                    String path = listOfGhostDir.get(t);
                    File del = new File(path);
                    try {
                        FileUtils.deleteDirectory(del);
                        count++;
                    } catch (IOException e) {
                        Log.e("GhostCleanup", "Unable to remove " + t);
                        removeMan.append(t).append("\n");
                    }
                }
                if (removeMan.length() > 0) {
                    new AlertDialog.Builder(getContext()).setTitle("Unable to remove some directories")
                            .setMessage("Some directories cannot be removed. Please remove them manually.\n\n" + removeMan)
                            .setPositiveButton(android.R.string.ok, null).show();
                }
                Toast.makeText(getContext(), "Cleaned up " + count + " directories", Toast.LENGTH_LONG).show();
                return true;
            }).positiveText("Clean Up").negativeText(android.R.string.cancel).show();
        }
    }

    private class LoadAppThread extends AsyncTask<Boolean, Void, Void> {

        private ArrayList<AppsItem> finalStr;

        @Override
        protected Void doInBackground(Boolean... params) {
            boolean system = params[0];
            PackageManager pm = getContext().getPackageManager();
            final List<ApplicationInfo> pkgAppsList = pm.getInstalledApplications(PackageManager.GET_META_DATA);
            finalStr = new ArrayList<>();
            for (ApplicationInfo i : pkgAppsList) {
                appPackageNamesInstalled.add(i.packageName);
                if (isSystemApp(i) && !system) continue;
                String version = "Unknown";
                try {
                    PackageInfo pInfo = getContext().getPackageManager().getPackageInfo(i.packageName, PackageManager.GET_PERMISSIONS);
                    version = pInfo.versionName;
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

                AppsItem item = new AppsItem(getContext());
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
            getActivity().runOnUiThread(() -> {
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

            StringBuilder appCount = new StringBuilder();
            for (Map.Entry<Integer, Integer> object : tmp.entrySet()) {
                appCount.append(object.getKey()).append(":").append(object.getValue()).append("-");
            }
            return appCount.substring(0, appCount.length() - 1);
        }

        private AppsAdapter finalAdapter;
    }
}