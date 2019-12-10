package com.itachi1706.cheesecakeutilities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.itachi1706.appupdater.Util.PrefHelper
import com.itachi1706.cepaslib.SettingsHandler
import com.itachi1706.cheesecakeutilities.modules.ConnectivityQuietHours.QHConstants

class UtilitySettingsActivity : AppCompatActivity() {

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        supportFragmentManager.beginTransaction().replace(android.R.id.content, UtilitySettingsFragment()).commit()
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    class UtilitySettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.pref_utility)
            SettingsHandler(activity!!).initSettings(this)

            val sp = PrefHelper.getDefaultSharedPreferences(activity)

            // Utility Specific
            // Clear Quiet Hour Utility History
            findPreference<Preference>("quiethour_clear_hist")?.setOnPreferenceClickListener {
                sp.edit().remove(QHConstants.QH_HISTORY).apply()
                Toast.makeText(activity, "History Cleared", Toast.LENGTH_SHORT).show()
                false
            }

            findPreference<Preference>("veh_mileage_report_rows")?.summary = (findPreference<Preference>("veh_mileage_report_rows") as EditTextPreference).text
            findPreference<Preference>("veh_mileage_report_rows")?.setOnPreferenceChangeListener { preference, newValue ->
                preference.summary = newValue.toString()
                true
            }
        }
    }
}
