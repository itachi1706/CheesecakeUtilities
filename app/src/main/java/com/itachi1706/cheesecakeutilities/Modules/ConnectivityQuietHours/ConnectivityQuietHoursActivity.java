package com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.itachi1706.appupdater.Util.PrefHelper;
import com.itachi1706.cheesecakeutilities.BaseModuleActivity;
import com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.Objects.ConnectivityPeriod;
import com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.Receivers.BluetoothToggleReceiver;
import com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.Receivers.BootRescheduleToggleReceiver;
import com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.Receivers.WifiToggleReceiver;
import com.itachi1706.cheesecakeutilities.objects.DualLineString;
import com.itachi1706.cheesecakeutilities.R;
import com.itachi1706.cheesecakeutilities.RecyclerAdapters.DualLineStringRecyclerAdapter;
import com.itachi1706.cheesecakeutilities.RecyclerAdapters.StringRecyclerAdapter;
import com.itachi1706.cheesecakeutilities.Util.CommonMethods;
import com.itachi1706.cheesecakeutilities.Util.LogHelper;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import static com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.QHConstants.BT_END_INTENT;
import static com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.QHConstants.BT_START_INTENT;
import static com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.QHConstants.QH_BT_NOTIFICATION;
import static com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.QHConstants.QH_BT_STATE;
import static com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.QHConstants.QH_BT_TIME;
import static com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.QHConstants.QH_NOTIFY_ALWAYS;
import static com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.QHConstants.QH_NOTIFY_DEBUG;
import static com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.QHConstants.QH_WIFI_NOTIFICATION;
import static com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.QHConstants.QH_WIFI_STATE;
import static com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.QHConstants.QH_WIFI_TIME;
import static com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.QHConstants.WIFI_END_INTENT;
import static com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.QHConstants.WIFI_START_INTENT;

public class ConnectivityQuietHoursActivity extends BaseModuleActivity {

    LinearLayout btLayout, wifiLayout; // Main Layouts
    Spinner btNotification, wifiNotification; // Notification Spinners
    LinearLayout wifiStart, wifiEnd, btStart, btEnd; // Clicking to launch time dialog
    TextView wifiStartTxt, wifiEndTxt, btStartTxt, btEndTxt; // Show time itself
    SwitchCompat btSwitch, wifiSwitch; // To schedule or not

    ConnectivityPeriod wifiConnectivity, btConnectivity;
    SharedPreferences sharedPreferences;
    AlarmManager alarmManager;

    // For History Processing
    LinearLayout historyLayout;
    RecyclerView historyRecyclerView;

    @Override
    public String getHelpDescription() {
        return "Configures \"Quiet Hours\" for your device where within the period, either wireless or bluetooth connectivity will" +
                " be turned off to help conserve power";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connectivity_quiet_hours);

        // Init elements
        btLayout = findViewById(R.id.layout_configure_bt);
        wifiLayout = findViewById(R.id.layout_configure_wifi);
        btNotification = findViewById(R.id.spinner_bt_notification);
        wifiNotification = findViewById(R.id.spinner_wifi_notification);
        wifiStart = findViewById(R.id.wifi_st_layout);
        wifiEnd = findViewById(R.id.wifi_et_layout);
        btStart = findViewById(R.id.bt_st_layout);
        btEnd = findViewById(R.id.bt_et_layout);
        wifiStartTxt = findViewById(R.id.wifi_start_time);
        wifiEndTxt = findViewById(R.id.wifi_end_time);
        btStartTxt = findViewById(R.id.bt_start_time);
        btEndTxt = findViewById(R.id.bt_end_time);
        btSwitch = findViewById(R.id.bt_activate);
        wifiSwitch = findViewById(R.id.wifi_activate);
        historyLayout = findViewById(R.id.layout_history);
        historyRecyclerView = findViewById(R.id.rv_qh_history);
        CommonMethods.disableAutofill(getWindow().getDecorView());

        sharedPreferences = PrefHelper.getDefaultSharedPreferences(this);

        // Init On Click for Time
        initOrRefreshConnectivityObjects();
        initOnClick(wifiStart, true, wifiStartTxt, QH_WIFI_TIME, true);
        initOnClick(wifiEnd, false, wifiEndTxt, QH_WIFI_TIME, true);
        initOnClick(btStart, true, btStartTxt, QH_BT_TIME, false);
        initOnClick(btEnd, false, btEndTxt, QH_BT_TIME, false);

        // Init Enable toggle
        initEnableToggles(btSwitch, false);
        initEnableToggles(wifiSwitch, true);
        // Init Notification Toggle
        initItemSelected(btNotification, QH_BT_NOTIFICATION);
        initItemSelected(wifiNotification, QH_WIFI_NOTIFICATION);
    }

    private void initEnableToggles(SwitchCompat mSwitch, boolean isWifi) {
        initOrRefreshConnectivityObjects();
        int start = (isWifi) ? WIFI_START_INTENT : BT_START_INTENT;
        int end = (isWifi) ? WIFI_END_INTENT : BT_END_INTENT;
        String text = (isWifi) ? "Wifi" : "BT";
        Class className = (isWifi) ? WifiToggleReceiver.class : BluetoothToggleReceiver.class;
        String prefName = (isWifi) ? QH_WIFI_STATE : QH_BT_STATE;

        mSwitch.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean(prefName, isChecked).apply();
            toggleConnectivitySwitch(start, end, text, mSwitch, (isWifi) ? wifiConnectivity : btConnectivity, className);
        }));
    }

    private void initOrRefreshConnectivityObjects() {
        String defBt = sharedPreferences.getString(QH_BT_TIME, "");
        String defWifi = sharedPreferences.getString(QH_WIFI_TIME, "");
        if (btConnectivity != null) btConnectivity = null;
        if (wifiConnectivity != null) wifiConnectivity = null;
        btConnectivity = (defBt.isEmpty()) ? new ConnectivityPeriod(0,0,0,0) : new ConnectivityPeriod(defBt);
        wifiConnectivity = (defWifi.isEmpty()) ? new ConnectivityPeriod(0,0,0,0) : new ConnectivityPeriod(defWifi);
    }

    private void toggleConnectivitySwitch(int startIntent, int endIntent, String name, SwitchCompat mSwitch, ConnectivityPeriod period, Class className) {
        PendingIntent connStartIntent = PendingIntent.getBroadcast(this, startIntent, new Intent(this, className).putExtra("status", true), 0);
        PendingIntent connEndIntent = PendingIntent.getBroadcast(this, endIntent, new Intent(this, className).putExtra("status", false), 0);
        // Cancel all possible pending intents
        if (alarmManager == null) alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        assert alarmManager != null;
        alarmManager.cancel(connStartIntent);
        alarmManager.cancel(connEndIntent);
        LogHelper.i("QH", "Cleared existing " + name + " Schedules");
        if (mSwitch.isChecked()) { // Enabled
            // Set Alarm
            long millis = System.currentTimeMillis();
            Calendar startCal = Calendar.getInstance();
            startCal.setTimeInMillis(millis);
            startCal.set(Calendar.HOUR_OF_DAY, period.getStartHr());
            startCal.set(Calendar.MINUTE, period.getStartMin());
            startCal.set(Calendar.SECOND, 0);

            Calendar endCal = Calendar.getInstance();
            endCal.setTimeInMillis(millis);
            endCal.set(Calendar.HOUR_OF_DAY, period.getEndHr());
            endCal.set(Calendar.MINUTE, period.getEndMin());
            endCal.set(Calendar.SECOND, 0);

            if (millis > startCal.getTimeInMillis()) startCal.add(Calendar.DAY_OF_YEAR, 1);
            if (millis > endCal.getTimeInMillis()) endCal.add(Calendar.DAY_OF_YEAR, 1);

            if (startCal.getTimeInMillis() == endCal.getTimeInMillis()) {
                LogHelper.d("QH", "Same Time Found. Not doing anything for " + name + " Scheduling");
                return;
            }

            LogHelper.i("QH", name + " Start Scheduled at " + DateFormat.getDateTimeInstance().format(startCal.getTimeInMillis()));
            LogHelper.i("QH", name + " End Scheduled at " + DateFormat.getDateTimeInstance().format(endCal.getTimeInMillis()));

            // Update Alarms
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, startCal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, connStartIntent);
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, endCal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, connEndIntent);
            LogHelper.i("QH", "Updated " + name + " Toggle State");
        }
        toggleBootReceiver();
    }

    private void toggleBootReceiver() {
        ComponentName receiver = new ComponentName(this, BootRescheduleToggleReceiver.class);
        PackageManager pm = getPackageManager();

        // COMPONENT_ENABLED_STATE_DISABLED - Disabled
        // COMPONENT_ENABLED_STATE_ENABLED - Enabled
        if (pm.getComponentEnabledSetting(receiver) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            LogHelper.i("QH", "Boot Receiver Status: Enabled");
            // Enabled. Check if I should be disabling it
            if (!btSwitch.isChecked() && !wifiSwitch.isChecked()) {
                // No service running, disable boot receiver
                LogHelper.i("QH", "No toggles toggled, disabling boot receiver to save CPU time");
                pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                LogHelper.i("QH", "Boot Receiver disabled");
            }
        } else {
            LogHelper.i("QH", "Boot Receiver Status: Disabled");
            if (btSwitch.isChecked() || wifiSwitch.isChecked()) {
                LogHelper.i("QH", "One of the toggle is toggled, enabling boot receiver...");
                pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                LogHelper.i("QH", "Boot Receiver enabled");

            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        initOrRefreshConnectivityObjects();

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        btSwitch.setChecked(sharedPreferences.getBoolean(QH_BT_STATE, false));
        wifiSwitch.setChecked(sharedPreferences.getBoolean(QH_WIFI_STATE, false));
        btNotification.setSelection(sharedPreferences.getInt(QH_BT_NOTIFICATION, 0));
        wifiNotification.setSelection(sharedPreferences.getInt(QH_WIFI_NOTIFICATION, 0));

        btStartTxt.setText(get12HrTime(btConnectivity.getStartHr(), btConnectivity.getStartMin()));
        btEndTxt.setText(get12HrTime(btConnectivity.getEndHr(), btConnectivity.getEndMin()));
        wifiStartTxt.setText(get12HrTime(wifiConnectivity.getStartHr(), wifiConnectivity.getStartMin()));
        wifiEndTxt.setText(get12HrTime(wifiConnectivity.getEndHr(), wifiConnectivity.getEndMin()));
        toggleBootReceiver();

        // Update to Always from Always (VERBOSE) if Firebase disables it
        // Firebase key: quiethour_debug_mode
        FirebaseRemoteConfig firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        firebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);
        if (!firebaseRemoteConfig.getBoolean("quiethour_debug_mode")) {
            if (btNotification.getSelectedItemPosition() == QH_NOTIFY_DEBUG) {
                btNotification.setSelection(QH_NOTIFY_ALWAYS);
                sharedPreferences.edit().putInt(QH_BT_NOTIFICATION, QH_NOTIFY_ALWAYS).apply();
            }
            if (wifiNotification.getSelectedItemPosition() == QH_NOTIFY_DEBUG) {
                wifiNotification.setSelection(QH_NOTIFY_ALWAYS);
                sharedPreferences.edit().putInt(QH_WIFI_NOTIFICATION, QH_NOTIFY_ALWAYS).apply();
            }
            List<String> noDebugSpinnerOpts = new ArrayList<>();
            Collections.addAll(noDebugSpinnerOpts, getResources().getStringArray(R.array.connectivity_notification_type));
            noDebugSpinnerOpts.remove("Always (DEBUG)");
            ArrayAdapter<String> noDebugSpinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                    noDebugSpinnerOpts);
            wifiNotification.setAdapter(noDebugSpinnerAdapter);
            btNotification.setAdapter(noDebugSpinnerAdapter);
            // Reinsert selection
            btNotification.setSelection(sharedPreferences.getInt(QH_BT_NOTIFICATION, 0));
            wifiNotification.setSelection(sharedPreferences.getInt(QH_WIFI_NOTIFICATION, 0));
        }

        // Hide layout if hardware doesnt exist
        boolean wifiFeature = getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI);
        boolean btFeature = getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH);
        wifiLayout.setVisibility((wifiFeature) ? View.VISIBLE : View.GONE);
        btLayout.setVisibility((btFeature) ? View.VISIBLE : View.GONE);

        if (!wifiFeature && !btFeature) {
            // Why the hell are you trying to use this utility lmao. you dont even have the hardware
            new AlertDialog.Builder(this).setTitle("Hardware Not Found")
                    .setMessage("This device does not have WiFi or Bluetooth capabilities and hence cannot utilize this utility." +
                            " This utility will now exit")
                    .setCancelable(false).setPositiveButton(android.R.string.ok, (dialog, which) -> finish()).show();
        }

        // Setup the recyclerview
        historyRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        historyRecyclerView.setLayoutManager(linearLayoutManager);
        historyRecyclerView.setItemAnimator(new DefaultItemAnimator());

        String historyLines = sharedPreferences.getString(QHConstants.QH_HISTORY, "");
        LogHelper.d("QH", "History: " + historyLines);
        if (historyLines.isEmpty()) {
            // Show no history
            String[] s = new String[1];
            s[0] = "No History Found";
            StringRecyclerAdapter adapter = new StringRecyclerAdapter(s, false);
            historyRecyclerView.setAdapter(adapter);
        } else {
            List<DualLineString> ds = new ArrayList<>();
            String[] s1 = historyLines.split(";");
            for (String s2 : s1) {
                String[] s3 = s2.split(":");
                if (s3.length != 3) continue;

                s3[1] = (s3[1].equalsIgnoreCase("Enabled")) ? "<font color='green'>" + s3[1] + "</font>"
                        : "<font color='red'>" + s3[1] + "</font>";
                ds.add(new DualLineString(s3[0] + " Quiet Hour State " + s3[1], "Triggered at: " + DateFormat.getDateTimeInstance().format(Long.parseLong(s3[2]))));
            }
            Collections.reverse(ds);

            DualLineStringRecyclerAdapter adapter = new DualLineStringRecyclerAdapter(ds, false);
            adapter.setHtmlFormat(true);
            historyRecyclerView.setAdapter(adapter);
        }


        // Hide History if disabled
        if (sharedPreferences.getBoolean(QHConstants.QH_HISTORY_VIEW, true)) historyLayout.setVisibility(View.VISIBLE);
        else historyLayout.setVisibility(View.GONE);
    }

    private static String get12HrTime(int hr, int min) {
        // Parse Min String
        String minStr = (min < 10) ? "0" + min : Integer.toString(min);
        if (hr > 12) return (hr - 12) + ":" + minStr + " pm";
        if (hr == 12) return "12:" + minStr + " pm";
        if (hr == 0) return "12:" + minStr + " am";
        return hr + ":" + minStr + " am";
    }

    private ConnectivityPeriod getConnPeriod(boolean isWifi) {
        initOrRefreshConnectivityObjects();
        if (isWifi) return wifiConnectivity;
        else return btConnectivity;
    }

    private int getTime(boolean isWifi, boolean isStart, boolean isHour) {
        ConnectivityPeriod p = (isWifi) ? wifiConnectivity : btConnectivity;

        if (isHour && isStart) return p.getStartHr();
        else if (isHour) return p.getEndHr();
        else if (isStart) return p.getStartMin();
        else return p.getEndMin();
    }

    private void initOnClick(LinearLayout layout, boolean isStart, TextView tv, String prefKey, boolean isWifi) {
        layout.setOnClickListener(v -> new TimePickerDialog(v.getContext(), (view, hourOfDay, minute) -> {
            ConnectivityPeriod period = getConnPeriod(isWifi);
            tv.setText(get12HrTime(hourOfDay, minute));
            if (isStart) {
                period.setStartHr(hourOfDay);
                period.setStartMin(minute);
            } else {
                period.setEndHr(hourOfDay);
                period.setEndMin(minute);
            }
            sharedPreferences.edit().putString(prefKey, period.serialize()).apply();
            if (isWifi) toggleConnectivitySwitch(WIFI_START_INTENT, WIFI_END_INTENT, "Wifi", wifiSwitch, period, WifiToggleReceiver.class);
            else toggleConnectivitySwitch(BT_START_INTENT, BT_END_INTENT, "BT", btSwitch, period, BluetoothToggleReceiver.class);
        }, getTime(isWifi, isStart, true), getTime(isWifi, isStart, false), false).show());
    }

    private void initItemSelected(Spinner spinner, String prefKey) {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sharedPreferences.edit().putInt(prefKey, position).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Option will remain the same
            }
        });
    }
}
