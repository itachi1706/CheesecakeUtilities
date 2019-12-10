package com.itachi1706.cheesecakeutilities.modules.ordCountdown.json

import java.util.*

/**
 * Created by Kenneth on 15/10/2017.
 * for com.itachi1706.cheesecakeutilities.modules.ORDCountdown.json in CheesecakeUtilities
 */

class GCalHolidayItem {
    val name: String? = null
    val date: String? = null

    val dateInMillis: Long
        get() {
            val dates = date!!.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val cal = Calendar.getInstance()
            cal.set(Integer.parseInt(dates[0]), Integer.parseInt(dates[1]) - 1, Integer.parseInt(dates[2]), 0, 0, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.timeInMillis
        }
}
