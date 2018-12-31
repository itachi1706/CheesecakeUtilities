package com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.Receivers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.ConnectivityQuietHoursActivity;
import com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.QHConstants;
import com.itachi1706.cheesecakeutilities.R;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

/**
 * Created by Kenneth on 1/1/2019.
 * for com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours in CheesecakeUtilities
 */
public class NotificationHelper {

    private NotificationHelper() {
        throw new IllegalStateException("Utility class. Do not instantiate like this");
    }

    public static ArrayList<String> lines = null;
    public static int summaryId = -9999;
    public static final String NOTIFICATION_SUM_CANCEL = "summary_cancelled", NOTIFICATION_CANCEL = "subitem_cancelled";
    public static final String NOTIFICATION_GROUP = "connectivityqh";

    public static void sendNotification(Context context, int notificationLevel, boolean workDone, boolean state, String connection) {
        String time = DateFormat.getTimeInstance().format(new Date(System.currentTimeMillis()));
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) return;
        QHConstants.createNotificationChannel(notificationManager); // Create the Notification Channel
        String contentTitle = connection + " Quiet Hour " + ((state) ? "Enabled" : "Disabled");
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, QHConstants.QH_NOTIFICATION_CHANNEL);
        mBuilder.setSmallIcon(R.drawable.notification_icon).setContentTitle(contentTitle)
                .setContentText(connection + " state toggled on " + time)
                .setAutoCancel(true)
                .setGroup(NOTIFICATION_GROUP)
                .setDeleteIntent(createDeleteIntent(context, NOTIFICATION_CANCEL, contentTitle))
                .setContentIntent(PendingIntent.getActivity(context, 0, new Intent(context, ConnectivityQuietHoursActivity.class), 0));
        Random random = new Random();
        if (lines == null) lines = new ArrayList<>();
        if (summaryId == -9999) summaryId = random.nextInt();

        switch (notificationLevel) {
            case QHConstants.QH_NOTIFY_ALWAYS:
            case QHConstants.QH_NOTIFY_DEBUG:
                lines.add(contentTitle);
                createSummaryNotification(context, notificationManager);
                notificationManager.notify(random.nextInt(), mBuilder.build()); break;
            case QHConstants.QH_NOTIFY_WHEN_TRIGGERED: if (workDone) {
                lines.add(contentTitle);
                createSummaryNotification(context, notificationManager);
                notificationManager.notify(random.nextInt(), mBuilder.build());
            } break;
            case QHConstants.QH_NOTIFY_NEVER:
            default: break;
        }
    }

    public static void createSummaryNotification(Context context, NotificationManager manager) {
        if (lines == null || lines.size() <= 0) return; // Don't do anything if there is no intent
        NotificationCompat.Builder summaryNotification = new NotificationCompat.Builder(context, QHConstants.QH_NOTIFICATION_CHANNEL);
        NotificationCompat.InboxStyle summaryBuilder = new NotificationCompat.InboxStyle().setSummaryText(lines.size() + " changes toggled").setBigContentTitle(lines.size() + " changes");
        for (String s : lines) {
            summaryBuilder.addLine(s);
        }

        summaryNotification.setDeleteIntent(createDeleteIntent(context, NOTIFICATION_SUM_CANCEL, null)).setSmallIcon(R.drawable.notification_icon)
                .setContentTitle("Quiet Hour Updates").setContentText(lines.size() + " changes").setGroupSummary(true).setGroup(NOTIFICATION_GROUP)
                .setStyle(summaryBuilder);
        manager.notify(summaryId, summaryNotification.build());
    }

    private static PendingIntent createDeleteIntent(Context context, String action, @Nullable String content) {
        Intent del = new Intent(context, DeleteNotificationIntent.class);
        del.setAction(action);
        if (content != null) del.putExtra("data", content);
        Random random = new Random();
        return PendingIntent.getBroadcast(context, random.nextInt(5000), del, PendingIntent.FLAG_CANCEL_CURRENT);
    }
}
