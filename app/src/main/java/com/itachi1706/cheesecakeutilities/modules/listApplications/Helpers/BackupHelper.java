package com.itachi1706.cheesecakeutilities.modules.listApplications.Helpers;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import androidx.core.content.FileProvider;

import com.itachi1706.cheesecakeutilities.util.LogHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Kenneth on 28/8/2016.
 * for com.itachi1706.cheesecakeutilities.Modules.ListApplications in CheesecakeUtilities
 */
public class BackupHelper {

    public static boolean backupApk(String fileLocation, String apkFileName) throws IOException {
        if (!createFolder()) {
            return false;
        }

        File outFile = new File(getFolder().getAbsolutePath() + "/" + apkFileName);
        if (outFile.exists()) {
            LogHelper.i("Backup", "Existing file exists, removing " + apkFileName + "from directory");
            if (!outFile.delete()) {
                throw new IOException("File exists and cannot be deleted");
            }
        }
        File inFile = new File(fileLocation);

        InputStream in = new FileInputStream(inFile);
        OutputStream out = new FileOutputStream(outFile);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
        return true;
    }

    public static File getFolder() {
        return new File(Environment.getExternalStorageDirectory(), "AndroidAppBackup");
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static boolean createFolder() {
        File folder = getFolder();
        if (folder.exists() && folder.isDirectory() && folder.canWrite()) {
            return true;
        }
        if (!folder.exists()) {
            return folder.mkdir();
        }
        return !folder.isDirectory() && folder.delete() && folder.mkdir();
    }

    public static void shareFile(String TAG, Context context, File shareFile, String shareIntentTitle, String mime) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType(mime);
        Uri shareUri;
        // Android O Strict Mode crash fix
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LogHelper.i(TAG, "Post-Oreo: Using new Content URI method");
            LogHelper.i(TAG, "Invoking Content Provider " + context.getPackageName() + ".provider");
            shareUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", shareFile);
        } else {
            LogHelper.i(TAG, "Pre-Oreo: Fallbacking to old method as it worked previously");
            shareUri = Uri.fromFile(shareFile);
        }
        shareIntent.putExtra(Intent.EXTRA_STREAM, shareUri);
        context.startActivity(Intent.createChooser(shareIntent, shareIntentTitle).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }
}
