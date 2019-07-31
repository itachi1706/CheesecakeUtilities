package com.itachi1706.cheesecakeutilities.Modules.ListApplications

import android.app.Activity
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.AsyncTask
import androidx.collection.ArrayMap
import com.google.firebase.perf.FirebasePerformance
import com.itachi1706.cheesecakeutilities.Modules.ListApplications.Objects.AppsItem
import com.itachi1706.cheesecakeutilities.Modules.ListApplications.RecyclerAdapters.AppsAdapter
import com.itachi1706.cheesecakeutilities.Util.LogHelper
import java.lang.ref.WeakReference

/**
 * Created by Kenneth on 31/7/2019.
 * for com.itachi1706.cheesecakeutilities.Modules.ListApplications in CheesecakeUtilities
 */
class LoadAppListTask(activity: Activity, private val callback: LoadAppCallback, private val sortByApi: Boolean = false) : AsyncTask<Boolean, Void, Void>() {

    private val activityRef = WeakReference(activity)
    private lateinit var finalStr: ArrayList<AppsItem>

    interface LoadAppCallback {
        fun complete(appCount: String = "", appPackageNameInstall: List<String>, appPackageNameClean: List<String>, finalAdapter: AppsAdapter)
    }

    override fun doInBackground(vararg params: Boolean?): Void? {
        if (params.isEmpty()) return null
        val system = params[0]!!
        val activity = activityRef.get()

        if (activity == null) {
            LogHelper.e(TAG, "An error occurred loading app list")
            return null
        }

        val pm = activity.packageManager
        val pkgAppsList = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        finalStr = ArrayList()
        val appPackageNameCleaned = ArrayList<String>()
        val appPackageNamesInstalled = ArrayList<String>()
        val appTrace = FirebasePerformance.getInstance().newTrace("load_app_list")
        appTrace.start()
        appTrace.putMetric("app_item_count", pkgAppsList.size.toLong())
        for (i in pkgAppsList) {
            appPackageNamesInstalled.add(i.packageName)
            if (isSystemApp(i) && !system) continue
            appTrace.incrementMetric("app_item_count_actual", 1)
            appPackageNameCleaned.add(i.packageName)
            var version = "Unknown"
            try {
                val pInfo = activity.packageManager.getPackageInfo(i.packageName, 0)
                version = pInfo.versionName
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            } catch (e: NullPointerException) {
                e.printStackTrace()
                LogHelper.e(TAG, "Null Pointer Encounted in PInfoQuery (${e.localizedMessage})")
            }

            val item = AppsItem(activity)
            item.apiVersion = i.targetSdkVersion
            item.appName = i.loadLabel(pm).toString()
            item.packageName = i.packageName
            item.icon = i.loadIcon(pm)
            item.version = version
            finalStr.add(item)
        }
        appTrace.stop()

        val finalAdapter = AppsAdapter(finalStr)
        finalAdapter.sort()
        if (sortByApi) finalAdapter.sort(AppsAdapter.SORT_API)

        val appCountString = generateApiAppCountList()
        callback.complete(appCountString, appPackageNamesInstalled, appPackageNameCleaned, finalAdapter)
        return null
    }

    private fun generateApiAppCountList(): String {
        val tmp: ArrayMap<Int, Int> = ArrayMap()
        for (appItem in finalStr) {
            var count = tmp[appItem.apiVersion] ?: 0
            //if (tmp.containsKey(appItem.apiVersion)) count = tmp[appItem.apiVersion] ?: 0
            count++
            tmp[appItem.apiVersion] = count
        }

        val appCount = StringBuilder()
        for (obj in tmp.entries) {
            appCount.append(obj.key).append(":").append(obj.value).append("-")
        }
        return appCount.substring(0, appCount.length - 1)
    }

    private fun isSystemApp(i: ApplicationInfo): Boolean {
        return i.flags and ApplicationInfo.FLAG_SYSTEM == ApplicationInfo.FLAG_SYSTEM
    }

    companion object {
        private const val TAG = "LoadApp"
    }
}