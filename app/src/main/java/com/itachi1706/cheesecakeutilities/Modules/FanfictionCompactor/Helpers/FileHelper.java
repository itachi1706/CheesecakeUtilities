package com.itachi1706.cheesecakeutilities.Modules.FanfictionCompactor.Helpers;

import android.os.Environment;

/**
 * Created by Kenneth on 7/6/2016.
 * for com.itachi1706.cheesecakeutilities.Modules.FanfictionCompactor in CheesecakeUtilities
 */
@Deprecated
public class FileHelper {

    /**
     * Get External Storage Path
     * @return Path of External Storage available to user
     */
    public static String getExternalStorage() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }
}
