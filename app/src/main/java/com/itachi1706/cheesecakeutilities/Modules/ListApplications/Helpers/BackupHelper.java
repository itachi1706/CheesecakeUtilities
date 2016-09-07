package com.itachi1706.cheesecakeutilities.Modules.ListApplications.Helpers;

import android.util.Log;

import com.itachi1706.cheesecakeutilities.Modules.FanfictionCompactor.Helpers.FileHelper;

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
            Log.i("Backup", "Existing file exists, removing " + apkFileName + "from directory");
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

    private static File getFolder() {
        String externalStorage = FileHelper.getExternalStorage();
        return new File(externalStorage + "/AndroidAppBackup");
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
}