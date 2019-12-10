package com.itachi1706.cheesecakeutilities.modules.ListApplications.Objects

/**
 * Created by Kenneth on 1/10/2016.
 * for com.itachi1706.cheesecakeutilities.Modules.ListApplications.Objects in CheesecakeUtilities
 */

data class LabelledColumn(var label: String? = null, var field: String? = null) {
    constructor(label: String, field: Int) : this(label, field.toString() + "")
}
