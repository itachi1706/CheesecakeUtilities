package com.itachi1706.cheesecakeutilities;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.itachi1706.cheesecakeutilities.RecyclerAdapters.StringRecyclerAdapter;

import java.util.ArrayList;
import java.util.List;

public class ListApplicationsActivity extends BaseActivity {

    Button systemApps, dataApps;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_applications);

        systemApps = (Button) findViewById(R.id.btn_system_apps);
        dataApps = (Button) findViewById(R.id.btn_data_apps);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_test);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        eval(false);

        systemApps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eval(true);
            }
        });
        dataApps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eval(false);
            }
        });
    }

    private void eval(boolean system) {
        final List<ApplicationInfo> pkgAppsList = getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);
        ArrayList<String> finalStr = new ArrayList<>();
        for (ApplicationInfo i : pkgAppsList) {
            String test1 = "";
            if (isSystemApp(i)) {
                if (!system) continue;
            } else {
                if (system) continue;
            }
            test1 += i.packageName + "\n";
            test1 += i.sourceDir + "\n";
            test1 += i.dataDir + "\n";
            if (isSystemApp(i)) {
                test1 += "System\n";
            } else {
                test1 += "Not System\n";
            }

            if ((i.flags & ApplicationInfo.FLAG_DEBUGGABLE) == ApplicationInfo.FLAG_DEBUGGABLE) {
                test1 += "DEBUGGABLE\n";
            }
            test1 += "API " + i.targetSdkVersion + "\n\n";
            finalStr.add(test1);
        }
        StringRecyclerAdapter adapter = new StringRecyclerAdapter(finalStr);
        recyclerView.setAdapter(adapter);
    }

    private boolean isSystemApp(ApplicationInfo i) {
        return (i.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM;
    }

    @Override
    String getHelpDescription() {
        return "ListApplicationActivity";
    }
}
