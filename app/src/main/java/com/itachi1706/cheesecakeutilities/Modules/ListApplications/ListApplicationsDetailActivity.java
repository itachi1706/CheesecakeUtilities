package com.itachi1706.cheesecakeutilities.Modules.ListApplications;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.itachi1706.cheesecakeutilities.Modules.ListApplications.Objects.LabelledColumn;
import com.itachi1706.cheesecakeutilities.R;

import java.util.ArrayList;
import java.util.List;

public class ListApplicationsDetailActivity extends AppCompatActivity {

    TextView appName, appVersion;
    ImageView icon;
    GridLayout grid;
    LinearLayout creator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_applications_detail);

        String packageName = getIntent().getStringExtra("packageName");
        if (packageName == null) {
            Log.e("ListAppDetail", "Invalid Package Name. Exiting...");
            finish();
            return;
        }

        PackageManager pm = getPackageManager();
        ApplicationInfo info;
        try {
            info = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Log.e("ListAppDetail", "Failed to get info for " + packageName + ". Exiting");
            Toast.makeText(this, "Failed to get app info!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String version = "Unknown";
        String[] requestedPermissions = null;
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(info.packageName, PackageManager.GET_PERMISSIONS);
            requestedPermissions = pInfo.requestedPermissions;
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        String permissionList = "";
        if (requestedPermissions != null) {
            for (String s : requestedPermissions) {
                permissionList += s + "\n";
            }
        }

        appName = (TextView) findViewById(R.id.appName);
        appVersion = (TextView) findViewById(R.id.appVersion);
        icon = (ImageView) findViewById(R.id.iv_icon);
        grid = (GridLayout) findViewById(R.id.gridLayout);
        creator = (LinearLayout) findViewById(R.id.layout_creator);


        appName.setText(info.packageName);
        appVersion.setText("Version " + version);
        icon.setImageDrawable(info.loadIcon(pm));

        creator.addView(generateSingleColumn("Basic Information", "This is a test", "This is test 2"));
        List<LabelledColumn> testList = new ArrayList<>();
        testList.add(new LabelledColumn("Test1", "Testing"));
        testList.add(new LabelledColumn("Test2", "Testing more"));
        creator.addView(generateDualColumn("Test Info", testList));

    }

    private LinearLayout generateSingleColumn(String title, String... message) {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextAppearance(this, android.R.style.TextAppearance_Material_Medium);
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
        titleView.setTextAppearance(this, android.R.style.TextAppearance_Material_Medium);
        titleView.setPadding(0,20,0,20);
        l.addView(titleView);
        TextView labelView;
        LinearLayout detailLayout;
        TextView detailView;
        for (LabelledColumn lc : fields) {
            detailLayout = new LinearLayout(this);
            detailLayout.setOrientation(LinearLayout.HORIZONTAL);
            labelView = new TextView(this);
            labelView.setTextColor(Color.WHITE);
            detailView = new TextView(this);

            labelView.setText(lc.getLabel());
            detailView.setText(lc.getField());
            detailView.setPadding(0, 0, 0, 3);
            detailLayout.addView(labelView);
            detailLayout.addView(detailView);
            l.addView(detailLayout);
        }
        return l;
    }
}
