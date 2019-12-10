package com.itachi1706.cheesecakeutilities.objects

/**
 * Created by Kenneth on 29/4/2017.
 * Modified to Kotlin on 24/11/2017.
 * for com.itachi1706.cheesecakeutilities.Objects in CheesecakeUtilities
 */

data class DualLineString(var main: String?, var sub: String?, var extra: Any?) {
    constructor(main: String?, sub: String?): this(main, sub, null)
}
