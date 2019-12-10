package com.itachi1706.cheesecakeutilities.modules.listApplications

import android.app.Activity
import android.app.ProgressDialog
import android.os.AsyncTask
import android.widget.Toast
import com.itachi1706.cheesecakeutilities.modules.listApplications.helpers.BackupHelper
import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference

/**
 * Created by Kenneth on 31/7/2019.
 * for com.itachi1706.cheesecakeutilities.modules.ListApplications in CheesecakeUtilities
 */
class BackupAppThread(private val dialog: ProgressDialog, private val shareApk: Boolean = false, activity: Activity) : AsyncTask<String, Void, Void>() {

    val activityRef = WeakReference(activity)

    override fun doInBackground(vararg params: String?): Void? {
        if (params.size < 3) return null
        val appName = params[0]
        val appPath = params[1]
        val filepath = params[2]
        val activity = activityRef.get() ?: return null
        // Init

        try {
            if (!BackupHelper.backupApk(appPath, filepath))
                activity.runOnUiThread {
                    Toast.makeText(activity.applicationContext, "Unable to create folder! Backup failed!", Toast.LENGTH_LONG).show()
                    dialog.dismiss()
                }
            else
                activity.runOnUiThread {
                    Toast.makeText(activity.applicationContext, "Backup of $appName completed", Toast.LENGTH_LONG).show()
                    dialog.dismiss()

                    if (shareApk) {
                        val shareFile = File(BackupHelper.getFolder().absolutePath + "/" + filepath)
                        if (!shareFile.exists()) Toast.makeText(activity.applicationContext, "Unable to share file. File does not exist", Toast.LENGTH_SHORT).show()
                        else BackupHelper.shareFile("ShareApp", activity.applicationContext, shareFile, "Share with", "*/*")
                    }
                }
        } catch (e: IOException) {
            activity.runOnUiThread {
                Toast.makeText(activity.applicationContext, "Error backupping app (${e.localizedMessage})", Toast.LENGTH_LONG).show()
                dialog.dismiss()
            }
            e.printStackTrace()
        }
        return null
    }
}