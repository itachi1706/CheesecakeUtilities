package com.itachi1706.cheesecakeutilities.huh.modules.gpaCalculator.objects

/**
 * Created by Kenneth on 26/6/2019.
 * for com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.objects in CheesecakeUtilities
 */
data class GpaInstitution (val name: String = "", val shortName: String = "", val type: String = "", val creditName: String? = null,
                           val order: Long = Long.MAX_VALUE, val semester: HashMap<String, GpaSemester> = HashMap(), val gpa: String = "Unknown",
                           val startTimestamp: Long = System.currentTimeMillis(), val endTimestamp: Long = -1)