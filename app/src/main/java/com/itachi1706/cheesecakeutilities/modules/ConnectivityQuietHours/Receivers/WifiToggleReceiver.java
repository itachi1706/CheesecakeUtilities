package com.itachi1706.cheesecakeutilities.modules.ConnectivityQuietHours.Receivers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;

import com.itachi1706.appupdater.Util.PrefHelper;
import com.itachi1706.cheesecakeutilities.BaseBroadcastReceiver;
import com.itachi1706.cheesecakeutilities.modules.ConnectivityQuietHours.QHConstants;
import com.itachi1706.cheesecakeutilities.util.LogHelper;

public class WifiToggleReceiver extends BaseBroadcastReceiver {

    private static final String TAG = "QuietHour-Wifi";

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        LogHelper.i(TAG, "Waking up");
        boolean state = intent.getExtras().getBoolean("status");
        SharedPreferences sp = PrefHelper.getDefaultSharedPreferences(context);
        if (!sp.getBoolean(QHConstants.QH_WIFI_STATE, false)) return; // Not enabled
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI)) return; // No Hardware

        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null) return; // Can't get Wifi Manager

        boolean workDone = false;
        boolean enabled = wifiManager.isWifiEnabled();
        LogHelper.i(TAG, "Quiet Hour: " + state + " | Adapter Enabled: " + enabled);
        if (state) {
            // Quiet Mode Enabled, Turn off Wifi
            if (enabled) {
                wifiManager.setWifiEnabled(false);
                LogHelper.i(TAG, "Disabled Wifi at " + System.currentTimeMillis());
                workDone = true;
            }
        } else {
            // Quiet Mode Disabled, Turn On Wifi
            if (!enabled) {
                wifiManager.setWifiEnabled(true);
                LogHelper.i(TAG, "Enabled Wifi at " + System.currentTimeMillis());
                workDone = true;
            }
        }
        NotificationHelper.Companion.sendNotification(context, sp.getInt(QHConstants.QH_WIFI_NOTIFICATION, QHConstants.QH_NOTIFY_NEVER), workDone, state, "Wi-Fi");
        if (workDone) logResult(sp, state);
        LogHelper.i(TAG, "Job Done");
    }

    private void logResult(SharedPreferences sp, boolean state) {
        String existing = sp.getString(QHConstants.QH_HISTORY, "");
        existing += "WiFi:" + ((state) ? "Enabled" : "Disabled") + ":" + System.currentTimeMillis() + ";";
        sp.edit().putString(QHConstants.QH_HISTORY, existing).apply();
    }
}
