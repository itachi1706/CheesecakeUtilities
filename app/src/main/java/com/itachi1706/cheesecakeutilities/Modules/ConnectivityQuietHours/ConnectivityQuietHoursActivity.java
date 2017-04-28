package com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours;

import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.itachi1706.cheesecakeutilities.BaseActivity;
import com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.Objects.ConnectivityPeriod;
import com.itachi1706.cheesecakeutilities.R;

public class ConnectivityQuietHoursActivity extends BaseActivity {

    LinearLayout btLayout, wifiLayout; // Main Layouts
    Spinner btNotification, wifiNotification; // Notification Spinners
    LinearLayout wifiStart, wifiEnd, btStart, btEnd; // Clicking to launch time dialog
    TextView wifiStartTxt, wifiEndTxt, btStartTxt, btEndTxt; // Show time itself
    SwitchCompat btSwitch, wifiSwitch; // To schedule or not

    ConnectivityPeriod wifiConnectivity, btConnectivity;
    SharedPreferences sharedPreferences;

    @Override
    public String getHelpDescription() {
        return "Wifi BT Connectivity Configurator";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connectivity_quiet_hours);

        // Init elements
        btLayout = (LinearLayout) findViewById(R.id.layout_configure_bt);
        wifiLayout = (LinearLayout) findViewById(R.id.layout_configure_wifi);
        btNotification = (Spinner) findViewById(R.id.spinner_bt_notification);
        wifiNotification = (Spinner) findViewById(R.id.spinner_wifi_notification);
        wifiStart = (LinearLayout) findViewById(R.id.wifi_st_layout);
        wifiEnd = (LinearLayout) findViewById(R.id.wifi_et_layout);
        btStart = (LinearLayout) findViewById(R.id.bt_st_layout);
        btEnd = (LinearLayout) findViewById(R.id.bt_et_layout);
        wifiStartTxt = (TextView) findViewById(R.id.wifi_start_time);
        wifiEndTxt = (TextView) findViewById(R.id.wifi_end_time);
        btStartTxt = (TextView) findViewById(R.id.bt_start_time);
        btEndTxt = (TextView) findViewById(R.id.bt_end_time);
        wifiSwitch = (SwitchCompat) findViewById(R.id.bt_activate);
        btSwitch = (SwitchCompat) findViewById(R.id.wifi_activate);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Init On Click for Time
        wifiStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerDialog(v.getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        wifiStartTxt.setText(get12HrTime(hourOfDay, minute));
                        wifiConnectivity.setStartHr(hourOfDay);
                        wifiConnectivity.setStartMin(minute);
                        sharedPreferences.edit().putString("quiethour_wifi", wifiConnectivity.serialize()).apply();
                    }
                }, wifiConnectivity.getStartHr(), wifiConnectivity.getStartMin(), false).show();
            }
        });
        wifiEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerDialog(v.getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        wifiEndTxt.setText(get12HrTime(hourOfDay, minute));
                        wifiConnectivity.setEndHr(hourOfDay);
                        wifiConnectivity.setEndMin(minute);
                        sharedPreferences.edit().putString("quiethour_wifi", wifiConnectivity.serialize()).apply();
                    }
                }, wifiConnectivity.getEndHr(), wifiConnectivity.getEndMin(), false).show();
            }
        });
        btStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerDialog(v.getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        btStartTxt.setText(get12HrTime(hourOfDay, minute));
                        btConnectivity.setStartHr(hourOfDay);
                        btConnectivity.setStartMin(minute);
                        sharedPreferences.edit().putString("quiethour_bt", btConnectivity.serialize()).apply();
                    }
                }, btConnectivity.getStartHr(), btConnectivity.getStartMin(), false).show();
            }
        });
        btEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerDialog(v.getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        btEndTxt.setText(get12HrTime(hourOfDay, minute));
                        btConnectivity.setEndHr(hourOfDay);
                        btConnectivity.setEndMin(minute);
                        sharedPreferences.edit().putString("quiethour_bt", btConnectivity.serialize()).apply();
                    }
                }, btConnectivity.getEndHr(), btConnectivity.getEndMin(), false).show();
            }
        });

        // Init Enable toggle
        btSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sharedPreferences.edit().putBoolean("quiethour_bt_status", isChecked).apply();
                toggleBtSwitch();
            }
        });
        wifiSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sharedPreferences.edit().putBoolean("quiethour_wifi_status", isChecked).apply();
                toggleWifiSwitch();
            }
        });
        // Init Notification Toggle
        btNotification.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sharedPreferences.edit().putInt("quiethour_bt_notification", position).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        wifiNotification.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sharedPreferences.edit().putInt("quiethour_wifi_notification", position).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void toggleBtSwitch() {
        // TODO: Code Stub
    }

    private void toggleWifiSwitch() {
        // TODO: Code Stub
    }

    @Override
    protected void onResume() {
        super.onResume();

        String defBt = sharedPreferences.getString("quiethour_bt", "");
        String defWifi = sharedPreferences.getString("quiethour_wifi", "");

        btConnectivity = (defBt.isEmpty()) ? new ConnectivityPeriod(0,0,0,0) : new ConnectivityPeriod(defBt);
        wifiConnectivity = (defWifi.isEmpty()) ? new ConnectivityPeriod(0,0,0,0) : new ConnectivityPeriod(defWifi);
        btSwitch.setChecked(sharedPreferences.getBoolean("quiethour_bt_status", false));
        wifiSwitch.setChecked(sharedPreferences.getBoolean("quiethour_wifi_status", false));
        btNotification.setSelection(sharedPreferences.getInt("quiethour_bt_notification", 0));
        wifiNotification.setSelection(sharedPreferences.getInt("quiethour_wifi_notification", 0));

        btStartTxt.setText(get12HrTime(btConnectivity.getStartHr(), btConnectivity.getStartMin()));
        btEndTxt.setText(get12HrTime(btConnectivity.getEndHr(), btConnectivity.getEndMin()));
        wifiStartTxt.setText(get12HrTime(wifiConnectivity.getStartHr(), wifiConnectivity.getStartMin()));
        wifiEndTxt.setText(get12HrTime(wifiConnectivity.getEndHr(), wifiConnectivity.getEndMin()));

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
                    .setCancelable(false).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            }).show();
        }
    }

    private static String get12HrTime(int hr, int min) {
        // Parse Min String
        String minStr = (min < 10) ? "0" + min : "" + min;
        if (hr > 12) return (hr - 12) + ":" + minStr + " pm";
        if (hr == 12) return "12:" + minStr + " pm";
        if (hr == 0) return "12:" + minStr + "am";
        return hr + ":" + minStr + " am";
    }
}
