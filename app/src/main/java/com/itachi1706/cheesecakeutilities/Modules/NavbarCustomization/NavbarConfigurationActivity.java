package com.itachi1706.cheesecakeutilities.Modules.NavbarCustomization;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Spinner;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.itachi1706.cheesecakeutilities.BaseActivity;
import com.itachi1706.cheesecakeutilities.R;

import net.grandcentrix.tray.AppPreferences;

import static com.itachi1706.cheesecakeutilities.Modules.NavbarCustomization.Utils.NAVBAR_IMAGE_TYPE_APP;
import static com.itachi1706.cheesecakeutilities.Modules.NavbarCustomization.Utils.NAVBAR_IMAGE_TYPE_RANDOM_IMG;
import static com.itachi1706.cheesecakeutilities.Modules.NavbarCustomization.Utils.NAVBAR_IMAGE_TYPE_STATIC;
import static com.itachi1706.cheesecakeutilities.Modules.NavbarCustomization.Utils.NAVBAR_SHOW_APPNAME;
import static com.itachi1706.cheesecakeutilities.Modules.NavbarCustomization.Utils.NAVBAR_SHOW_CLOCK;
import static com.itachi1706.cheesecakeutilities.Modules.NavbarCustomization.Utils.NAVBAR_SHOW_IMAGE;
import static com.itachi1706.cheesecakeutilities.Modules.NavbarCustomization.Utils.NAVBAR_SHOW_IMAGE_TYPE;
import static com.itachi1706.cheesecakeutilities.Modules.NavbarCustomization.Utils.NAVBAR_SHOW_STATIC_COLOR;

public class NavbarConfigurationActivity extends BaseActivity {

    private final int OVERLAY_PERMISSION_REQ_CODE = 1234;
    private SwitchCompat navbarToggle;
    private ImageView staticColor;

    @Override
    public String getHelpDescription() {
        return "A navigation bar utility that makes the navigation bar more special and provides information in it";
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navbar_config);

        navbarToggle = (SwitchCompat) findViewById(R.id.navbar_service_toggle);
        navbarToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    if (accessibilityServiceEnabled(NavbarConfigurationActivity.this)) {
                        new AlertDialog.Builder(NavbarConfigurationActivity.this).setTitle("Further Actions to disable needed")
                                .setMessage(R.string.accessibility_disable_prompt)
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
                } else {
                    if (Utils.IS_AT_LEAST_MARSHMALLOW && !canShowOverlays()) {
                        new AlertDialog.Builder(NavbarConfigurationActivity.this).setTitle("Require Permission")
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

        // Update Configurations
        SwitchCompat showClock = (SwitchCompat) findViewById(R.id.navbar_service_toggle_clock);
        SwitchCompat showAppName = (SwitchCompat) findViewById(R.id.navbar_service_toggle_app_name);
        SwitchCompat showImage = (SwitchCompat) findViewById(R.id.navbar_service_toggle_image);
        staticColor = (ImageView) findViewById(R.id.navbar_service_static_color);
        final Spinner imageType = (Spinner) findViewById(R.id.navbar_service_image_type);
        final AppPreferences sp = new AppPreferences(this);
        showAppName.setChecked(sp.getBoolean(NAVBAR_SHOW_APPNAME, true));
        showClock.setChecked(sp.getBoolean(NAVBAR_SHOW_CLOCK, true));
        showImage.setChecked(sp.getBoolean(NAVBAR_SHOW_IMAGE, true));

        showClock.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sp.put(NAVBAR_SHOW_CLOCK, isChecked);
                getApplicationContext().sendBroadcast(new Intent(Broadcasts.BROADCAST_ACTION));
            }
        });

        showImage.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sp.put(NAVBAR_SHOW_IMAGE, isChecked);
                getApplicationContext().sendBroadcast(new Intent(Broadcasts.BROADCAST_ACTION));
            }
        });

        showAppName.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sp.put(NAVBAR_SHOW_APPNAME, isChecked);
                getApplicationContext().sendBroadcast(new Intent(Broadcasts.BROADCAST_ACTION));
            }
        });

        String type = sp.getString(NAVBAR_SHOW_IMAGE_TYPE, NAVBAR_IMAGE_TYPE_APP);
        assert type != null;
        switch (type) {
            case NAVBAR_IMAGE_TYPE_RANDOM_IMG: imageType.setSelection(1); imageType.setTag(1); break;
            case NAVBAR_IMAGE_TYPE_STATIC: imageType.setSelection(2); imageType.setTag(2); break;
            case NAVBAR_IMAGE_TYPE_APP:
            default: imageType.setSelection(0); imageType.setTag(0); break;
        }
        imageType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (imageType.getTag() == (Object) position) return;
                String type = imageType.getSelectedItem().toString();
                switch (type) {
                    case "Random Image": sp.put(NAVBAR_SHOW_IMAGE_TYPE, NAVBAR_IMAGE_TYPE_RANDOM_IMG); break;
                    case "Static Color": sp.put(NAVBAR_SHOW_IMAGE_TYPE, NAVBAR_IMAGE_TYPE_STATIC); break;
                    case "Current App Color":
                    default: sp.put(NAVBAR_SHOW_IMAGE_TYPE, NAVBAR_IMAGE_TYPE_APP); break;
                }
                imageType.setTag(null);
                getApplicationContext().sendBroadcast(new Intent(Broadcasts.BROADCAST_ACTION));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        staticColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ColorPickerDialogBuilder.with(NavbarConfigurationActivity.this).setTitle("Select Static Color")
                        .initialColor(Color.BLACK).wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
                        .density(12).setPositiveButton(android.R.string.ok, new ColorPickerClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, Integer[] integers) {
                        updateColorPref(i);
                    }
                }).setNegativeButton(android.R.string.cancel, null).showColorPreview(true).build().show();
            }
        });

        staticColor.setImageDrawable(new ColorDrawable(sp.getInt(NAVBAR_SHOW_STATIC_COLOR, Color.BLACK)));
    }

    private void updateColorPref(int newColor) {
        staticColor.setImageDrawable(new ColorDrawable(newColor));
        // TODO: Update AppPref
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
                // Disable toggle
                navbarToggle.setChecked(false);
            } else {
                requestAccessibilityServiceEnable();
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
        final String service = getPackageName() + "/" + NavBarService.class.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    mContext.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, "Error finding setting, default accessibility to not found: "
                    + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            Log.v(TAG, "Accessibility Status: Enabled");
            String settingValue = Settings.Secure.getString(
                    mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();

                    Log.v(TAG, "Accessibility Service: " + accessibilityService + " | " + service);
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        return true;
                    }
                }
            }
        } else {
            Log.v(TAG, "Accessibility Status: Disabled");
        }

        return false;
    }
}
