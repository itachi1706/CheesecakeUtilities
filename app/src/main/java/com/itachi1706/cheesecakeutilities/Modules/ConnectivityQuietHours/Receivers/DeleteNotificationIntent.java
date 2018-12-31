package com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.Receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Kenneth on 1/1/2019.
 * for com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.Receivers in CheesecakeUtilities
 */
public class DeleteNotificationIntent extends BroadcastReceiver {

    private static final String TAG = "QuietHour-Notif";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) return;
        Log.i(TAG, "Received Notification Removal Intent (" + action + ")");
        if (action.equalsIgnoreCase(NotificationHelper.NOTIFICATION_SUM_CANCEL)) {
            Log.i(TAG, "Removing all notifications");
            NotificationHelper.lines.clear();
            NotificationHelper.lines = null;
            NotificationHelper.summaryId = -9999;
        } else if (action.equalsIgnoreCase(NotificationHelper.NOTIFICATION_CANCEL)) {
            if (NotificationHelper.lines == null || NotificationHelper.lines.size() <= 0) return;
            String data = intent.getStringExtra("data");
            Log.i(TAG, "Size: " + NotificationHelper.lines.size() + " | Removing " + data);
            NotificationHelper.lines.remove(intent.getStringExtra("data"));
            Log.i(TAG, "Removed and updating notification. New Size: " + NotificationHelper.lines.size());
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager == null) return;
            NotificationHelper.createSummaryNotification(context, manager);
        }
    }
}
