package com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.itachi1706.cheesecakeutilities.R;

import java.text.DateFormat;
import java.util.Date;
import java.util.Random;

import androidx.core.app.NotificationCompat;

/**
 * Created by Kenneth on 1/1/2019.
 * for com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours in CheesecakeUtilities
 */
public class NotificationHelper {

    private NotificationHelper() {
        throw new IllegalStateException("Utility class. Do not instantiate like this");
    }

    public static void sendNotification(Context context, int notificationLevel, boolean workDone, boolean state, String connection) {
        String time = DateFormat.getTimeInstance().format(new Date(System.currentTimeMillis()));
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) return;
        QHConstants.createNotificationChannel(notificationManager); // Create the Notification Channel
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, QHConstants.QH_NOTIFICATION_CHANNEL);
        mBuilder.setSmallIcon(R.drawable.notification_icon).setContentTitle(connection + "Quiet Hour " + ((state) ? "Enabled" : "Disabled"))
                .setContentText(connection + "state toggled on " + time)
                .setAutoCancel(true)
                .setGroup("connectivityqh")
                .setGroupSummary(true)
                .setContentIntent(PendingIntent.getActivity(context, 0, new Intent(context, ConnectivityQuietHoursActivity.class), 0));
        Random random = new Random();

        switch (notificationLevel) {
            case QHConstants.QH_NOTIFY_ALWAYS:
            case QHConstants.QH_NOTIFY_DEBUG:
                notificationManager.notify(random.nextInt(), mBuilder.build()); break;
            case QHConstants.QH_NOTIFY_WHEN_TRIGGERED: if (workDone) notificationManager.notify(random.nextInt(), mBuilder.build()); break;
            case QHConstants.QH_NOTIFY_NEVER:
            default: break;
        }
    }
}
