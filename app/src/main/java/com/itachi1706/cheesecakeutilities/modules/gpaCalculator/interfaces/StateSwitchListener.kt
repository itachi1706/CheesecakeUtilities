package com.itachi1706.cheesecakeutilities.modules.gpaCalculator.interfaces

import com.google.firebase.database.DatabaseReference
import com.itachi1706.cheesecakeutilities.modules.gpaCalculator.objects.GpaInstitution
import com.itachi1706.cheesecakeutilities.modules.gpaCalculator.objects.GpaScoring
import com.itachi1706.cheesecakeutilities.modules.gpaCalculator.objects.GpaSemester

/**
 * Created by Kenneth on 26/6/2019.
 * for com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.`interface` in CheesecakeUtilities
 */
interface StateSwitchListener {
    fun onStateSwitch(newState: Int)
    fun getUserData(): DatabaseReference
    fun getUserId(): String
    fun getInstitution(): GpaInstitution?
    fun getSemester(): GpaSemester?
    fun selectInstitute(instituteSelected: GpaInstitution)
    fun selectSemester(semester: GpaSemester, key: String)
    fun updateActionBar(title: String?, subtitle: String?)
    fun getScoreMap(): HashMap<String, GpaScoring>
    fun updateScoreMap(callback: GpaCalcCallback)
    fun getCurrentState(): Int
    fun updateSelectedInstitution()
    fun goBack()
}