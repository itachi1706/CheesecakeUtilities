package com.itachi1706.cheesecakeutilities.Modules.FanfictionCompactor.Helpers;

import android.os.Environment;
import android.os.StatFs;

import com.itachi1706.appupdater.Util.DeprecationHelper;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Kenneth on 7/6/2016.
 * for com.itachi1706.cheesecakeutilities.Modules.FanfictionCompactor in CheesecakeUtilities
 */
public class FileHelper {

    /**
     * Get default folder of stories
     * @return Stories File Object
     */
    public static File getDefaultFolder() {
        String external = getExternalStorage();
        return new File(external + "/FanfictionReader/stories");
    }

    /**
     * Get file size
     * @param file File object (folder)
     * @return size of file/folder
     */
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

    /**
     * Get External Storage Path
     * @return Path of External Storage available to user
     */
    public static String getExternalStorage() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    /**
     * Get Backup file folder
     * @return Backup File Object
     */
    public static File getBackupFolder() {
        String external = getExternalStorage();
        File file = new File(external + "/FanfictionReader/backups");
        if (file.exists() && !file.isDirectory() && file.delete() && file.mkdir()) {
            return file;
        }

        if (!file.exists() && file.mkdir()) {
            return file;
        }

        return file;
    }

    /**
     * Gets the size available to the user
     * @param f File object
     * @return size available in MB
     */
    public static float megabytesAvailable(File f) {
        StatFs stat = new StatFs(f.getPath());
        long bytesAvailable;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            bytesAvailable = stat.getBlockSizeLong() * stat.getAvailableBlocksLong();
        } else {
            bytesAvailable = DeprecationHelper.StatFs.getBlockSize(stat) * DeprecationHelper.StatFs.getAvailableBlocks(stat);
        }
        return bytesAvailable / (1024.f * 1024.f);
    }

    /**
     * Gets the story count
     * @param file Folder Object (Must be folder with list of stories as directories)
     * @return number of directories in folder
     */
    public static int getStoryFolderCount(final File file) {
        return getStoryFolderCount(file, false);
    }

    /**
     * Gets the story count
     * @param file Folder Object (Must be folder with list of stories as directories)
     * @param countFiles Whether to count the files or not
     * @return count size
     */
    public static int getStoryFolderCount(final File file, boolean countFiles) {
        if (file == null || !file.exists() || !file.isDirectory())
            return 0;
        File[] files = file.listFiles();

        if (files == null)
            return 0;
        int count = 0;
        for (File f : files) {
            if (!f.isDirectory() && !countFiles) continue;
            count++;
        }
        return count;
    }
}
