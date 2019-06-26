package com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.objects

/**
 * Created by Kenneth on 24/6/2019.
 * for com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.objects in CheesecakeUtilities
 */
data class GpaScoring(val name: String = "Some name", val shortname: String = "Some shortname", val description: String = "No description available", val passtier: List<GpaTier>? = null,
                      val gradetier: List<GpaTier> = ArrayList(), val type: String = "gpa") {
    data class GpaTier(val name: String = "Grade Tier", val desc: String = "No description", val value: Double = 0.0)
}