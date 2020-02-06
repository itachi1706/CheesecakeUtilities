package com.itachi1706.cheesecakeutilities.redirectapp.fanfictionReader

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.itachi1706.cheesecakeutilities.R
import com.itachi1706.cheesecakeutilities.redirectapp.InstallAppTask
import com.itachi1706.helperlib.helpers.LogHelper


/**
 * Created by Kenneth on 11/8/2019.
 * for com.itachi1706.cheesecakeutilities in CheesecakeUtilities
 */
class FanfictionReaderRedirectApp : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val data = packageManager.getLaunchIntentForPackage("com.itachi1706.fanfictionnetreader")
        LogHelper.d(TAG, "Has Data: ${data == null}")
        if (data == null) {
            LogHelper.i(TAG, "App Not Installed")
            notInstalled()
            return
        }
        startActivity(data)
        finish()
    }

    private fun notInstalled() {
        // App not installed
        AlertDialog.Builder(this).setTitle("Fanfiction Reader App not installed")
                .setMessage("The Fanfiction Reader app is not installed on this device")
                .setNegativeButton(android.R.string.cancel) { _,_ -> finish() }
                .setPositiveButton(R.string.install) { _, _ ->
                    Toast.makeText(this, "Downloading app... This may take a while...", Toast.LENGTH_LONG).show()
                    InstallAppTask(this, "com.itachi1706.fanfictionnetreader").execute()
                }.show()
    }

    companion object {
        private const val TAG = "FanficReaderRedir"
    }
}