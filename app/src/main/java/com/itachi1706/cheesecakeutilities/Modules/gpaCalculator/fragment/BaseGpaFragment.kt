package com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.fragment

import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import com.google.firebase.database.ValueEventListener
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.GpaCalcFirebaseUtils
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.MainViewActivity
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.interfaces.StateSwitchListener
import com.itachi1706.cheesecakeutilities.Util.FirebaseUtils
import com.itachi1706.cheesecakeutilities.Util.LogHelper
import java.util.*

/**
 * Created by Kenneth on 16/7/2019.
 * for com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.fragment in CheesecakeUtilities
 */
abstract class BaseGpaFragment : Fragment() {
    var callback: StateSwitchListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainViewActivity) {
            callback = context
        }
    }

    var listener: ValueEventListener? = null
    override fun onStart() {
        super.onStart()
        if (listener != null) {
            FirebaseUtils.removeListener(listener!!)
            listener = null
            LogHelper.e(getLogTag(), "Firebase DB Listeners exists when it should not have, terminating it forcibly")
        }
    }

    override fun onStop() {
        super.onStop()
        if (listener != null) {
            FirebaseUtils.removeListener(listener!!)
            Log.i(getLogTag(), "Firebase Listener Unregisted")
            listener = null
        }
    }

    abstract fun getLogTag(): String

    fun getTimestampString(startTimestamp: Long, endTimestamp: Long): String {
        val calendar = Calendar.getInstance()
        val dateFormat = GpaCalcFirebaseUtils.DATE_FORMAT
        calendar.timeInMillis = startTimestamp
        var timestamp = "${dateFormat.format(calendar.time)} - "
        if (endTimestamp == (-1).toLong()) timestamp += "Present"
        else {
            calendar.timeInMillis = endTimestamp
            timestamp += dateFormat.format(calendar.time)
        }
        return timestamp
    }
}