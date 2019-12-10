package com.itachi1706.cheesecakeutilities.modules.ConnectivityQuietHours.Receivers

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.util.Log
import com.itachi1706.cheesecakeutilities.BaseBroadcastReceiver

/**
 * Created by Kenneth on 1/1/2019.
 * for com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.Receivers in CheesecakeUtilities
 */
class DeleteNotificationIntent : BaseBroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val action = intent.action ?: return
        Log.i(TAG, "Received Notification Removal Intent ($action)")
        when (action) {
            NotificationHelper.NOTIFICATION_SUM_CANCEL -> {
                Log.i(TAG, "Removing all notifications")
                if (NotificationHelper.lines == null) return
                NotificationHelper.lines!!.clear()
                NotificationHelper.lines = null
                NotificationHelper.summaryId = -9999
            }
            NotificationHelper.NOTIFICATION_CANCEL -> {
                if (NotificationHelper.lines == null || NotificationHelper.lines!!.size <= 0) return
                val data = intent.getStringExtra("data")
                Log.i(TAG, "Size: " + NotificationHelper.lines!!.size + " | Removing " + data)
                NotificationHelper.lines!!.remove(intent.getStringExtra("data"))
                Log.i(TAG, "Removed and updating notification. New Size: " + NotificationHelper.lines!!.size)
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                NotificationHelper.createSummaryNotification(context, manager)
            }
        }
    }

    companion object {

        private val TAG = "QuietHour-Notif"
    }
}
