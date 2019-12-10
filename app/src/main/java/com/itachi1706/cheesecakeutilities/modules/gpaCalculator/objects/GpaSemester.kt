package com.itachi1706.cheesecakeutilities.modules.gpaCalculator.objects

/**
 * Created by Kenneth on 26/6/2019.
 * for com.itachi1706.cheesecakeutilities.modules.gpaCalculator.objects in CheesecakeUtilities
 */
data class GpaSemester(val name: String = "", val order: Long = Long.MAX_VALUE, val modules: HashMap<String, GpaModule> = HashMap(), val gpa: String = "Unknown",
                       val startTimestamp: Long = System.currentTimeMillis(), val endTimestamp: Long = -1)