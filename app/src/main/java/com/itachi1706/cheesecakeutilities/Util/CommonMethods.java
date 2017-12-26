package com.itachi1706.cheesecakeutilities.Util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;

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

    public static boolean isColorDark(int color){
        double darkness = 1-(0.299* Color.red(color) + 0.587*Color.green(color) + 0.114*Color.blue(color))/255;
        if(darkness<0.5){
            return false; // It's a light color
        }else{
            return true; // It's a dark color
        }
    }

}
