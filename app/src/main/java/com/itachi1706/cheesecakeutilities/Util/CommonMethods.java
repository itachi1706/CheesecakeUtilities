package com.itachi1706.cheesecakeutilities.Util;

import android.app.Activity;
import android.app.AlertDialog;

import java.text.DecimalFormat;

/**
 * Created by Kenneth on 3/19/2016.
 * for com.itachi1706.cheesecakeutilities.Util in CheesecakeUtilities
 */
public class CommonMethods {

    public static void betaInfo(Activity mActivity, String title) {
        new AlertDialog.Builder(mActivity).setTitle("BETA Utility")
                .setMessage("This utility (" + title + ") is currently being implemented and" +
                        " may or may not be present in the release application.\n\nBugs and Crashes " +
                        "are to be expected for the utility")
                .setPositiveButton(android.R.string.ok, null).show();
    }

    public static String readableFileSize(long size) {
        if(size <= 0) return "0";
        final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

}
