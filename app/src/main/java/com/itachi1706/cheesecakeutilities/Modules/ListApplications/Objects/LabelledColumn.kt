package com.itachi1706.cheesecakeutilities.Modules.ListApplications.Objects

/**
 * Created by Kenneth on 1/10/2016.
 * for com.itachi1706.cheesecakeutilities.Modules.ListApplications.Objects in CheesecakeUtilities
 */

class LabelledColumn {

    var label: String? = null
        private set
    var field: String? = null
        private set

    constructor(label: String, field: String) {
        this.label = label
        this.field = field
    }

    constructor(label: String, field: Int) {
        this.label = label
        this.field = field.toString() + ""
    }
}
