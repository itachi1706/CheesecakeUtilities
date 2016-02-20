package com.itachi1706.cheesecakeutilities.Util;

import com.itachi1706.cheesecakeutilities.Updater.Objects.AppUpdateMessageObject;

/**
 * Created by itachi1706 on 2/20/2016.
 * For com.itachi1706.cheesecakeutilities.Util in Cheesecake Utilities.
 */
public class StaticVariables {

    public static int HTTP_QUERY_TIMEOUT = 15000; //15 seconds timeout

    /**
     * Retrieves the changelog of the device
     * @param changelog List of Changelog parsed
     * @return Changelog
     */
    public static String getChangelogStringFromArray(AppUpdateMessageObject[] changelog){
        StringBuilder changelogBuilder = new StringBuilder();
        for (int i = 0; i < changelog.length; i++) {
            AppUpdateMessageObject obj = changelog[i];
            changelogBuilder.append("<b>Changelog for ").append(obj.getVersionName());
            // Get Labels
            changelogBuilder.append(" ").append(obj.getLabels()).append("</b><br/>");

            changelogBuilder.append(obj.getUpdateText().replace("\r\n", "<br/>"));
            changelogBuilder.append("<br/><br/>");
        }
        return changelogBuilder.toString();
    }

}
