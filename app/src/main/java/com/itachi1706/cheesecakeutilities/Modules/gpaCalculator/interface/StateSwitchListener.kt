package com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.`interface`

import com.google.firebase.database.DatabaseReference

/**
 * Created by Kenneth on 26/6/2019.
 * for com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.`interface` in CheesecakeUtilities
 */
interface StateSwitchListener {
    fun onStateSwitch(newState: Int)
    fun getState(): Int
    fun getUserData(): DatabaseReference
}