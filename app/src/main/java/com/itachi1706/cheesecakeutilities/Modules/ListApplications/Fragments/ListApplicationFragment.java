package com.itachi1706.cheesecakeutilities.Modules.ListApplications.Fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.itachi1706.cheesecakeutilities.Modules.ListApplications.Helpers.BackupHelper;
import com.itachi1706.cheesecakeutilities.Modules.ListApplications.ListApplicationsApiGraphActivity;
import com.itachi1706.cheesecakeutilities.Modules.ListApplications.ListApplicationsDetailActivity;
import com.itachi1706.cheesecakeutilities.Modules.ListApplications.LoadAppListTask;
import com.itachi1706.cheesecakeutilities.Modules.ListApplications.Objects.AppsItem;
import com.itachi1706.cheesecakeutilities.Modules.ListApplications.RecyclerAdapters.AppsAdapter;
import com.itachi1706.cheesecakeutilities.R;
import com.itachi1706.cheesecakeutilities.util.LogHelper;
import com.turingtechnologies.materialscrollbar.CustomIndicator;
import com.turingtechnologies.materialscrollbar.TouchScrollBar;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.CLIPBOARD_SERVICE;
import static com.itachi1706.cheesecakeutilities.util.CommonMethods.displayPermErrorMessage;
import static com.itachi1706.cheesecakeutilities.util.CommonVariables.PERM_MAN_TAG;
import static org.apache.commons.lang3.StringEscapeUtils.escapeCsv;

/**
 * Created by Kenneth on 18/4/2018.
 * for com.itachi1706.cheesecakeutilities.Modules.ListApplications.Fragments in CheesecakeUtilities
 */
public class ListApplicationFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar bar;
    private TextView label;

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
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        TouchScrollBar scrollBar = v.findViewById(R.id.scrollBar);
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
        if (getActivity() == null) return;
        new LoadAppListTask(getActivity(), (appCount, appPackageNameInstall, appPackageNameClean, finalAdapter) -> {
            appCountString = appCount;
            appPackageNamesCleaned = appPackageNameClean;
            appPackageNamesInstalled = appPackageNameInstall;

            // Done
            if (getActivity() != null) getActivity().runOnUiThread(() -> {
                recyclerView.setAdapter(finalAdapter);
                bar.setVisibility(View.GONE);
                label.setVisibility(View.GONE);
                getActivity().invalidateOptionsMenu();
            });
        }, sortByApi).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, system);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (appPackageNamesInstalled.size() == 0) return;

        if (appPackageNamesInstalled.contains("com.google.android.gms")) menu.findItem(R.id.view_gps).setEnabled(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.modules_applist, menu);
        menu.findItem(R.id.systemapp).setChecked(checkSystem);
        menu.findItem(R.id.sortapi).setChecked(sortByApi);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private boolean checkSystem = false;
    private boolean sortByApi = false;
    private String appCountString = "";
    private List<String> appPackageNamesInstalled = new ArrayList<>();
    private List<String> appPackageNamesCleaned = new ArrayList<>();

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
            case R.id.generate_package_list:
                generatePackageList();
                break;
            case R.id.view_gps:
                Intent gpsIntent = new Intent(getActivity(), ListApplicationsDetailActivity.class);
                gpsIntent.putExtra("packageName", "com.google.android.gms");
                startActivity(gpsIntent);
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
        LogHelper.w(PERM_MAN_TAG, "Storage permission is not granted. Requesting permission");
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
        if (requestCode == RC_HANDLE_REQUEST_STORAGE) {
            if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                LogHelper.i(PERM_MAN_TAG, "Storage Permission Granted. Allowing Utility Access");
                scanGhostDir();
                return;
            }
            displayPermErrorMessage("You have denied the app ability to access your storage. This app will not be able to scan" +
                    " for ghost directories or perform ghost directories cleanup for you", grantResults, getActivity());
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void generatePackageList() {
        if (getContext() == null) {
            LogHelper.e("ListApp", "Error getting context to generate package list");
            return;
        }
        if (appPackageNamesCleaned.isEmpty()) {
            Toast.makeText(getContext(), "No Apps Installed. Let the scan complete if it have not", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] options = {"Include App Name", "Include App Version", "Share as .txt/.csv file"};
        new MaterialDialog.Builder(getContext()).title("Generate App List")
                .items(options).itemsCallbackMultiChoice(null, (dialog, which, text) -> {
            boolean appName = checkIfSelectedOption(text, options[0]);
            boolean appVersion = checkIfSelectedOption(text, options[1]);
            boolean isFile = checkIfSelectedOption(text, options[2]);

            String fileOrCsv = generatePackageListString(appName, appVersion);

            File f = null;
            if (isFile) {
                String fileExt = ".txt"; // Default create txt file
                if (appName || appVersion) fileExt = ".csv"; // Create CSV file instead
                f = new File(getContext().getCacheDir().getAbsolutePath() + "/applist/applist" + fileExt);
                if (f.exists()) f.delete();
                try {
                    f.getParentFile().mkdirs();
                    f.createNewFile();
                    FileWriter fw = new FileWriter(f);
                    fw.append(fileOrCsv);
                    fw.flush();
                    fw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    f = null;
                    Toast.makeText(getContext(), "Failed to generate file, allowing copy to clipboard instead", Toast.LENGTH_SHORT).show();
                }
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).setTitle("Application List")
                    .setMessage(fileOrCsv).setNeutralButton(R.string.dialog_action_positive_close, null)
                    .setNegativeButton("Copy", (dialog12, which12) -> {
                        assert getActivity() != null;
                        ClipboardManager manager = (ClipboardManager) getActivity().getSystemService(CLIPBOARD_SERVICE);
                        assert manager != null;
                        ClipData data = ClipData.newPlainText("applist", fileOrCsv);
                        manager.setPrimaryClip(data);
                        Toast.makeText(getContext(), "Copied App List to clipboard", Toast.LENGTH_SHORT).show();
                    });

            if (f == null) {
                builder.setPositiveButton("Share", (dialog1, which1) -> {
                    Intent shareTextIntent = new Intent(Intent.ACTION_SEND);
                    shareTextIntent.setType("text/plain");
                    shareTextIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Application List");
                    shareTextIntent.putExtra(Intent.EXTRA_TEXT, fileOrCsv);
                    startActivity(Intent.createChooser(shareTextIntent, "Share App List with"));
                });
            } else {
                final File fileToShare = f;
                builder.setPositiveButton("Share", (dialog1, which2) -> BackupHelper.shareFile("ShareAppList", getContext(),
                        fileToShare, "Share App List File with", (appName || appVersion) ? "text/csv" : "text/plain"));
            }
            builder.show();
            return false;
        }).positiveText(android.R.string.ok).negativeText(android.R.string.cancel).show();
    }

    private String generatePackageListString(boolean appName, boolean appVersion) {
        assert getContext() != null;
        if (!appName && !appVersion) {
            StringBuilder sb = new StringBuilder();
            for (String s : appPackageNamesCleaned) {
                sb.append(s).append("\n");
            }
            return sb.toString().trim();
        }
        StringBuilder csv = new StringBuilder();
        PackageManager pm = getContext().getPackageManager();
        for (String packageName : appPackageNamesCleaned) {
            try {
                PackageInfo packageInfo = pm.getPackageInfo(packageName, 0);
                if (appName) csv.append(escapeCsv(packageInfo.applicationInfo.loadLabel(pm).toString())).append(",");
                csv.append(escapeCsv(packageName));
                if (appVersion) csv.append(",").append(escapeCsv(packageInfo.versionName));
                csv.append("\n");
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                if (appName) csv.append("Unknown App,");
                csv.append(escapeCsv(packageName));
                if (appVersion) csv.append(",").append("Unknown Version");
                csv.append("\n");
            }
        }
        return csv.toString().trim();
    }

    private boolean checkIfSelectedOption(CharSequence[] listToCheck, String selection) {
        for (CharSequence sel : listToCheck) {
            if (sel.equals(selection)) return true;
        }
        return false;
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
                        LogHelper.e("GhostCleanup", "Unable to remove " + t);
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
}
