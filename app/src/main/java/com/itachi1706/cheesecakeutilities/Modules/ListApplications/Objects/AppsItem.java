package com.itachi1706.cheesecakeutilities.Modules.ListApplications.Objects;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

import com.itachi1706.cheesecakeutilities.R;

/**
 * Created by Kenneth on 14/8/2016.
 * for com.itachi1706.cheesecakeutilities.Modules.ListApplications.Objects in CheesecakeUtilities
 */
public class AppsItem {
    private String appName, appPath, apiVersion, packageName;
    private Drawable icon;

    public AppsItem(Context context) {
        this.icon = ContextCompat.getDrawable(context, R.mipmap.ic_launcher);
    }

    public AppsItem(Context context, String appName, String appPath, String apiVersion, String packageName) {
        this.appName = appName;
        this.appPath = appPath;
        this.apiVersion = apiVersion;
        this.packageName = packageName;
        this.icon = ContextCompat.getDrawable(context, R.mipmap.ic_launcher);
    }

    public AppsItem(String appName, String appPath, String apiVersion, String packageName, Drawable icon) {
        this.appName = appName;
        this.appPath = appPath;
        this.apiVersion = apiVersion;
        this.packageName = packageName;
        this.icon = icon;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppPath() {
        return appPath;
    }

    public void setAppPath(String appPath) {
        this.appPath = appPath;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }
}
