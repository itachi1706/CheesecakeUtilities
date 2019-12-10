package com.itachi1706.cheesecakeutilities.modules.listApplications.recyclerAdapters

import android.content.Context
import android.content.pm.PackageManager
import android.os.AsyncTask
import com.itachi1706.cheesecakeutilities.modules.listApplications.helpers.BackupHelper
import com.itachi1706.cheesecakeutilities.modules.listApplications.objects.RestoreAppsItemsBase
import com.itachi1706.cheesecakeutilities.modules.listApplications.objects.RestoreAppsItemsFooter
import com.itachi1706.cheesecakeutilities.modules.listApplications.objects.RestoreAppsItemsHeader
import com.itachi1706.cheesecakeutilities.util.LogHelper
import org.apache.commons.io.FileUtils
import java.lang.ref.WeakReference

/**
 * Created by Kenneth on 31/7/2019.
 * for com.itachi1706.cheesecakeutilities.modules.ListApplications.RecyclerAdapters in CheesecakeUtilities
 */
class LoadAppsRestoreTask(context: Context, private val callback: LoadAppsCallback) : AsyncTask<Void, Void, Void>() {

    private lateinit var items: HashMap<String, RestoreAppsItemsBase>
    private var finalAdapter: RestoreAppsAdapter? = null
    private val contextRef = WeakReference(context)

    interface LoadAppsCallback {
        fun complete(finalAdapter: RestoreAppsAdapter?)
    }

    override fun doInBackground(vararg params: Void?): Void? {
        val context = contextRef.get() ?: return null
        if (!BackupHelper.createFolder()) {
            LogHelper.e(TAG, "Unable to initialize backup folder. Exiting...")
            return null
        }
        val backupFolder = BackupHelper.getFolder()
        if (!backupFolder.isDirectory) {
            LogHelper.e(TAG, "Invalid Backup Folder. Is not a directory")
            return null
        }

        val ext = arrayOf("apk")
        val apkfiles = FileUtils.listFiles(backupFolder, ext, false)
        val pm = context.packageManager
        items = HashMap()
        for (f in apkfiles) {
            LogHelper.d(TAG, "File: ${f.name}")

            val info = pm.getPackageArchiveInfo(f.absolutePath, PackageManager.GET_META_DATA)
            info.applicationInfo.sourceDir = f.absolutePath
            info.applicationInfo.publicSourceDir = f.absolutePath

            // Group into specifics
            if (!items.containsKey(info.packageName)) {
                // Don't exist, add new record
                val header = RestoreAppsItemsHeader(info.applicationInfo.loadLabel(pm).toString(), info.applicationInfo.loadIcon(pm))
                items.put(info.packageName, header)
            }

            // Add the version
            val header = items.get(info.packageName) as RestoreAppsItemsHeader
            val children: ArrayList<RestoreAppsItemsFooter> = (header.getChild() ?: ArrayList()) as ArrayList<RestoreAppsItemsFooter>
            var alrExist = false
            for (c in children) {
                // Make sure version does not already exist
                if (c.version == info.versionName) {
                    alrExist = true
                    break
                }
            }

            if (!alrExist) {
                val child = RestoreAppsItemsFooter(f.absolutePath, info.versionName)
                children.add(child)
                header.setChild(children)
                items.put(info.packageName, header)
            }
        }

        val tmp = ArrayList(items.values)
        finalAdapter = RestoreAppsAdapter(tmp)
        return null
    }

    override fun onPostExecute(result: Void?) {
        callback.complete(finalAdapter)
    }

    companion object {
        private const val TAG = "RestoreApp"
    }

}