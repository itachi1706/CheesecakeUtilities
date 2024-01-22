package com.itachi1706.cheesecakeutilities.modules.systeminformation.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.itachi1706.cheesecakeutilities.BaseBroadcastReceiver;
import com.itachi1706.cheesecakeutilities.R;

import androidx.fragment.app.Fragment;

public class BatteryFragment extends Fragment {
    private TextView batteryInfo;
    BroadcastReceiver batteryStatusReceiver;
    private ProgressBar pb;

    class BatteryReceiver extends BaseBroadcastReceiver {
        BatteryReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            super.onReceive(context, intent);
            int level = intent.getIntExtra("level", -1);
            int voltage = intent.getIntExtra("voltage", -1);
            int status = intent.getIntExtra("status", -1);
            int plug = intent.getIntExtra("plugged", -1);
            int health = intent.getIntExtra("health", -1);
            int temp = intent.getIntExtra("temperature", -1);
            BatteryFragment.this.pb.setProgress(level);
            BatteryFragment.this.batteryInfo.setText(BatteryFragment.this.getString(R.string.sys_info_battery_level) + Integer.toString(level) + "%\n\n" + BatteryFragment.this.getString(R.string.sys_info_battery_voltage) + Integer.toString(voltage) + " mV\n\n" + BatteryFragment.this.getString(R.string.sys_info_battery_status) + BatteryFragment.this.getStatusString(status) + "\n\n" + BatteryFragment.this.getString(R.string.sys_info_battery_plug) + BatteryFragment.this.getPlugTypeString(plug) + "\n\n" + BatteryFragment.this.getString(R.string.sys_info_battery_health) + BatteryFragment.this.getHealthString(health) + "\n\n" + BatteryFragment.this.getString(R.string.sys_info_battery_temp) + Integer.toString(temp / 10) + '\u00b0' + "C / " + Integer.toString((((temp / 10) * 9) / 5) + 32) + '\u00b0' + "F");
        }
    }

    public BatteryFragment() {
        this.batteryStatusReceiver = new BatteryReceiver();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tab_battery, container, false);
        this.batteryInfo = view.findViewById(R.id.batteryText);
        this.pb = view.findViewById(R.id.progressbar);
        return view;
    }

    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(this.batteryStatusReceiver, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
    }

    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(this.batteryStatusReceiver);
    }

    private String getPlugTypeString(int plug) {
        switch (plug) {
            case 1: return "AC";
            case 2: return "USB";
            case 4: return "Wireless";
            default: return getString(R.string.sys_info_battery_not_charging);
        }
    }

    private String getHealthString(int health) {
        switch (health) {
            case 2: return getString(R.string.sys_info_battery_health_good);
            case 3: return getString(R.string.sys_info_battery_health_over_h);
            case 4: return getString(R.string.sys_info_battery_health_dead);
            case 5: return getString(R.string.sys_info_battery_health_over_v);
            case 6: return getString(R.string.sys_info_battery_health_fail);
            default: return getString(R.string.sys_info_battery_health_unknown);
        }
    }

    private String getStatusString(int status) {
        switch (status) {
            case 2: return getString(R.string.sys_info_battery_charging);
            case 3: return getString(R.string.sys_info_battery_discharging);
            case 4: return getString(R.string.sys_info_battery_not_charging);
            case 5: return getString(R.string.sys_info_battery_full);
            default: return getString(R.string.sys_info_battery_health_unknown);
        }
    }
}
