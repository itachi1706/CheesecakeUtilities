/*
 * Copyright 2016 Vikram Kakkar
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.itachi1706.cheesecakeutilities.Modules.NavbarCustomization;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.itachi1706.cheesecakeutilities.R;

public class InitializationAct extends AppCompatActivity {

    private final int OVERLAY_PERMISSION_REQ_CODE = 1234;

    private Button bAllowSystemAlertWindow;
    private TextView tvAllowSystemAlertWindow;

    private SwitchCompat navbarToggle;

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_initialization);

        navbarToggle = (SwitchCompat) findViewById(R.id.navbar_service_toggle);
        navbarToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    // TODO: Disable shit
                } else {
                    // TODO: Enable shit
                    if (Utils.IS_AT_LEAST_MARSHMALLOW && !canShowOverlays()) {
                        new AlertDialog.Builder(InitializationAct.this).setTitle("Require Permission")
                                .setMessage(R.string.request_overlay_perm)
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Show request overlay
                                        allowSystemAlertWindow();
                                    }
                                }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                navbarToggle.setChecked(false);
                            }
                        }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                navbarToggle.setChecked(false);
                            }
                        }).show();
                    } else {
                        requestAccessibilityServiceEnable();
                    }
                }
            }
        });

        LinearLayout llAllowSystemAlertWindow = (LinearLayout) findViewById(R.id.ll_allow_system_alert_window);

        // Seek Settings.ACTION_MANAGE_OVERLAY_PERMISSION - required on Marshmallow & above
        bAllowSystemAlertWindow = (Button) findViewById(R.id.b_allow_system_alert_window);
        bAllowSystemAlertWindow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allowSystemAlertWindow();
            }
        });

        tvAllowSystemAlertWindow = (TextView) findViewById(R.id.tv_allow_system_alert_window);

        // Takes the user to the Accessibility Settings activity.
        // Here, you can enable/disable SublimeNavBar service
        Button bStartStopService = (Button) findViewById(R.id.b_start_stop_service);
        bStartStopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            }
        });

        // We only need permission if we're on Marshmallow & above.
        // Hide the Button if we're on a lower API level, or if the
        // permission has already been granted.
        if (Utils.IS_AT_LEAST_MARSHMALLOW) {
            if (!canShowOverlays()) {
                llAllowSystemAlertWindow.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        navbarToggle.setChecked(accessibilityServiceEnabled(this));
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean canShowOverlays() {
        return Settings.canDrawOverlays(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void allowSystemAlertWindow() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (!canShowOverlays()) {
                // `Settings.ACTION_MANAGE_OVERLAY_PERMISSION` was not granted...
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                // Disable toggle
                navbarToggle.setChecked(false);
            } else {
                // TODO: Request Accessibility
                requestAccessibilityServiceEnable();

                bAllowSystemAlertWindow.setVisibility(View.GONE);
                tvAllowSystemAlertWindow.setText(getResources()
                        .getString(R.string.text_permission_granted_restart_service));
            }
        }
    }

    private void requestAccessibilityServiceEnable() {
        if (!accessibilityServiceEnabled(this)) {
            new AlertDialog.Builder(this).setTitle("Further Actions needed")
                    .setMessage(R.string.accessibility_enable_prompt)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                        }
                    }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    navbarToggle.setChecked(false);
                }
            }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    navbarToggle.setChecked(false);
                }
            }).show();
        }
    }

    private static final String TAG = "NAVBAR-SETUP";

    /**
     * Check if accessibility service is enabled
     * @param mContext Application Context
     * @return true if enabled, false otherwise
     */
    private boolean accessibilityServiceEnabled(Context mContext) {
        int accessibilityEnabled = 0;
        final String service = getPackageName() + "/" + SublimeNavBarService.class.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    mContext.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
            Log.v(TAG, "accessibilityEnabled = " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, "Error finding setting, default accessibility to not found: "
                    + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            Log.v(TAG, "***ACCESSIBILITY IS ENABLED*** -----------------");
            String settingValue = Settings.Secure.getString(
                    mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();

                    Log.v(TAG, "-------------- > accessibilityService :: " + accessibilityService + " " + service);
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        Log.v(TAG, "We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }
        } else {
            Log.v(TAG, "***ACCESSIBILITY IS DISABLED***");
        }

        return false;
    }
}
