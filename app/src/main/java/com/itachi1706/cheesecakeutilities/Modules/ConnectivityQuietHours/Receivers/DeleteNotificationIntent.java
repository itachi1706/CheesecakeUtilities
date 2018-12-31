package com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.Receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Kenneth on 1/1/2019.
 * for com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.Receivers in CheesecakeUtilities
 */
public class DeleteNotificationIntent extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) return;
        if (action.equalsIgnoreCase(NotificationHelper.NOTIFICATION_SUM_CANCEL)) {
            NotificationHelper.lines.clear();
            NotificationHelper.lines = null;
            NotificationHelper.summaryId = -9999;
        } else if (action.equalsIgnoreCase(NotificationHelper.NOTIFICATION_CANCEL)) {
            if (NotificationHelper.lines == null || NotificationHelper.lines.size() <= 0) return;
            NotificationHelper.lines.remove(intent.getStringExtra("data"));
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager == null) return;
            NotificationHelper.createSummaryNotification(context, manager);
        }
    }
}
