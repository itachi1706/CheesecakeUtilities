package com.itachi1706.cheesecakeutilities.Modules.AppRestore.Objects;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

import com.itachi1706.cheesecakeutilities.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kenneth on 3/12/2016.
 * for com.itachi1706.cheesecakeutilities.Modules.AppRestore.Objects in CheesecakeUtilities
 */
public class RestoreAppsItemsHeader extends RestoreAppsItemsBase {
    private String appName;
    private int count = 0;
    private Drawable appIcon;
    private List<RestoreAppsItemsFooter> child;

    public RestoreAppsItemsHeader(String appName, Context context) {
        super();
        this.appName = appName;
        this.appIcon = ContextCompat.getDrawable(context, R.mipmap.ic_launcher);
        this.child = new ArrayList<>();
        this.count = this.child.size();
    }

    public RestoreAppsItemsHeader(String appName, Drawable appIcon) {
        super();
        this.appName = appName;
        this.appIcon = appIcon;
        this.child = new ArrayList<>();
        this.count = this.child.size();
    }

    public RestoreAppsItemsHeader(String appName, List<RestoreAppsItemsFooter> child, Context context) {
        super();
        this.appName = appName;
        this.child = child;
        this.count = this.child.size();
        this.appIcon = ContextCompat.getDrawable(context, R.mipmap.ic_launcher);
    }

    public RestoreAppsItemsHeader(String appName, List<RestoreAppsItemsFooter> child, Drawable appIcon) {
        super();
        this.appName = appName;
        this.child = child;
        this.count = this.child.size();
        this.appIcon = appIcon;
    }


    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<RestoreAppsItemsFooter> getChild() {
        return child;
    }

    public void setChild(List<RestoreAppsItemsFooter> child) {
        this.child = child;
        this.count = this.child.size();
    }

    public boolean hasChild(){
        return this.child != null && this.child.size() != 0;
    }

    public Drawable getIcon() {
        return appIcon;
    }

    public void setIcon(Drawable appIcon) {
        this.appIcon = appIcon;
    }
}