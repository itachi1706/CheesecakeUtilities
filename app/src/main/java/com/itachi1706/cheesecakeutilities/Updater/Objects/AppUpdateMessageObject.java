package com.itachi1706.cheesecakeutilities.Updater.Objects;

/**
 * Created by itachi1706 on 2/20/2016.
 * For com.itachi1706.cheesecakeutilities.Updater.Objects in Cheesecake Utilities.
 */
public class AppUpdateMessageObject {
    private int index;
    private String id, appid, updateText, dateModified, versionCode, versionName, labels, url;

    public int getIndex() {
        return index;
    }

    public String getId() {
        return id;
    }

    public String getAppid() {
        return appid;
    }

    public String getUpdateText() {
        return updateText;
    }

    public String getDateModified() {
        return dateModified;
    }

    public String getVersionCode() {
        return versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public String getLabels() {
        return labels;
    }

    public String getUrl() {
        return url;
    }
}
