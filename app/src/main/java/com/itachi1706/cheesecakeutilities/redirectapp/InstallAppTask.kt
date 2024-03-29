package com.itachi1706.cheesecakeutilities.redirectapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.widget.Toast
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.itachi1706.cheesecakeutilities.util.CommonVariables
import com.itachi1706.helperlib.helpers.LogHelper
import com.itachi1706.helperlib.helpers.URLHelper
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.URL

/**
 * Created by Kenneth on 11/8/2019.
 * for com.itachi1706.cheesecakeutilities.modules.FanfictionCompactor in CheesecakeUtilities
 */
class InstallAppTask(activity: Activity, val packageName: String) : AsyncTask<Void, Void, File>() {

    private val activityRef = WeakReference(activity)

    @Deprecated("Deprecated in Java")
    override fun doInBackground(vararg params: Void?): File? {
        val activity = activityRef.get() ?: return null
        val url = "${CommonVariables.BASE_API_URL}appupdatechecker.php?action=androidgetlatesturl&packagename=$packageName"
        val urlHelper = URLHelper(url)
        LogHelper.d(TAG, "Querying $url")
        val data = urlHelper.executeString()
        LogHelper.d(TAG, "Data Obj: $data")

        val gson = Gson()
        val obj = gson.fromJson(data, RedirectAppUrlObject::class.java)
        if (obj.error != 21) {
            LogHelper.e(TAG, "Error occurred retrieving URL for app")
            return null
        }

        val urlLink = obj.msg.url
        if (urlLink.isEmpty()) {
            LogHelper.e(TAG, "No URL")
            return null
        }

        // Continue to get the URL and download the file
        try {
            val downloadUrl = URL(urlLink)
            val conn = downloadUrl.openConnection() as HttpURLConnection
            conn.connectTimeout = 60000
            conn.readTimeout = 60000
            conn.requestMethod = "GET"
            conn.connect()
            LogHelper.d(TAG, "Starting Download to cache...")
            val cacheFolder = File(activity.externalCacheDir, "redirect")
            cacheFolder.mkdirs()
            val downloadFile = File(cacheFolder, "redirect.apk")
            if (downloadFile.exists()) downloadFile.delete()

            LogHelper.i(TAG, "Downloading to ${downloadFile.absolutePath}")
            val fos = FileOutputStream(downloadFile)
            LogHelper.d(TAG, "Connection done, File Obtained")
            LogHelper.d(TAG, "Writing to file")
            val inputStream = conn.inputStream
            inputStream.copyTo(fos, 1024)
            fos.close()
            inputStream.close()
            LogHelper.d(TAG, "Download Complete...")
            return downloadFile
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onPostExecute(result: File?) {
        val activity = activityRef.get() ?: return
        if (result == null) {
            Toast.makeText(activity, "An error occurred installing app", Toast.LENGTH_LONG).show()
        } else {
            LogHelper.i(TAG, "Retrieved file ${result.absolutePath}. Launching installer")
            val intent = Intent(Intent.ACTION_INSTALL_PACKAGE)
            val postNougat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
            val contentUri = if (postNougat) FileProvider.getUriForFile(activity.baseContext, activity.applicationContext.packageName + ".provider", result) else Uri.fromFile(result)
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            activity.startActivity(intent)
        }
        activity.finish()
    }

    companion object {
        private const val TAG = "InstallAppTask"
    }
}