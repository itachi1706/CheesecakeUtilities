package com.itachi1706.cheesecakeutilities;

import android.content.ComponentName;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.itachi1706.cheesecakeutilities.Modules.CEPASReader.activity.BackgroundTagActivity;
import com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.QHConstants;

import androidx.appcompat.app.AppCompatActivity;

import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED;

public class UtilitySettingsActivity extends AppCompatActivity {

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new UtilitySettingsFragment())
                .commit();
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    public static class UtilitySettingsFragment extends PreferenceFragment {
        @Override
        @SuppressWarnings("deprecation")
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.pref_utility);

            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());

            // Utility Specific
            // Clear Quiet Hour Utility History
            findPreference("quiethour_clear_hist").setOnPreferenceClickListener(preference -> {
                sp.edit().remove(QHConstants.QH_HISTORY).apply();
                Toast.makeText(getActivity(), "History Cleared", Toast.LENGTH_SHORT).show();
                return false;
            });

            findPreference("veh_mileage_report_rows").setSummary(((EditTextPreference)findPreference("veh_mileage_report_rows")).getText());
            findPreference("veh_mileage_report_rows").setOnPreferenceChangeListener((preference, newValue) -> {
                preference.setSummary(String.valueOf(newValue));
                return true;
            });

            CheckBoxPreference mPreferenceLaunchFromBackground = (CheckBoxPreference) findPreference("pref_launch_from_background");
            mPreferenceLaunchFromBackground.setChecked(isLaunchFromBgEnabled());
            mPreferenceLaunchFromBackground.setOnPreferenceChangeListener((preference, newValue) -> {
                setLaunchFromBgEnabled((Boolean) newValue);
                return true;
            });
        }

        private boolean isLaunchFromBgEnabled() {
            ComponentName componentName = new ComponentName(getActivity(), BackgroundTagActivity.class);
            PackageManager packageManager = getActivity().getPackageManager();
            int componentEnabledSetting = packageManager.getComponentEnabledSetting(componentName);
            return componentEnabledSetting == COMPONENT_ENABLED_STATE_ENABLED;
        }

        private void setLaunchFromBgEnabled(boolean enabled) {
            ComponentName componentName = new ComponentName(getActivity(), BackgroundTagActivity.class);
            PackageManager packageManager = getActivity().getPackageManager();
            int newState = enabled ? COMPONENT_ENABLED_STATE_ENABLED : COMPONENT_ENABLED_STATE_DISABLED;
            packageManager.setComponentEnabledSetting(componentName, newState, PackageManager.DONT_KILL_APP);
        }
    }
}
