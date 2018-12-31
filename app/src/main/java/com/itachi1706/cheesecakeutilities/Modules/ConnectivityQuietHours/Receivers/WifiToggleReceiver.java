package com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.itachi1706.appupdater.Util.PrefHelper;
import com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.NotificationHelper;
import com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.QHConstants;

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
        NotificationHelper.sendNotification(context, sp.getInt(QHConstants.QH_WIFI_NOTIFICATION, QHConstants.QH_NOTIFY_NEVER), workDone, state, "Wi-Fi");
        if (workDone) logResult(sp, state);
        Log.i(TAG, "Job Done");
    }

    private void logResult(SharedPreferences sp, boolean state) {
        String existing = sp.getString(QHConstants.QH_HISTORY, "");
        existing += "WiFi:" + ((state) ? "Enabled" : "Disabled") + ":" + System.currentTimeMillis() + ";";
        sp.edit().putString(QHConstants.QH_HISTORY, existing).apply();
    }
}
