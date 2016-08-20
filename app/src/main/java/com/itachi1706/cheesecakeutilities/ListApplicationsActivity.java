package com.itachi1706.cheesecakeutilities;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.itachi1706.cheesecakeutilities.Modules.ListApplications.Objects.AppsItem;
import com.itachi1706.cheesecakeutilities.Modules.ListApplications.RecyclerAdapters.AppsAdapter;

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

            AppsItem item = new AppsItem(this);
            item.setApiVersion(i.targetSdkVersion + "");
            item.setAppName(i.loadLabel(pm).toString());
            item.setAppPath(i.sourceDir);
            item.setPackageName(i.packageName);
            item.setIcon(i.loadIcon(pm));
            finalStr.add(item);
        }
        AppsAdapter adapter = new AppsAdapter(finalStr);
        recyclerView.setAdapter(adapter);
    }

    private boolean isSystemApp(ApplicationInfo i) {
        return (i.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM;
    }

    @Override
    String getHelpDescription() {
        return "ListApplicationActivity";
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
}
