package com.itachi1706.cheesecakeutilities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class ListApplicationsActivity extends BaseActivity {

    Button test, test2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_applications);

        test = (Button) findViewById(R.id.button);
        test2 = (Button) findViewById(R.id.button2);
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eval(true);
            }
        });
        test2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eval(false);
            }
        });
    }

    private void eval(boolean system) {
        final List<ApplicationInfo> pkgAppsList = getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);
        String test1 = "";
        for (ApplicationInfo i : pkgAppsList) {
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
        }
        new AlertDialog.Builder(this).setMessage(test1).setPositiveButton(android.R.string.ok, null).show();
    }

    private boolean isSystemApp(ApplicationInfo i) {
        return (i.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM;
    }

    @Override
    String getHelpDescription() {
        return "ListApplicationActivity";
    }
}
