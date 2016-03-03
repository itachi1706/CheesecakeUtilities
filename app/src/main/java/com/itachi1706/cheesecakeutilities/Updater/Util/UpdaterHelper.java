package com.itachi1706.cheesecakeutilities.Updater.Util;

import com.google.gson.Gson;
import com.itachi1706.cheesecakeutilities.Updater.Objects.AppUpdateMessageObject;
import com.itachi1706.cheesecakeutilities.Updater.Objects.UpdateShell;

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

}
