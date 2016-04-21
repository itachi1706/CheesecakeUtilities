package com.itachi1706.cheesecakeutilities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.itachi1706.cheesecakeutilities.Util.CommonMethods;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class FanfictionCompactorActivity extends AppCompatActivity {

    TextView folder, database, folderSize;
    Button fileSizeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fanfiction_compactor);

        folder = (TextView) findViewById(R.id.tvFolder);
        database = (TextView) findViewById(R.id.tvDB);
        folderSize = (TextView) findViewById(R.id.tvSize);
        fileSizeButton = (Button) findViewById(R.id.btnGetFolderSize);

        fileSizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFileSize();
            }
        });

        folder.setText(getDefaultFolder().getAbsolutePath());
        database.setText("Unimplemented");

        CommonMethods.betaInfo(this, "Fanfiction Compactor");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.binhex_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                new AlertDialog.Builder(this)
                        .setMessage("Compacts fanfiction folders by removing unneeded folders/files.\n\n" +
                                "This is a BETA utility and may not appear in the release build of the application")
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.ok, null).show();
                return true;
            case R.id.exit:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void getFileSize() {
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            canGetFileSize();
        } else {
            requestStoragePermission();
        }
    }

    private File getDefaultFolder() {
        String external = Environment.getExternalStorageDirectory().getAbsolutePath();
        return new File(external + "/FanfictionReader/stories");
    }

    private void canGetFileSize() {
        File file = getDefaultFolder();
        long totalSize = getFileSize(file);
        this.folderSize.setText(CommonMethods.readableFileSize(totalSize) + " (" + totalSize + " bytes)");
    }

    public static long getFileSize(final File file)
    {
        if(file==null||!file.exists())
            return 0;
        if(!file.isDirectory())
            return file.length();
        final List<File> dirs= new LinkedList<>();
        dirs.add(file);
        long result=0;
        while(!dirs.isEmpty())
        {
            final File dir=dirs.remove(0);
            if(!dir.exists())
                continue;
            final File[] listFiles=dir.listFiles();
            if(listFiles==null||listFiles.length==0)
                continue;
            for(final File child : listFiles)
            {
                result+=child.length();
                if(child.isDirectory())
                    dirs.add(child);
            }
        }
        return result;
    }

    private static final int RC_HANDLE_REQUEST_STORAGE = 3;

    private void requestStoragePermission() {
        Log.w("PermMan", "Storage permission is not granted. Requesting permission");
        final String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_REQUEST_STORAGE);
            return;
        }

        final Activity thisActivity = this;

        new AlertDialog.Builder(this).setTitle("Requesting Storage Permission")
                .setMessage("This app requires ability to access your storage to calculate file size and compact Fanfictions")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(thisActivity, permissions, RC_HANDLE_REQUEST_STORAGE);
                    }
                }).show();
    }


    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        final Activity thisActivity = this;
        switch (requestCode) {
            case RC_HANDLE_REQUEST_STORAGE:
                if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("PermMan", "Storage Permission Granted. Getting File Size");
                    canGetFileSize();
                    return;
                }
                Log.e("PermMan", "Permission not granted: results len = " + grantResults.length +
                        " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));
                new AlertDialog.Builder(this).setTitle("Permission Denied")
                        .setMessage("You have denied the app ability to access your storage. This app will not be able to calculate" +
                                " file size or compact Fanfictions")
                        .setPositiveButton(android.R.string.ok, null)
                        .setNeutralButton("SETTINGS", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent permIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri packageURI = Uri.parse("package:" + thisActivity.getPackageName());
                                permIntent.setData(packageURI);
                                startActivity(permIntent);
                            }
                        }).show();
                break;
        }
    }
}
