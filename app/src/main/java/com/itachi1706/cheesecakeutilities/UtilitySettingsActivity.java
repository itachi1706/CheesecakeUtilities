package com.itachi1706.cheesecakeutilities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;

import com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.QHConstants;

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
        }
    }
}
