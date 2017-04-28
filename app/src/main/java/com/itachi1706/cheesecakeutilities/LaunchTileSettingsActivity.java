package com.itachi1706.cheesecakeutilities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class LaunchTileSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Check if tiles companion app is installed
        if (isQSTileModuleInstalled()) {
            // Launch configuration activity
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.itachi1706.cheesecakeutilities_tiles",
                    "com.itachi1706.cheesecakeutilities_tiles.ConfigurationActivity"));
            startActivity(intent);
        } else
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=com.itachi1706.cheesecakeutilities_tiles"))); // Launch Play Store
        finish();
    }

    private boolean isQSTileModuleInstalled() {
        try {
            getPackageManager().getPackageInfo("com.itachi1706.cheesecakeutilities_tiles", 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
