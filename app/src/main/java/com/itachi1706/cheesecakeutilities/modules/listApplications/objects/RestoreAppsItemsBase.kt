package com.itachi1706.cheesecakeutilities.modules.listApplications.objects

/**
 * Created by Kenneth on 3/12/2016.
 * for com.itachi1706.cheesecakeutilities.modules.AppRestore.Objects in CheesecakeUtilities
 */
open class RestoreAppsItemsBase {

    var isExpanded = false

    constructor()

    constructor(isExpanded: Boolean) {
        this.isExpanded = isExpanded
    }
}
