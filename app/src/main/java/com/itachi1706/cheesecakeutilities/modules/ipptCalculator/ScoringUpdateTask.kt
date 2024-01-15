package com.itachi1706.cheesecakeutilities.modules.ipptCalculator

import android.app.Activity
import android.os.AsyncTask
import com.itachi1706.cheesecakeutilities.modules.ipptCalculator.helpers.JsonHelper
import java.lang.ref.WeakReference

/**
 * Created by Kenneth on 31/7/2019.
 * for com.itachi1706.cheesecakeutilities.modules.IPPTCalculator in CheesecakeUtilities
 */
class ScoringUpdateTask(activity: Activity, private var callback: ScoringCallback) : AsyncTask<Int, Void, Void>() {

    private val activityRef = WeakReference(activity)
    private var results: List<String> = ArrayList()

    interface ScoringCallback {
        fun updateResults(results: List<String>)
    }

    @Deprecated("Deprecated in Java")
    override fun doInBackground(vararg params: Int?): Void? {
        if (params.size < 3) return null
        val ageGroup = params[0]!!
        val gender = params[1]!!
        val exercise = params[2]!!
        val activity = activityRef.get() ?: return null

        results = JsonHelper.getExerciseScores(ageGroup, exercise, gender, activity.applicationContext)
        activity.runOnUiThread {
            callback.updateResults(results)
        }
        return null
    }

}