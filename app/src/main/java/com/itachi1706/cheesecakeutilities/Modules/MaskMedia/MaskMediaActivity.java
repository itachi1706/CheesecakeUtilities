package com.itachi1706.cheesecakeutilities.Modules.MaskMedia;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.os.EnvironmentCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.itachi1706.cheesecakeutilities.R;
import com.itachi1706.cheesecakeutilities.Util.CommonMethods;

import java.io.File;

@RequiresApi(Build.VERSION_CODES.KITKAT)
public class MaskMediaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mask_media);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            CommonMethods.incompatible(this, "KitKat", "above");
            return;
        }

        hasStoragePermissionCheck();
    }

    private void continueInit() {
        // Request Root Removable Storage
        new AlertDialog.Builder(this).setTitle("External Storage")
                .setMessage("Do you have any removable storage? If so, click yes and select the root of the SD Card")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                        startActivityForResult(intent, RC_GET_EXTERNAL_STORAGE);
                    }
                }).setNegativeButton("No Removable Storage", null);

    }

    private static final int RC_HANDLE_REQUEST_STORAGE = 3, RC_GET_EXTERNAL_STORAGE = 4;

    private void hasStoragePermissionCheck() {
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (rc != PackageManager.PERMISSION_GRANTED)
            requestStoragePermission();
        else
            continueInit();
    }

    private void requestStoragePermission() {
        Log.w("PermMan", "Storage permission is not granted. Requesting permission");
        final String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_REQUEST_STORAGE);
            return;
        }

        final Activity thisActivity = this;

        new android.app.AlertDialog.Builder(this).setTitle("Requesting Storage Permission")
                .setMessage("This app requires ability to access your storage to mask media files")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(thisActivity, permissions, RC_HANDLE_REQUEST_STORAGE);
                    }
                }).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        final Activity thisActivity = this;
        switch (requestCode) {
            case RC_HANDLE_REQUEST_STORAGE:
                if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i("PermMan", "Storage Permission Granted. Allowing Utility Access");
                    continueInit();
                    return;
                }
                Log.e("PermMan", "Permission not granted: results len = " + grantResults.length +
                        " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));
                new android.app.AlertDialog.Builder(this).setTitle("Permission Denied")
                        .setMessage("You have denied the app ability to access your storage. This app will not be able to mask media files. The app will now exit")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .setCancelable(false)
                        .setNeutralButton("SETTINGS", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent permIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri packageURI = Uri.parse("package:" + thisActivity.getPackageName());
                                permIntent.setData(packageURI);
                                startActivity(permIntent);
                                finish();
                            }
                        }).show();
                break;
        }
    }
}
