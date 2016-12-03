package com.itachi1706.cheesecakeutilities.Modules.AppRestore;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.itachi1706.cheesecakeutilities.Modules.AppRestore.Objects.RestoreAppsItemsBase;
import com.itachi1706.cheesecakeutilities.Modules.AppRestore.Objects.RestoreAppsItemsFooter;
import com.itachi1706.cheesecakeutilities.Modules.AppRestore.Objects.RestoreAppsItemsHeader;
import com.itachi1706.cheesecakeutilities.Modules.AppRestore.RecyclerAdapters.RestoreAppsAdapter;
import com.itachi1706.cheesecakeutilities.Modules.ListApplications.Helpers.BackupHelper;
import com.itachi1706.cheesecakeutilities.R;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class RestoreAppActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ProgressBar bar;
    TextView label;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restore_app);

        recyclerView = (RecyclerView) findViewById(R.id.list_app_recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        bar = (ProgressBar) findViewById(R.id.list_app_pb);
        label = (TextView) findViewById(R.id.list_app_pb_label);
    }

    @Override
    public void onResume() {
        super.onResume();

        RestoreAppsAdapter adapter = new RestoreAppsAdapter(new RestoreAppsItemsBase[0]);
        recyclerView.setAdapter(adapter);
        bar.setVisibility(View.VISIBLE);
        label.setVisibility(View.VISIBLE);
        new LoadAppThread().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class LoadAppThread extends AsyncTask<Boolean, Void, Void> {

        private HashMap<String, RestoreAppsItemsBase> items;
        private RestoreAppsAdapter finalAdapter;

        @Override
        protected Void doInBackground(Boolean... params) {
            if (!BackupHelper.createFolder()) {
                Log.e("RestoreApp", "Unable to initialize backup folder. Exiting...");
                return null;
            }
            File backupFolder = BackupHelper.getFolder();
            if (!backupFolder.isDirectory()) {
                Log.e("RestoreApp", "Invalid Backup Folder. Is not a directory");
                return null;
            }

            String[] ext = {"apk"};
            Collection<File> apkfiles = FileUtils.listFiles(backupFolder, ext, false);
            PackageManager pm = getPackageManager();
            items = new HashMap<>();
            for (File f : apkfiles) {
                Log.d("File", f.getName());

                PackageInfo info = pm.getPackageArchiveInfo(f.getAbsolutePath(), PackageManager.GET_META_DATA);
                info.applicationInfo.sourceDir = f.getAbsolutePath();
                info.applicationInfo.publicSourceDir = f.getAbsolutePath();

                // Group into specifics
                if (!items.containsKey(info.packageName)) {
                    // Does not exist, create a new record
                    RestoreAppsItemsBase header = new RestoreAppsItemsHeader(info.applicationInfo.loadLabel(pm).toString(),
                            info.applicationInfo.loadIcon(pm));
                    items.put(info.packageName, header);
                }

                // Add the version
                RestoreAppsItemsHeader header = (RestoreAppsItemsHeader) items.get(info.packageName);
                List<RestoreAppsItemsFooter> children = header.getChild();
                boolean alrExist = false;
                for (RestoreAppsItemsFooter c : children) {
                    // Make sure version does not exist alr
                    if (c.getVersion().equals(info.versionName)) {
                        alrExist = true;
                        break;
                    }
                }

                if (!alrExist) {
                    RestoreAppsItemsFooter child = new RestoreAppsItemsFooter(f.getAbsolutePath(), info.versionName);
                    children.add(child);
                    header.setChild(children);
                    items.put(info.packageName, header);
                }
            }
            List<RestoreAppsItemsBase> tmp = new ArrayList<>(items.values());
            finalAdapter = new RestoreAppsAdapter(tmp);

           return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (finalAdapter != null) recyclerView.setAdapter(finalAdapter);
            bar.setVisibility(View.GONE);
            label.setVisibility(View.GONE);
        }
    }
}