package com.itachi1706.cheesecakeutilities.Modules.ListApplications.Objects

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat

import com.itachi1706.cheesecakeutilities.R

/**
 * Created by Kenneth on 14/8/2016.
 * for com.itachi1706.cheesecakeutilities.Modules.ListApplications.Objects in CheesecakeUtilities
 */
class AppsItem {
    var appName: String? = null
    var packageName: String? = null
    var version: String? = null
    var apiVersion: Int = 0
    var icon: Drawable? = null

    constructor(context: Context) {
        this.icon = ContextCompat.getDrawable(context, R.mipmap.ic_launcher)
    }

    constructor(context: Context, appName: String, apiVersion: Int, packageName: String, version: String) {
        this.appName = appName
        this.apiVersion = apiVersion
        this.packageName = packageName
        this.version = version
        this.icon = ContextCompat.getDrawable(context, R.mipmap.ic_launcher)
    }

    constructor(appName: String, apiVersion: Int, packageName: String, icon: Drawable, version: String) {
        this.appName = appName
        this.apiVersion = apiVersion
        this.packageName = packageName
        this.version = version
        this.icon = icon
    }
}
