package com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.objects

/**
 * Created by Kenneth on 26/6/2019.
 * for com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.objects in CheesecakeUtilities
 */
data class GpaInstitution (val name: String, val shortName: String, val type: String, val creditName: String? = null,
                           val order: Int = Integer.MAX_VALUE, val semester: ArrayList<GpaSemester> = ArrayList())