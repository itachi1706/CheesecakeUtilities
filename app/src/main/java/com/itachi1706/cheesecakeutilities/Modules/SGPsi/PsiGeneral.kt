package com.itachi1706.cheesecakeutilities.Modules.SGPsi

import com.itachi1706.cheesecakeutilities.Util.ColorUtils

/**
 * Created by Kenneth on 18/2/2018.
 * for com.itachi1706.cheesecakeutilities.Modules.SGPsi in CheesecakeUtilities
 */

class PsiGeneral {

    val psirange: String? = null
    val particlerange: String? = null
    val time: String? = null
    val threehr: Int = 0
    val north: Int = 0
    val south: Int = 0
    val east: Int = 0
    val west: Int = 0
    val central: Int = 0
    val global: Int = 0
    val particlenorth: Int = 0
    val particlesouth: Int = 0
    val particleeast: Int = 0
    val particlewest: Int = 0
    val particlecentral: Int = 0
    val rawtimestamp: Long = 0

    @JvmOverloads
    fun getColor(value: Int, nightMode: Boolean = false): Int {
        if (value <= 50) return if (nightMode) ColorUtils.GREEN else ColorUtils.DARK_GREEN
        if (value <= 100) return ColorUtils.BLUE
        if (value <= 200) return ColorUtils.YELLOW
        if (value <= 300) return ColorUtils.ORANGE
        return ColorUtils.RED
    }
}
