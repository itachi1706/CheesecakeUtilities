package com.itachi1706.cheesecakeutilities.modules.navbarCustomization;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.itachi1706.cheesecakeutilities.BaseModuleActivity;
import com.itachi1706.cheesecakeutilities.R;
import com.itachi1706.cheesecakeutilities.util.LogHelper;

import net.grandcentrix.tray.AppPreferences;

import static com.itachi1706.cheesecakeutilities.modules.navbarCustomization.Utils.NAVBAR_IMAGE_TYPE_APP;
import static com.itachi1706.cheesecakeutilities.modules.navbarCustomization.Utils.NAVBAR_IMAGE_TYPE_RANDOM_IMG;
import static com.itachi1706.cheesecakeutilities.modules.navbarCustomization.Utils.NAVBAR_IMAGE_TYPE_STATIC;
import static com.itachi1706.cheesecakeutilities.modules.navbarCustomization.Utils.NAVBAR_SERVICE_ENABLED;
import static com.itachi1706.cheesecakeutilities.modules.navbarCustomization.Utils.NAVBAR_SHOW_APPNAME;
import static com.itachi1706.cheesecakeutilities.modules.navbarCustomization.Utils.NAVBAR_SHOW_CLOCK;
import static com.itachi1706.cheesecakeutilities.modules.navbarCustomization.Utils.NAVBAR_SHOW_IMAGE;
import static com.itachi1706.cheesecakeutilities.modules.navbarCustomization.Utils.NAVBAR_SHOW_IMAGE_TYPE;
import static com.itachi1706.cheesecakeutilities.modules.navbarCustomization.Utils.NAVBAR_SHOW_STATIC_COLOR;

/**
 * Note: Deprecated past Android Oreo
 */
public class NavbarConfigurationActivity extends BaseModuleActivity {

    private final int OVERLAY_PERMISSION_REQ_CODE = 1234;
    private SwitchCompat navbarToggle;
    private ImageView staticColor;

    private LinearLayout enableServiceLayout;
    private SwitchCompat enableServiceToggle;

    @Override
    public String getHelpDescription() {
        return "A navigation bar utility that makes the navigation bar more special and provides information in it";
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navbar_config);
        final AppPreferences sp = new AppPreferences(this);

        navbarToggle = findViewById(R.id.navbar_service_toggle);
        navbarToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked) {
                if (accessibilityServiceEnabled(NavbarConfigurationActivity.this)) {
                    new AlertDialog.Builder(NavbarConfigurationActivity.this).setTitle("Further Actions to disable needed")
                            .setMessage(R.string.nav_bar_accessibility_disable_prompt)
                            .setPositiveButton(android.R.string.ok, (dialog, which) -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))).setNegativeButton(android.R.string.cancel, (dialog, which) -> navbarToggle.setChecked(true)).setOnCancelListener(dialog -> navbarToggle.setChecked(true)).show();
                }
            } else {
                if (Utils.IS_AT_LEAST_MARSHMALLOW && !canShowOverlays()) {
                    new AlertDialog.Builder(NavbarConfigurationActivity.this).setTitle("Require Permission")
                            .setMessage(R.string.nav_bar_request_overlay_perm)
                            // Show request overlay
                            .setPositiveButton(android.R.string.ok, (dialog, which) -> allowSystemAlertWindow())
                            .setNegativeButton(android.R.string.cancel, (dialog, which) -> navbarToggle.setChecked(false))
                            .setOnCancelListener(dialog -> navbarToggle.setChecked(false)).show();
                } else {
                    requestAccessibilityServiceEnable();
                }
            }
        });

        enableServiceLayout = findViewById(R.id.enable_service);

        // Update Configurations
        SwitchCompat showClock = findViewById(R.id.navbar_service_toggle_clock);
        SwitchCompat showAppName = findViewById(R.id.navbar_service_toggle_app_name);
        SwitchCompat showImage = findViewById(R.id.navbar_service_toggle_image);
        staticColor = findViewById(R.id.navbar_service_static_color);
        enableServiceToggle = findViewById(R.id.navbar_service_enable_toggle);
        final Spinner imageType = findViewById(R.id.navbar_service_image_type);
        showAppName.setChecked(sp.getBoolean(NAVBAR_SHOW_APPNAME, true));
        showClock.setChecked(sp.getBoolean(NAVBAR_SHOW_CLOCK, true));
        showImage.setChecked(sp.getBoolean(NAVBAR_SHOW_IMAGE, true));

        enableServiceToggle.setOnCheckedChangeListener((compoundButton, b) -> {
            sp.put(NAVBAR_SERVICE_ENABLED, b);
            getApplicationContext().sendBroadcast(new Intent(Broadcasts.BROADCAST_ACTION));
        });

        showClock.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sp.put(NAVBAR_SHOW_CLOCK, isChecked);
            getApplicationContext().sendBroadcast(new Intent(Broadcasts.BROADCAST_ACTION));
        });

        showImage.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sp.put(NAVBAR_SHOW_IMAGE, isChecked);
            getApplicationContext().sendBroadcast(new Intent(Broadcasts.BROADCAST_ACTION));
        });

        showAppName.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sp.put(NAVBAR_SHOW_APPNAME, isChecked);
            getApplicationContext().sendBroadcast(new Intent(Broadcasts.BROADCAST_ACTION));
        });

        String type = sp.getString(NAVBAR_SHOW_IMAGE_TYPE, NAVBAR_IMAGE_TYPE_APP);
        assert type != null;
        switch (type) {
            case NAVBAR_IMAGE_TYPE_RANDOM_IMG:
                imageType.setSelection(1);
                imageType.setTag(1);
                break;
            case NAVBAR_IMAGE_TYPE_STATIC:
                imageType.setSelection(2);
                imageType.setTag(2);
                break;
            case NAVBAR_IMAGE_TYPE_APP:
            default:
                imageType.setSelection(0);
                imageType.setTag(0);
                break;
        }
        imageType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (imageType.getTag() == (Object) position) return;
                String type = imageType.getSelectedItem().toString();
                switch (type) {
                    case "Random Image":
                        sp.put(NAVBAR_SHOW_IMAGE_TYPE, NAVBAR_IMAGE_TYPE_RANDOM_IMG);
                        break;
                    case "Static Color":
                        sp.put(NAVBAR_SHOW_IMAGE_TYPE, NAVBAR_IMAGE_TYPE_STATIC);
                        break;
                    case "Current App Color":
                    default:
                        sp.put(NAVBAR_SHOW_IMAGE_TYPE, NAVBAR_IMAGE_TYPE_APP);
                        break;
                }
                imageType.setTag(null);
                getApplicationContext().sendBroadcast(new Intent(Broadcasts.BROADCAST_ACTION));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        staticColor.setOnClickListener(v -> ColorPickerDialogBuilder.with(NavbarConfigurationActivity.this).setTitle("Select Static Color")
                .initialColor(getColorFromPref(sp)).wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
                .density(12).setPositiveButton(android.R.string.ok, (dialogInterface, i, integers) -> updateColorPref(i, sp)).setNegativeButton(android.R.string.cancel, null).build().show());

        staticColor.setImageDrawable(new ColorDrawable(getColorFromPref(sp)));
    }

    private void updateColorPref(int newColor, AppPreferences sp) {
        staticColor.setImageDrawable(new ColorDrawable(newColor));
        sp.put(NAVBAR_SHOW_STATIC_COLOR, newColor);
        this.sendBroadcast(new Intent(Broadcasts.BROADCAST_ACTION));
    }

    private int getColorFromPref(AppPreferences sp) {
        return sp.getInt(NAVBAR_SHOW_STATIC_COLOR, Color.BLUE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Utils.IS_OREO_AND_ABOVE)
            new AlertDialog.Builder(this).setCancelable(false)
                    .setTitle("Not supported since Android Oreo")
                    .setMessage("API changes since Android Oreo makes in unfeasible to continue supporting this utility. Therefore this feature has been removed on those versions of Android")
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> finish()).show();

        AppPreferences sp = new AppPreferences(this);
        navbarToggle.setChecked(accessibilityServiceEnabled(this));
        if (accessibilityServiceEnabled(this))
            enableServiceLayout.setVisibility(View.VISIBLE);
        else {
            enableServiceLayout.setVisibility(View.GONE);
            sp.put(NAVBAR_SERVICE_ENABLED, true);
        }
        enableServiceToggle.setChecked(sp.getBoolean(NAVBAR_SERVICE_ENABLED, true));
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
        super.onActivityResult(requestCode, resultCode, data);
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
                    .setMessage(R.string.nav_bar_accessibility_enable_prompt)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))).setNegativeButton(android.R.string.cancel, (dialog, which) -> navbarToggle.setChecked(false)).setOnCancelListener(dialog -> navbarToggle.setChecked(false)).show();
        }
    }

    private static final String TAG = "NAVBAR-SETUP";

    /**
     * Check if accessibility service is enabled
     *
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
            LogHelper.e(TAG, "Error finding setting, default accessibility to not found: "
                    + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            LogHelper.v(TAG, "Accessibility Status: Enabled");
            String settingValue = Settings.Secure.getString(
                    mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();

                    LogHelper.v(TAG, "Accessibility Service: " + accessibilityService + " | " + service);
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        return true;
                    }
                }
            }
        } else {
            LogHelper.v(TAG, "Accessibility Status: Disabled");
        }

        return false;
    }
}
