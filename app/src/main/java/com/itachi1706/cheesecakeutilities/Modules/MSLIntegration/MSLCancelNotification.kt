package com.itachi1706.cheesecakeutilities.Modules.MSLIntegration

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class MSLCancelNotification : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Cancels the notification
        Log.d("MSLNotification", "Force Cancelling Notification")
        if (!intent.hasExtra("id")) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(intent.getIntExtra("id", 0));
    }
}
