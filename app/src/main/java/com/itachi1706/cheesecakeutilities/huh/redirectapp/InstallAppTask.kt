package com.itachi1706.cheesecakeutilities.huh.redirectapp

import android.app.Activity
import android.content.Intent
import android.os.AsyncTask
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.itachi1706.appupdater.Util.URLHelper
import com.itachi1706.cheesecakeutilities.huh.util.CommonVariables
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.URL

/**
 * Created by Kenneth on 11/8/2019.
 * for com.itachi1706.cheesecakeutilities.Modules.FanfictionCompactor in CheesecakeUtilities
 */
class InstallAppTask(activity: Activity) : AsyncTask<Void, Void, File>() {

    private val activityRef = WeakReference(activity)

    override fun doInBackground(vararg params: Void?): File? {
        val activity = activityRef.get() ?: return null
        val url = "${CommonVariables.BASE_API_URL}appupdatechecker.php?action=androidgetlatesturl&packagename=com.itachi1706.fanfictionnetreader"
        val urlHelper = URLHelper(url)
        Log.d(TAG, "Querying $url")
        val data = urlHelper.executeString()
        Log.d(TAG, "Data Obj: $data")

        val gson = Gson()
        val obj = gson.fromJson(data, RedirectAppUrlObject::class.java)
        if (obj.error != 21) {
            Log.e(TAG, "Error occurred retrieving URL for app")
            return null
        }

        val urlLink = obj.msg.url
        if (urlLink.isEmpty()) {
            Log.e(TAG, "No URL")
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
            Log.d(TAG, "Starting Download to cache...")
            val cacheFolder = File(activity.externalCacheDir, "redirect")
            cacheFolder.mkdirs()
            val downloadFile = File(cacheFolder, "redirect.apk")
            if (downloadFile.exists()) downloadFile.delete()

            Log.i(TAG, "Downloading to ${downloadFile.absolutePath}")
            val fos = FileOutputStream(downloadFile)
            Log.d(TAG, "Connection done, File Obtained")
            Log.d(TAG, "Writing to file")
            val inputStream = conn.inputStream
            inputStream.copyTo(fos, 1024)
            fos.close()
            inputStream.close()
            Log.d(TAG, "Download Complete...")
            return downloadFile
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    override fun onPostExecute(result: File?) {
        val activity = activityRef.get() ?: return
        if (result == null) {
            Toast.makeText(activity, "An error occurred installing app", Toast.LENGTH_LONG).show()
        } else {
            Log.i(TAG, "Retrieved file ${result.absolutePath}. Launching installer")
            val intent = Intent(Intent.ACTION_INSTALL_PACKAGE)
            val contentUri = FileProvider.getUriForFile(activity.baseContext, activity.applicationContext.packageName + ".provider", result)
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