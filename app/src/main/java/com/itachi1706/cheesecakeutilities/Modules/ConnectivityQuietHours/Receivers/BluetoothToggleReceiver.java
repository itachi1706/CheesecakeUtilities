package com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.Receivers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.itachi1706.appupdater.Util.UpdaterHelper;
import com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.ConnectivityQuietHoursActivity;
import com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.QHConstants;
import com.itachi1706.cheesecakeutilities.R;

import java.text.DateFormat;
import java.util.Date;
import java.util.Random;

public class BluetoothToggleReceiver extends BroadcastReceiver {

    private static final String TAG = "QuietHour-BT";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Waking up");
        boolean state = intent.getExtras().getBoolean("status");
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        if (!sp.getBoolean(QHConstants.QH_BT_STATE, false)) return; // Not enabled
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) return; // No Hardware

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) return; // Can't get BT Adapter

        boolean workDone = false;
        boolean enabled = bluetoothAdapter.isEnabled();
        Log.i(TAG, "Quiet Hour: " + state + " | Adapter Enabled: " + enabled);
        if (state) {
            // Quiet Mode Enabled, Turn off BT
            if (enabled) {
                bluetoothAdapter.disable();
                Log.i(TAG, "Disabled Bluetooth at " + System.currentTimeMillis());
                workDone = true;
            }
        } else {
            // Quiet Mode Disabled, Turn On BT
            if (!enabled) {
                bluetoothAdapter.enable();
                Log.i(TAG, "Enabled Bluetooth at " + System.currentTimeMillis());
                workDone = true;
            }
        }
        sendNotification(context, sp.getInt(QHConstants.QH_BT_NOTIFICATION, QHConstants.QH_NOTIFY_NEVER), workDone, state);
        if (workDone) logResult(sp, state);
        Log.i(TAG, "Job Done");
    }

    private void sendNotification(Context context, int notificationLevel, boolean workDone, boolean btState) {
        String time = DateFormat.getTimeInstance().format(new Date(System.currentTimeMillis()));
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // Create the Notification Channel
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(QHConstants.QH_NOTIFICATION_CHANNEL, QHConstants.QH_NOTIFICATION_CHANNEL_TITLE, NotificationManager.IMPORTANCE_DEFAULT);
            mChannel.setDescription(QHConstants.QH_NOTIFICATION_CHANNEL_DESC);
            mChannel.enableLights(false);
            notificationManager.createNotificationChannel(mChannel);
        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, QHConstants.QH_NOTIFICATION_CHANNEL);
        mBuilder.setSmallIcon(R.drawable.notification_icon).setContentTitle("Bluetooth Quiet Hour " + ((btState) ? "Enabled" : "Disabled"))
                .setContentText("Bluetooth state toggled on " + time)
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
        existing += "Bluetooth:" + ((state) ? "Enabled" : "Disabled") + ":" + System.currentTimeMillis() + ";";
        sp.edit().putString(QHConstants.QH_HISTORY, existing).apply();
    }
}
