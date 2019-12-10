package com.itachi1706.cheesecakeutilities.huh.modules.gpaCalculator.objects

/**
 * Created by Kenneth on 24/6/2019.
 * for com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.objects in CheesecakeUtilities
 */
data class GpaScoring(val name: String = "Some name", val shortname: String = "Some shortname", val description: String = "No description available", val passtier: List<GpaTier>? = null,
                      val gradetier: List<GpaTier> = ArrayList(), val type: String = "gpa", val finalGradeColor: List<GpaColor>? = null) {
    data class GpaTier(val name: String = "Grade Tier", val desc: String = "No description", val value: Double = 0.0)
    data class GpaColor(val from: Double = 0.0, val to: Double = 0.0, val color: String = "None")
}