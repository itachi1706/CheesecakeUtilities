package com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.Receivers;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.ConnectivityQuietHoursActivity;
import com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.Objects.ConnectivityPeriod;
import com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.QHConstants;
import com.itachi1706.cheesecakeutilities.R;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import static com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.QHConstants.BT_END_INTENT;
import static com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.QHConstants.BT_START_INTENT;
import static com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.QHConstants.QH_BT_TIME;
import static com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.QHConstants.QH_WIFI_TIME;
import static com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.QHConstants.WIFI_END_INTENT;
import static com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.QHConstants.WIFI_START_INTENT;

public class BootRescheduleToggleReceiver extends BroadcastReceiver {

    public static final String TAG = "QuietHour-Boot";
    private SharedPreferences sp;

    @Override
    public void onReceive(Context context, Intent intent) {
        sp = PreferenceManager.getDefaultSharedPreferences(context);
        if (sp.getBoolean(QHConstants.QH_BT_STATE, false)) scheduleBt(context);
        if (sp.getBoolean(QHConstants.QH_WIFI_STATE, false)) scheduleWifi(context);

        Log.i(TAG, "Job Done");
    }

    // Presume Enabled. Do Check first
    private void scheduleBt(Context context) {
        Log.i(TAG, "Start schedule of BT");
        String defBt = sp.getString(QH_BT_TIME, "");
        if (defBt.isEmpty()) return; // Cannot schedule dude
        ConnectivityPeriod btConnectivity = new ConnectivityPeriod(defBt);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent btSI = new Intent(context, BluetoothToggleReceiver.class).putExtra("status", true);
        Intent btEI = new Intent(context, BluetoothToggleReceiver.class).putExtra("status", false);
        PendingIntent btStartIntent = PendingIntent.getBroadcast(context, BT_START_INTENT, btSI, 0);
        PendingIntent btEndIntent = PendingIntent.getBroadcast(context, BT_END_INTENT, btEI, 0);
        // Cancel all possible pending intents
        alarmManager.cancel(btStartIntent);
        alarmManager.cancel(btEndIntent);
        Log.i(TAG, "Cleared existing BT Schedules");

        // Set Alarm
        long millis = System.currentTimeMillis();
        Calendar startCal = Calendar.getInstance();
        startCal.setTimeInMillis(millis);
        startCal.set(Calendar.HOUR_OF_DAY, btConnectivity.getStartHr());
        startCal.set(Calendar.MINUTE, btConnectivity.getStartMin());
        startCal.set(Calendar.SECOND, 0);

        Calendar endCal = Calendar.getInstance();
        endCal.setTimeInMillis(millis);
        endCal.set(Calendar.HOUR_OF_DAY, btConnectivity.getEndHr());
        endCal.set(Calendar.MINUTE, btConnectivity.getEndMin());
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
        if ((started && ended) || (!started && ended)) {
            // Presume ended
            context.sendBroadcast(btEI);
            Log.i(TAG, "Fired BT QH End Intent");
            sendNotification(context, sp.getInt(QHConstants.QH_WIFI_NOTIFICATION, QHConstants.QH_NOTIFY_NEVER), "BT QH End", true, -1);
        } else if (started) {
            // Presume started
            context.sendBroadcast(btSI);
            Log.i(TAG, "Fired BT QH Start Intent");
            sendNotification(context, sp.getInt(QHConstants.QH_WIFI_NOTIFICATION, QHConstants.QH_NOTIFY_NEVER), "BT QH End", true, -1);
        }

        if (startCal.getTimeInMillis() == endCal.getTimeInMillis()) {
            Log.d(TAG, "Same Time Found. Not doing anything for BT Scheduling");
            return;
        }

        Log.i(TAG, "BT Start Scheduled at " + DateFormat.getDateTimeInstance().format(startCal.getTimeInMillis()));
        Log.i(TAG, "BT End Scheduled at " + DateFormat.getDateTimeInstance().format(endCal.getTimeInMillis()));

        // Update Alarms
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, startCal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, btStartIntent);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, endCal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, btEndIntent);

        sendNotification(context, sp.getInt(QHConstants.QH_WIFI_NOTIFICATION, QHConstants.QH_NOTIFY_NEVER), "BT QH Enabled", false, startCal.getTimeInMillis());
        sendNotification(context, sp.getInt(QHConstants.QH_WIFI_NOTIFICATION, QHConstants.QH_NOTIFY_NEVER), "BT QH Disabled", false, endCal.getTimeInMillis());

        Log.i(TAG, "Scheduled BT Quiet Hours after boot");
    }

    // Presume Enabled. Do Check first
    private void scheduleWifi(Context context) {
        Log.i(TAG, "Start schedule of Wifi");
        String defWifi = sp.getString(QH_WIFI_TIME, "");
        if (defWifi.isEmpty()) return; // Cannot schedule dude
        ConnectivityPeriod wifiConnectivity = new ConnectivityPeriod(defWifi);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent wSI = new Intent(context, WifiToggleReceiver.class).putExtra("status", true);
        Intent wEI = new Intent(context, WifiToggleReceiver.class).putExtra("status", false);
        PendingIntent wifiStartIntent = PendingIntent.getBroadcast(context, WIFI_START_INTENT, wSI, 0);
        PendingIntent wifiEndIntent = PendingIntent.getBroadcast(context, WIFI_END_INTENT, wEI, 0);
        // Cancel all possible pending intents
        alarmManager.cancel(wifiStartIntent);
        alarmManager.cancel(wifiEndIntent);
        Log.i(TAG, "Cleared existing Wifi Schedules");

        // Set Alarm
        long millis = System.currentTimeMillis();
        Calendar startCal = Calendar.getInstance();
        startCal.setTimeInMillis(millis);
        startCal.set(Calendar.HOUR_OF_DAY, wifiConnectivity.getStartHr());
        startCal.set(Calendar.MINUTE, wifiConnectivity.getStartMin());
        startCal.set(Calendar.SECOND, 0);

        Calendar endCal = Calendar.getInstance();
        endCal.setTimeInMillis(millis);
        endCal.set(Calendar.HOUR_OF_DAY, wifiConnectivity.getEndHr());
        endCal.set(Calendar.MINUTE, wifiConnectivity.getEndMin());
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
        if ((started && ended) || (!started && ended)) {
            // Presume ended
            context.sendBroadcast(wEI);
            Log.i(TAG, "Fired Wifi QH End Intent");
            sendNotification(context, sp.getInt(QHConstants.QH_WIFI_NOTIFICATION, QHConstants.QH_NOTIFY_NEVER), "Wifi QH End", true, -1);
        } else if (started) {
            // Presume started
            context.sendBroadcast(wSI);
            Log.i(TAG, "Fired Wifi QH Start Intent");
            sendNotification(context, sp.getInt(QHConstants.QH_WIFI_NOTIFICATION, QHConstants.QH_NOTIFY_NEVER), "Wifi QH Start", true, -1);
        }

        if (startCal.getTimeInMillis() == endCal.getTimeInMillis()) {
            Log.d(TAG, "Same Time Found. Not doing anything for Wifi Scheduling");
            return;
        }

        Log.i(TAG, "Wifi Start Scheduled at " + DateFormat.getDateTimeInstance().format(startCal.getTimeInMillis()));
        Log.i(TAG, "Wifi End Scheduled at " + DateFormat.getDateTimeInstance().format(endCal.getTimeInMillis()));

        // Update Alarms
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, startCal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, wifiStartIntent);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, endCal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, wifiEndIntent);

        sendNotification(context, sp.getInt(QHConstants.QH_WIFI_NOTIFICATION, QHConstants.QH_NOTIFY_NEVER), "Wifi QH Enabled", false, startCal.getTimeInMillis());
        sendNotification(context, sp.getInt(QHConstants.QH_WIFI_NOTIFICATION, QHConstants.QH_NOTIFY_NEVER), "Wifi QH Disabled", false, endCal.getTimeInMillis());

        Log.i(TAG, "Scheduled Wifi Quiet Hours after boot");
    }

    private void sendNotification(Context context, int notificationLevel, String state, boolean prefire, long newtime) {
        String time = DateFormat.getTimeInstance().format(new Date(System.currentTimeMillis()));
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setSmallIcon(R.drawable.notification_icon).setContentTitle("Boot Quiet Hour Scheduling")
                .setAutoCancel(true)
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
