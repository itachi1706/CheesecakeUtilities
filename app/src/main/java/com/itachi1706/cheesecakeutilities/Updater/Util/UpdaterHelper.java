package com.itachi1706.cheesecakeutilities.Updater.Util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.itachi1706.cheesecakeutilities.Updater.Objects.AppUpdateMessageObject;

/**
 * Created by Kenneth on 3/3/2016.
 * for com.itachi1706.cheesecakeutilities.Updater.Util in CheesecakeUtilities
 */
public class UpdaterHelper {

    public static int HTTP_QUERY_TIMEOUT = 15000; //15 seconds timeout

    /**
     * Retrieves the changelog of the device
     * @param changelog List of Changelog parsed
     * @return Changelog
     */
    public static String getChangelogStringFromArray(AppUpdateMessageObject[] changelog){
        StringBuilder changelogBuilder = new StringBuilder();
        for (AppUpdateMessageObject obj : changelog) {
            changelogBuilder.append("<b>Changelog for ").append(obj.getVersionName());
            // Get Labels
            changelogBuilder.append(" ").append(obj.getLabels()).append("</b><br/>");

            changelogBuilder.append(obj.getUpdateText().replace("\r\n", "<br/>"));
            changelogBuilder.append("<br/><br/>");
        }
        return changelogBuilder.toString();
    }

    /**
     * Determines if an app can check for update
     * NOTE: This requires you to have a "updatewifi" checkbox preference to utilize
     * @param sp Shared Preference of the Application to get "updatewifi" check from
     * @param context The application context
     * @return True if app can check for updates, false otherwise
     */
    public static boolean canCheckUpdate(SharedPreferences sp, Context context) {
        if (sp.getBoolean("updatewifi", false) && !ConnectivityHelper.isWifiConnection(context)) {
            Log.i("Updater", "Not on WIFI, Ignore Update Checking");
            return false;
        }

        if (!ConnectivityHelper.hasInternetConnection(context)) {
            Log.w("Updater", "No internet connection, skipping WiFi checking");
            return false;
        }
        return true;
    }

}
