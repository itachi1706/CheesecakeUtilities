package com.itachi1706.cheesecakeutilities.Modules.ORDCountdown.json

/**
 * Created by Kenneth on 15/10/2017.
 * for com.itachi1706.cheesecakeutilities.Modules.ORDCountdown.json in CheesecakeUtilities
 */

class GCalHoliday {
    val startYear: String? = null
    val endYear: String? = null
    val yearRange: String? = null
    val timestamp: String? = null
    val msg: String? = null
    val size: Int = 0
    val error: Int = 0
    val isCache: Boolean = false
    val output: Array<GCalHolidayItem>? = null

    val timestampLong: Long
        get() = timestamp?.toLong() ?: 0


}
