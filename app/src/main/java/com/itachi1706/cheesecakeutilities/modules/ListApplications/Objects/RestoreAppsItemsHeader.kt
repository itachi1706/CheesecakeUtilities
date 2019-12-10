package com.itachi1706.cheesecakeutilities.modules.ListApplications.Objects

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.itachi1706.cheesecakeutilities.R

/**
 * Created by Kenneth on 3/12/2016.
 * for com.itachi1706.cheesecakeutilities.Modules.AppRestore.Objects in CheesecakeUtilities
 */
data class RestoreAppsItemsHeader(var appName: String? = null, var count: Int = 0, var icon: Drawable? = null, private var child: List<RestoreAppsItemsFooter>? = null) : RestoreAppsItemsBase() {
    constructor(appName: String, context: Context) : this(appName = appName, icon = ContextCompat.getDrawable(context, R.mipmap.ic_launcher), child = ArrayList<RestoreAppsItemsFooter>()) {
        this.count = this.child!!.size
    }

    constructor(appName: String, appIcon: Drawable) : this(appName = appName, icon = appIcon, child = ArrayList<RestoreAppsItemsFooter>()) {
        this.count = this.child!!.size
    }

    constructor(appName: String, child: List<RestoreAppsItemsFooter>, context: Context) : this(appName = appName, child = child, icon = ContextCompat.getDrawable(context, R.mipmap.ic_launcher)) {
        this.count = this.child!!.size
    }

    constructor(appName: String, child: List<RestoreAppsItemsFooter>, appIcon: Drawable) : this(appName = appName, child = child, icon = appIcon) {
        this.count = this.child!!.size
    }

    fun getChild(): List<RestoreAppsItemsFooter>? {
        return child
    }

    fun setChild(child: List<RestoreAppsItemsFooter>) {
        this.child = child
        this.count = this.child!!.size
    }

    fun hasChild(): Boolean {
        return this.child != null && this.child!!.isNotEmpty()
    }
}
