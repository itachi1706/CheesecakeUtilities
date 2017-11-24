package com.itachi1706.cheesecakeutilities.Modules.AppRestore.Objects

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat

import com.itachi1706.cheesecakeutilities.R

import java.util.ArrayList

/**
 * Created by Kenneth on 3/12/2016.
 * for com.itachi1706.cheesecakeutilities.Modules.AppRestore.Objects in CheesecakeUtilities
 */
class RestoreAppsItemsHeader : RestoreAppsItemsBase {
    var appName: String? = null
    var count = 0
    var icon: Drawable? = null
    private var child: List<RestoreAppsItemsFooter>? = null

    constructor(appName: String, context: Context) : super() {
        this.appName = appName
        this.icon = ContextCompat.getDrawable(context, R.mipmap.ic_launcher)
        this.child = ArrayList()
        this.count = this.child!!.size
    }

    constructor(appName: String, appIcon: Drawable) : super() {
        this.appName = appName
        this.icon = appIcon
        this.child = ArrayList()
        this.count = this.child!!.size
    }

    constructor(appName: String, child: List<RestoreAppsItemsFooter>, context: Context) : super() {
        this.appName = appName
        this.child = child
        this.count = this.child!!.size
        this.icon = ContextCompat.getDrawable(context, R.mipmap.ic_launcher)
    }

    constructor(appName: String, child: List<RestoreAppsItemsFooter>, appIcon: Drawable) : super() {
        this.appName = appName
        this.child = child
        this.count = this.child!!.size
        this.icon = appIcon
    }

    fun getChild(): List<RestoreAppsItemsFooter>? {
        return child
    }

    fun setChild(child: List<RestoreAppsItemsFooter>) {
        this.child = child
        this.count = this.child!!.size
    }

    fun hasChild(): Boolean {
        return this.child != null && this.child!!.size != 0
    }
}
