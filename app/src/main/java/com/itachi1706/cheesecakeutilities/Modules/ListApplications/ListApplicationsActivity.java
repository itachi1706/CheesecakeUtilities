package com.itachi1706.cheesecakeutilities.Modules.ListApplications;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.itachi1706.cheesecakeutilities.BaseActivity;
import com.itachi1706.cheesecakeutilities.Modules.ListApplications.Objects.AppsItem;
import com.itachi1706.cheesecakeutilities.Modules.ListApplications.RecyclerAdapters.AppsAdapter;
import com.itachi1706.cheesecakeutilities.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ListApplicationsActivity extends BaseActivity {

    RecyclerView recyclerView;
    ProgressBar bar;
    TextView label;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_applications);

        recyclerView = (RecyclerView) findViewById(R.id.list_app_recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        bar = (ProgressBar) findViewById(R.id.list_app_pb);
        label = (TextView) findViewById(R.id.list_app_pb_label);

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
            default: return super.onOptionsItemSelected(item);
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
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    recyclerView.setAdapter(finalAdapter);
                    bar.setVisibility(View.GONE);
                    label.setVisibility(View.GONE);
                }
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
