package com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.Receivers

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.Nullable
import androidx.core.app.NotificationCompat
import com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.ConnectivityQuietHoursActivity
import com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.QHConstants
import com.itachi1706.cheesecakeutilities.R
import java.text.DateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Kenneth on 1/1/2019.
 * for com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours in CheesecakeUtilities
 */
internal class NotificationHelper private constructor() {

    init {
        throw IllegalStateException("Utility class. Do not instantiate like this")
    }

    companion object {
        var lines: ArrayList<String>? = null
        var summaryId = -9999
        const val NOTIFICATION_SUM_CANCEL = "summary_cancelled"
        const val NOTIFICATION_CANCEL = "subitem_cancelled"
        const val NOTIFICATION_GROUP = "connectivityqh"

        fun sendNotification(context: Context, notificationLevel: Int, workDone: Boolean, state: Boolean, connection: String) {
            val time = DateFormat.getTimeInstance().format(Date(System.currentTimeMillis()))
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            QHConstants.createNotificationChannel(notificationManager) // Create the Notification Channel
            val contentTitle = connection + " Quiet Hour " + if (state) "Enabled" else "Disabled"
            val mBuilder = NotificationCompat.Builder(context, QHConstants.QH_NOTIFICATION_CHANNEL)
            mBuilder.setSmallIcon(R.drawable.notification_icon).setContentTitle(contentTitle)
                    .setContentText("$connection state toggled on $time")
                    .setAutoCancel(true)
                    .setGroup(NOTIFICATION_GROUP)
                    .setDeleteIntent(createDeleteIntent(context, NOTIFICATION_CANCEL, contentTitle))
                    .setContentIntent(PendingIntent.getActivity(context, 0, Intent(context, ConnectivityQuietHoursActivity::class.java), 0))
            val random = Random()
            if (lines == null) lines = ArrayList()
            if (summaryId == -9999) summaryId = random.nextInt()

            when (notificationLevel) {
                QHConstants.QH_NOTIFY_ALWAYS, QHConstants.QH_NOTIFY_DEBUG -> {
                    lines!!.add(contentTitle)
                    createSummaryNotification(context, notificationManager)
                    notificationManager.notify(random.nextInt(), mBuilder.build())
                }
                QHConstants.QH_NOTIFY_WHEN_TRIGGERED -> if (workDone) {
                    lines!!.add(contentTitle)
                    createSummaryNotification(context, notificationManager)
                    notificationManager.notify(random.nextInt(), mBuilder.build())
                }
            }
        }

        fun createSummaryNotification(context: Context, manager: NotificationManager) {
            if (lines == null || lines!!.size <= 0) return  // Don't do anything if there is no intent
            val summaryNotification = NotificationCompat.Builder(context, QHConstants.QH_NOTIFICATION_CHANNEL)
            val summaryBuilder = NotificationCompat.InboxStyle().setSummaryText(lines!!.size.toString() + " changes toggled").setBigContentTitle(lines!!.size.toString() + " changes")
            for (s in lines!!) {
                summaryBuilder.addLine(s)
            }

            summaryNotification.setDeleteIntent(createDeleteIntent(context, NOTIFICATION_SUM_CANCEL, null)).setSmallIcon(R.drawable.notification_icon)
                    .setContentTitle("Quiet Hour Updates").setContentText(lines!!.size.toString() + " changes").setGroupSummary(true).setGroup(NOTIFICATION_GROUP)
                    .setStyle(summaryBuilder)
            manager.notify(summaryId, summaryNotification.build())
        }

        fun addToLines(text: String) {
            if (lines == null) lines = ArrayList()
            lines!!.add(text)
        }

        fun createDeleteIntent(context: Context, action: String, @Nullable content: String?): PendingIntent {
            val del = Intent(context, DeleteNotificationIntent::class.java)
            del.action = action
            if (content != null) del.putExtra("data", content)
            val random = Random()
            return PendingIntent.getBroadcast(context, random.nextInt(5000), del, PendingIntent.FLAG_CANCEL_CURRENT)
        }
    }
}
