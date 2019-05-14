package com.itachi1706.cheesecakeutilities.Modules.APKMirrorDownloader.fragment

import android.content.Intent
import android.os.Bundle
import androidx.preference.PreferenceFragment
import com.itachi1706.cheesecakeutilities.UtilitySettingsActivity

@Deprecated("Switched to using my own preference menu")
class PreferencesFragment : PreferenceFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        startActivity(Intent(activity, UtilitySettingsActivity::class.java))
    }

}

