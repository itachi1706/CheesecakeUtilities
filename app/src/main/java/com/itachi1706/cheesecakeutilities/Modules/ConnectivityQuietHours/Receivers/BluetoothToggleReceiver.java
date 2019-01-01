package com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.Receivers;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;

import com.itachi1706.appupdater.Util.PrefHelper;
import com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.QHConstants;

public class BluetoothToggleReceiver extends BroadcastReceiver {

    private static final String TAG = "QuietHour-BT";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Waking up");
        boolean state = intent.getExtras().getBoolean("status");
        SharedPreferences sp = PrefHelper.getDefaultSharedPreferences(context);
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
        NotificationHelper.Companion.sendNotification(context, sp.getInt(QHConstants.QH_BT_NOTIFICATION, QHConstants.QH_NOTIFY_NEVER), workDone, state, "Bluetooth");
        if (workDone) logResult(sp, state);
        Log.i(TAG, "Job Done");
    }

    private void logResult(SharedPreferences sp, boolean state) {
        String existing = sp.getString(QHConstants.QH_HISTORY, "");
        existing += "Bluetooth:" + ((state) ? "Enabled" : "Disabled") + ":" + System.currentTimeMillis() + ";";
        sp.edit().putString(QHConstants.QH_HISTORY, existing).apply();
    }
}
