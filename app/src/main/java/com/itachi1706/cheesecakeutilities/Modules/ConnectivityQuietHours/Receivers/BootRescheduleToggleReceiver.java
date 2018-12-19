package com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.Receivers;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.itachi1706.appupdater.Util.PrefHelper;
import com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.ConnectivityQuietHoursActivity;
import com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.Objects.ConnectivityPeriod;
import com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.QHConstants;
import com.itachi1706.cheesecakeutilities.R;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import androidx.core.app.NotificationCompat;

import static com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.QHConstants.BT_END_INTENT;
import static com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.QHConstants.BT_START_INTENT;
import static com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.QHConstants.QH_BT_NOTIFICATION;
import static com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.QHConstants.QH_BT_TIME;
import static com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.QHConstants.QH_NOTIFICATION_CHANNEL;
import static com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.QHConstants.QH_WIFI_NOTIFICATION;
import static com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.QHConstants.QH_WIFI_TIME;
import static com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.QHConstants.WIFI_END_INTENT;
import static com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.QHConstants.WIFI_START_INTENT;

public class BootRescheduleToggleReceiver extends BroadcastReceiver {

    public static final String TAG = "QuietHour-Boot";
    private SharedPreferences sp;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) return; // Not Boot Action
        sp = PrefHelper.getDefaultSharedPreferences(context);
        if (sp.getBoolean(QHConstants.QH_BT_STATE, false))
            scheduleConnectivity(context, "BT", BT_START_INTENT, BT_END_INTENT, QH_BT_TIME, QH_BT_NOTIFICATION);
        if (sp.getBoolean(QHConstants.QH_WIFI_STATE, false))
            scheduleConnectivity(context, "Wifi", WIFI_START_INTENT, WIFI_END_INTENT, QH_WIFI_TIME, QH_WIFI_NOTIFICATION);

        Log.i(TAG, "Job Done");
    }

    // Presume enabled, do check first
    private void scheduleConnectivity(Context context, String name, int startIntent, int endIntent, String timePref, String notification) {
        Log.i(TAG, "Start schedule of " + name);
        String defConn = sp.getString(timePref, "");
        if (defConn.isEmpty()) return; // Cannot schedule dude
        ConnectivityPeriod connectivityPeriod = new ConnectivityPeriod(defConn);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent connSI = new Intent(context, BluetoothToggleReceiver.class).putExtra("status", true);
        Intent connEI = new Intent(context, BluetoothToggleReceiver.class).putExtra("status", false);
        PendingIntent connStartIntent = PendingIntent.getBroadcast(context, startIntent, connSI, 0);
        PendingIntent connEndIntent = PendingIntent.getBroadcast(context, endIntent, connEI, 0);
        // Cancel all possible pending intents
        alarmManager.cancel(connStartIntent);
        alarmManager.cancel(connEndIntent);
        Log.i(TAG, "Cleared existing " + name + " Schedules");

        // Set Alarm
        long millis = System.currentTimeMillis();
        Calendar startCal = Calendar.getInstance();
        startCal.setTimeInMillis(millis);
        startCal.set(Calendar.HOUR_OF_DAY, connectivityPeriod.getStartHr());
        startCal.set(Calendar.MINUTE, connectivityPeriod.getStartMin());
        startCal.set(Calendar.SECOND, 0);

        Calendar endCal = Calendar.getInstance();
        endCal.setTimeInMillis(millis);
        endCal.set(Calendar.HOUR_OF_DAY, connectivityPeriod.getEndHr());
        endCal.set(Calendar.MINUTE, connectivityPeriod.getEndMin());
        endCal.set(Calendar.SECOND, 0);

        boolean started = false, ended = false;

        if (millis > startCal.getTimeInMillis()) {
            startCal.add(Calendar.DAY_OF_YEAR, 1);
            started = true;
        }
        if (millis > endCal.getTimeInMillis()) {
            endCal.add(Calendar.DAY_OF_YEAR, 1);
            ended = true;
        }

        // Do some logic in case we need to toggle shit
        if ((started && ended) || (!started && ended)) processNotification(context, connEI, name, "End", notification);
        else if (started) processNotification(context, connSI, name, "Start", notification);

        if (startCal.getTimeInMillis() == endCal.getTimeInMillis()) {
            Log.d(TAG, "Same Time Found. Not doing anything for " + name + " Scheduling");
            return;
        }

        Log.i(TAG, name + " Start Scheduled at " + DateFormat.getDateTimeInstance().format(startCal.getTimeInMillis()));
        Log.i(TAG, name + " End Scheduled at " + DateFormat.getDateTimeInstance().format(endCal.getTimeInMillis()));

        // Update Alarms
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, startCal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, connStartIntent);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, endCal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, connEndIntent);

        sendNotification(context, sp.getInt(notification, QHConstants.QH_NOTIFY_NEVER), name + " QH Enabled", false, startCal.getTimeInMillis());
        sendNotification(context, sp.getInt(notification, QHConstants.QH_NOTIFY_NEVER), name + " QH Disabled", false, endCal.getTimeInMillis());

        Log.i(TAG, "Scheduled " + name + " Quiet Hours after boot");
    }

    private void processNotification(Context context, Intent connection, String name, String status, String notification) {
        // Presume started/ended
        context.sendBroadcast(connection);
        Log.i(TAG, "Fired " + name + " QH " + status + " Intent");
        sendNotification(context, sp.getInt(notification, QHConstants.QH_NOTIFY_NEVER), name + " QH " + status, true, -1);
    }

    private void sendNotification(Context context, int notificationLevel, String state, boolean prefire, long newtime) {
        String time = DateFormat.getTimeInstance().format(new Date(System.currentTimeMillis()));
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        QHConstants.createNotificationChannel(notificationManager); // Create the Notification Channel
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, QH_NOTIFICATION_CHANNEL);
        mBuilder.setSmallIcon(R.drawable.notification_icon).setContentTitle("Boot Quiet Hour Scheduling")
                .setAutoCancel(true)
                .setGroup("connectivityqh")
                .setContentIntent(PendingIntent.getActivity(context, 0, new Intent(context, ConnectivityQuietHoursActivity.class), 0));
        if (prefire) {
            mBuilder.setContentText("Pre-Fired " + state + " trigger on " + time);
        } else {

            mBuilder.setContentText("Rescheduled " + state + " trigger on " + time)
            .setStyle(new NotificationCompat.BigTextStyle().bigText("Rescheduled " + state + " trigger on " +
                    time + " to " + DateFormat.getDateTimeInstance().format(newtime)));
        }
        Random random = new Random();

        switch (notificationLevel) {
            case QHConstants.QH_NOTIFY_DEBUG:
                notificationManager.notify(random.nextInt(), mBuilder.build());
                break;
            default:
                break;
        }
    }
}
