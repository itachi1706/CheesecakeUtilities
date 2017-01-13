package com.itachi1706.cheesecakeutilities.Util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    public static void incompatible(final Activity mActivity, String version, String status) {
        new AlertDialog.Builder(mActivity).setTitle("Unsupported Android Version")
                .setMessage("This utility is only compatible with Android " + version + " and " + status)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mActivity.finish();
                    }
                }).show();
    }

    public static String readableFileSize(long size) {
        if(size <= 0) return "0";
        final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public static boolean isGlobalLocked(SharedPreferences sp) {
        return sp.getBoolean("global_applock", true);
    }

    public static boolean isUtilityLocked(SharedPreferences sp, String utilityName) {
        String lockedUtil = sp.getString("utilLocked", "");
        if (lockedUtil.isEmpty() || lockedUtil.equals("")) return false;
        List<String> locked = new ArrayList<>(Arrays.asList(lockedUtil.split("\\|\\|\\|")));
        return locked.contains(utilityName);
    }

}
