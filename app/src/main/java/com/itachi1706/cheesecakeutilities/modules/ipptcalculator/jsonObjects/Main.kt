package com.itachi1706.cheesecakeutilities.modules.ipptcalculator.jsonObjects

import com.google.gson.JsonObject

/**
 * Created by Kenneth on 7/9/2016.
 * for com.itachi1706.cheesecakeutilities.modules.IPPTCalculator.JsonObjects in CheesecakeUtilities
 */
data class Main (val ageRangeText: JsonObject? = null, val pass: JsonObject? = null, val dataMale: Gender = Gender(), val dataFemale: Gender = Gender())
