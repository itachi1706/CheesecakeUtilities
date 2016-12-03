package com.itachi1706.cheesecakeutilities.Modules.AppRestore;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.itachi1706.cheesecakeutilities.Modules.ListApplications.Helpers.BackupHelper;
import com.itachi1706.cheesecakeutilities.Modules.ListApplications.ListApplicationsActivity;
import com.itachi1706.cheesecakeutilities.Modules.ListApplications.Objects.AppsItem;
import com.itachi1706.cheesecakeutilities.Modules.ListApplications.RecyclerAdapters.AppsAdapter;
import com.itachi1706.cheesecakeutilities.R;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFileFilter;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collection;
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

        AppsAdapter adapter = new AppsAdapter(new AppsItem[0]);
        recyclerView.setAdapter(adapter);
        bar.setVisibility(View.VISIBLE);
        label.setVisibility(View.VISIBLE);
        new LoadAppThread().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class LoadAppThread extends AsyncTask<Boolean, Void, Void> {

        private ArrayList<AppsItem> finalStr;

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
            finalStr = new ArrayList<>();
            for (File f : apkfiles) {
                Log.d("File", f.getName());
                finalStr.add(new AppsItem(getApplicationContext(), f.getName(), f.getAbsolutePath(), 1, "1", "1", ""));
            }
            finalAdapter = new AppsAdapter(finalStr);

           return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (finalAdapter != null) recyclerView.setAdapter(finalAdapter);
            bar.setVisibility(View.GONE);
            label.setVisibility(View.GONE);
        }

        private AppsAdapter finalAdapter;
    }
}
