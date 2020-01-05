package com.itachi1706.cheesecakeutilities.modules.connectivityQuietHours.receivers;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import com.itachi1706.helperlib.helpers.PrefHelper;
import com.itachi1706.cheesecakeutilities.BaseBroadcastReceiver;
import com.itachi1706.cheesecakeutilities.modules.connectivityQuietHours.QHConstants;
import com.itachi1706.cheesecakeutilities.util.LogHelper;

public class BluetoothToggleReceiver extends BaseBroadcastReceiver {

    private static final String TAG = "QuietHour-BT";

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        LogHelper.i(TAG, "Waking up");
        boolean state = intent.getExtras().getBoolean("status");
        SharedPreferences sp = PrefHelper.getDefaultSharedPreferences(context);
        if (!sp.getBoolean(QHConstants.QH_BT_STATE, false)) return; // Not enabled
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) return; // No Hardware

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) return; // Can't get BT Adapter

        boolean workDone = false;
        boolean enabled = bluetoothAdapter.isEnabled();
        LogHelper.i(TAG, "Quiet Hour: " + state + " | Adapter Enabled: " + enabled);
        if (state) {
            // Quiet Mode Enabled, Turn off BT
            if (enabled) {
                bluetoothAdapter.disable();
                LogHelper.i(TAG, "Disabled Bluetooth at " + System.currentTimeMillis());
                workDone = true;
            }
        } else {
            // Quiet Mode Disabled, Turn On BT
            if (!enabled) {
                bluetoothAdapter.enable();
                LogHelper.i(TAG, "Enabled Bluetooth at " + System.currentTimeMillis());
                workDone = true;
            }
        }
        NotificationHelper.Companion.sendNotification(context, sp.getInt(QHConstants.QH_BT_NOTIFICATION, QHConstants.QH_NOTIFY_NEVER), workDone, state, "Bluetooth");
        if (workDone) logResult(sp, state);
        LogHelper.i(TAG, "Job Done");
    }

    private void logResult(SharedPreferences sp, boolean state) {
        String existing = sp.getString(QHConstants.QH_HISTORY, "");
        existing += "Bluetooth:" + ((state) ? "Enabled" : "Disabled") + ":" + System.currentTimeMillis() + ";";
        sp.edit().putString(QHConstants.QH_HISTORY, existing).apply();
    }
}
