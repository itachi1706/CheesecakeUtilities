package com.itachi1706.cheesecakeutilities

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.itachi1706.cepaslib.SettingsHandler
import com.itachi1706.cheesecakeutilities.modules.connectivityQuietHours.QHConstants
import com.itachi1706.helperlib.helpers.PrefHelper

class UtilitySettingsActivity : AppCompatActivity() {

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportFragmentManager.beginTransaction().replace(android.R.id.content, UtilitySettingsFragment()).commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean { return if (item?.itemId == android.R.id.home) { finish(); true } else super.onOptionsItemSelected(item); }

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
