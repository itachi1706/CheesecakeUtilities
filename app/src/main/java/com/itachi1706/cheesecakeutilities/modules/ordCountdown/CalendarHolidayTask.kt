package com.itachi1706.cheesecakeutilities.modules.ordCountdown

import android.os.AsyncTask
import com.itachi1706.appupdater.Util.PrefHelper
import com.itachi1706.appupdater.Util.URLHelper
import java.io.IOException
import java.lang.ref.WeakReference

/**
 * Created by Kenneth on 31/7/2019.
 * for com.itachi1706.cheesecakeutilities.Modules.ORDCountdown in CheesecakeUtilities
 */
class CalendarHolidayTask(activity: ORDActivity, private val callback: CalendarHolidayCallback) : AsyncTask<Void, Void, Void>() {

    private val activityRef: WeakReference<ORDActivity> = WeakReference(activity)

    interface CalendarHolidayCallback {
        fun callback()
    }

    override fun doInBackground(vararg params: Void?): Void? {
        val activity = activityRef.get() ?: return null
        val url = "https://api.itachi1706.com/api/gcal_sg_holidays.php"
        val urlHelper = URLHelper(url)
        try {
            val tmp = urlHelper.executeString()
            val sp = PrefHelper.getDefaultSharedPreferences(activity.applicationContext)
            sp.edit().putString(ORDActivity.ORD_HOLIDAY_PREF, tmp).apply()
            activity.runOnUiThread{callback.callback()}
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }
}