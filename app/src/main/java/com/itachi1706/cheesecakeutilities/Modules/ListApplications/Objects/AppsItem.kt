package com.itachi1706.cheesecakeutilities.Modules.ListApplications.Objects

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat

import com.itachi1706.cheesecakeutilities.R

/**
 * Created by Kenneth on 14/8/2016.
 * for com.itachi1706.cheesecakeutilities.Modules.ListApplications.Objects in CheesecakeUtilities
 */
data class AppsItem(var appName: String? = null, var apiVersion: Int = 0, var packageName: String? = null, var icon: Drawable? = null, var version: String? = null) {
    constructor(context: Context): this(icon=ContextCompat.getDrawable(context, R.mipmap.ic_launcher))
    constructor(context: Context, appName: String, apiVersion: Int, packageName: String, version: String): this(appName,apiVersion,packageName,ContextCompat.getDrawable(context, R.mipmap.ic_launcher),version)
}
