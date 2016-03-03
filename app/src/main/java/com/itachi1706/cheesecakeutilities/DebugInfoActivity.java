package com.itachi1706.cheesecakeutilities;


import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;


public class DebugInfoActivity extends AppCompatActivity {
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new GeneralPreferenceFragment())
                .commit();
    }


    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        @SuppressWarnings("deprecation")
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.pref_debug);
            getPreferenceManager().setSharedPreferencesMode(MODE_MULTI_PROCESS);

            Preference prefs =  findPreference("view_board_ver");
            prefs.setSummary(android.os.Build.BOARD);
            prefs = findPreference("view_bootloader_ver");
            prefs.setSummary(android.os.Build.BOOTLOADER);
            prefs = findPreference("view_brand_ver");
            prefs.setSummary(android.os.Build.BRAND);
            prefs = findPreference("view_cpu1_ver");
            prefs.setSummary(android.os.Build.CPU_ABI);
            prefs = findPreference("view_cpu2_ver");
            prefs.setSummary(android.os.Build.CPU_ABI2);
            prefs = findPreference("view_device_ver");
            prefs.setSummary(android.os.Build.DEVICE);
            prefs = findPreference("view_display_ver");
            prefs.setSummary(android.os.Build.DISPLAY);
            prefs = findPreference("view_fingerprint_ver");
            prefs.setSummary(android.os.Build.FINGERPRINT);
            prefs = findPreference("view_hardware_ver");
            prefs.setSummary(android.os.Build.HARDWARE);
            prefs = findPreference("view_host_ver");
            prefs.setSummary(android.os.Build.HOST);
            prefs = findPreference("view_id_ver");
            prefs.setSummary(android.os.Build.ID);
            prefs = findPreference("view_manufacturer_ver");
            prefs.setSummary(android.os.Build.MANUFACTURER);
            prefs = findPreference("view_model_ver");
            prefs.setSummary(android.os.Build.MODEL);
            prefs = findPreference("view_product_ver");
            prefs.setSummary(android.os.Build.PRODUCT);
            prefs = findPreference("view_serial_ver");
            prefs.setSummary(android.os.Build.SERIAL);
            prefs = findPreference("view_tags_ver");
            prefs.setSummary(android.os.Build.TAGS);
            prefs = findPreference("view_type_ver");
            prefs.setSummary(android.os.Build.TYPE);
            prefs = findPreference("view_user_ver");
            if (android.os.Build.USER != null){
                prefs.setSummary(android.os.Build.USER);
            }
        }
    }
}
