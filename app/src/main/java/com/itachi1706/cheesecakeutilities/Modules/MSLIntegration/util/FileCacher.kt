package com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.util

import android.content.Context
import android.util.Log
import androidx.annotation.Nullable
import java.io.*

/**
 * Created by Kenneth on 16/1/2019.
 * for com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.util in CheesecakeUtilities
 */
class FileCacher(private val mContext: Context) {

    private val directory: File
        get() = File(mContext.cacheDir, "msl")

    init {
        init()
    }

    private val fileName = "msl-data"

    private fun init() {
        val directory = directory
        if (!directory.exists() || directory.exists() && directory.isFile && directory.delete()) directory.mkdirs()
    }

    private fun hasFile(): Boolean {
        // Check for cached file as well as if it has expired or not
        val f = getFileObject()
        if (!f.exists()) return false

        Log.i(TAG, "Found $fileName")
        return true
    }

    private fun getFileObject(): File {
        return File(directory.absolutePath + "/" + fileName + ".json")
    }

    @Nullable
    private fun getFile(): File? {
        return if (hasFile()) {
            getFileObject()
        } else null
    }

    fun writeToFile(fileData: String): Boolean {
        val f = getFileObject()
        if (f.exists() && !f.delete()) {
            Log.e(TAG, "Unable to remove old file, exiting")
            return false
        }

        try {
            val fos = FileOutputStream(f)
            fos.write(fileData.toByteArray())
            fos.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            Log.e(TAG, "File not found. Something went wrong!")
            return false
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e(TAG, "Error writing file. Exiting prematurely")
            return false
        }
        Log.i(TAG, "Wrote to $fileName")
        return true
    }

    fun deleteFile(): Boolean {
        val f = getFile() ?: return true
        return f.delete()
    }

    @Nullable
    fun getStringFromFile(): String? {
        val f = getFile() ?: return null

        // Read text file and return it
        val sb = StringBuilder()
        try {
            val fis = FileInputStream(f)
            val br = BufferedReader(InputStreamReader(fis))
            for (line in br.readLine()) {
                sb.append(line)
            }
            br.close()
            fis.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            Log.e(TAG, "Cannot parse file, assuming corrupted")
            return null
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e(TAG, "Cannot parse file, assuming corrupted")
            return null
        }

        Log.i(TAG, "Loaded $fileName")
        return sb.toString()
    }

    companion object {
        private const val TAG = "MSL-FILE-CACHE"
    }
}
