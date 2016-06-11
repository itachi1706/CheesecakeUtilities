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

            //Preference prefs =  findPreference("view_board_ver");
            findPreference("view_board_ver").setSummary(android.os.Build.BOARD);
            findPreference("view_bootloader_ver").setSummary(android.os.Build.BOOTLOADER);
            findPreference("view_brand_ver").setSummary(android.os.Build.BRAND);
            findPreference("view_cpu1_ver").setSummary(android.os.Build.CPU_ABI);
            findPreference("view_cpu2_ver").setSummary(android.os.Build.CPU_ABI2);
            findPreference("view_device_ver").setSummary(android.os.Build.DEVICE);
            findPreference("view_display_ver").setSummary(android.os.Build.DISPLAY);
            findPreference("view_fingerprint_ver").setSummary(android.os.Build.FINGERPRINT);
            findPreference("view_hardware_ver").setSummary(android.os.Build.HARDWARE);
            findPreference("view_host_ver").setSummary(android.os.Build.HOST);
            findPreference("view_id_ver").setSummary(android.os.Build.ID);
            findPreference("view_manufacturer_ver").setSummary(android.os.Build.MANUFACTURER);
            findPreference("view_model_ver").setSummary(android.os.Build.MODEL);
            findPreference("view_product_ver").setSummary(android.os.Build.PRODUCT);
            findPreference("view_serial_ver").setSummary(android.os.Build.SERIAL);
            findPreference("view_tags_ver").setSummary(android.os.Build.TAGS);
            findPreference("view_type_ver").setSummary(android.os.Build.TYPE);
            if (android.os.Build.USER != null){
                findPreference("view_user_ver").setSummary(android.os.Build.USER);
            }
        }
    }
}
