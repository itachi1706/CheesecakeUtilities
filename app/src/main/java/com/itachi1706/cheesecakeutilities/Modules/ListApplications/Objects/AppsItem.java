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
    private String appName, appPath, packageName, version;
    private int apiVersion;
    private Drawable icon;

    public AppsItem(Context context) {
        this.icon = ContextCompat.getDrawable(context, R.mipmap.ic_launcher);
    }

    public AppsItem(Context context, String appName, String appPath, int apiVersion, String packageName, String version) {
        this.appName = appName;
        this.appPath = appPath;
        this.apiVersion = apiVersion;
        this.packageName = packageName;
        this.version = version;
        this.icon = ContextCompat.getDrawable(context, R.mipmap.ic_launcher);
    }

    public AppsItem(String appName, String appPath, int apiVersion, String packageName, Drawable icon, String version) {
        this.appName = appName;
        this.appPath = appPath;
        this.apiVersion = apiVersion;
        this.packageName = packageName;
        this.version = version;
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

    public int getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(int apiVersion) {
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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
