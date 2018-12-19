package com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.Receivers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.itachi1706.appupdater.Util.PrefHelper;
import com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.ConnectivityQuietHoursActivity;
import com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.QHConstants;
import com.itachi1706.cheesecakeutilities.R;

import java.text.DateFormat;
import java.util.Date;
import java.util.Random;

import androidx.core.app.NotificationCompat;

public class WifiToggleReceiver extends BroadcastReceiver {

    private static final String TAG = "QuietHour-Wifi";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Waking up");
        boolean state = intent.getExtras().getBoolean("status");
        SharedPreferences sp = PrefHelper.getDefaultSharedPreferences(context);
        if (!sp.getBoolean(QHConstants.QH_WIFI_STATE, false)) return; // Not enabled
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI)) return; // No Hardware

        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null) return; // Can't get Wifi Manager

        boolean workDone = false;
        boolean enabled = wifiManager.isWifiEnabled();
        Log.i(TAG, "Quiet Hour: " + state + " | Adapter Enabled: " + enabled);
        if (state) {
            // Quiet Mode Enabled, Turn off Wifi
            if (enabled) {
                wifiManager.setWifiEnabled(false);
                Log.i(TAG, "Disabled Wifi at " + System.currentTimeMillis());
                workDone = true;
            }
        } else {
            // Quiet Mode Disabled, Turn On Wifi
            if (!enabled) {
                wifiManager.setWifiEnabled(true);
                Log.i(TAG, "Enabled Wifi at " + System.currentTimeMillis());
                workDone = true;
            }
        }
        sendNotification(context, sp.getInt(QHConstants.QH_WIFI_NOTIFICATION, QHConstants.QH_NOTIFY_NEVER), workDone, state);
        if (workDone) logResult(sp, state);
        Log.i(TAG, "Job Done");
    }

    private void sendNotification(Context context, int notificationLevel, boolean workDone, boolean wifiState) {
        String time = DateFormat.getTimeInstance().format(new Date(System.currentTimeMillis()));
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        QHConstants.createNotificationChannel(notificationManager); // Create the Notification Channel
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, QHConstants.QH_NOTIFICATION_CHANNEL);
        mBuilder.setSmallIcon(R.drawable.notification_icon).setContentTitle("Wi-Fi Quiet Hour " + ((wifiState) ? "Enabled" : "Disabled"))
                .setContentText("Wi-Fi state toggled on " + time)
                .setAutoCancel(true)
                .setGroup("connectivityqh")
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

    private void logResult(SharedPreferences sp, boolean state) {
        String existing = sp.getString(QHConstants.QH_HISTORY, "");
        existing += "WiFi:" + ((state) ? "Enabled" : "Disabled") + ":" + System.currentTimeMillis() + ";";
        sp.edit().putString(QHConstants.QH_HISTORY, existing).apply();
    }
}
