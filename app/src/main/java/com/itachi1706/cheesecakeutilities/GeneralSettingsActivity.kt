package com.itachi1706.cheesecakeutilities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.Preference
import com.itachi1706.appupdater.EasterEggResMultiMusicPrefFragment
import com.itachi1706.appupdater.SettingsInitializer
import com.itachi1706.appupdater.Util.PrefHelper
import com.itachi1706.cheesecakeutilities.features.biometricAuth.AuthenticationActivity
import com.itachi1706.cheesecakeutilities.features.biometricAuth.BiometricCompatHelper
import com.itachi1706.cheesecakeutilities.features.UtilityManagement.ManageUtilityActivity
import com.itachi1706.cheesecakeutilities.util.CommonVariables
import me.jfenn.attribouter.Attribouter

/**
 * Created by Kenneth on 24/7/2019.
 * for com.itachi1706.cheesecakeutilities in CheesecakeUtilities
 */
class GeneralSettingsActivity : AppCompatActivity() {
    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, GeneralPreferenceFragment())
                .commit()
    }

    class GeneralPreferenceFragment : EasterEggResMultiMusicPrefFragment() {

        private lateinit var sp: SharedPreferences
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.pref_general)

            SettingsInitializer().setFullscreen(true).explodeUpdaterSettings(activity, R.drawable.notification_icon, CommonVariables.BASE_SERVER_URL,
                    resources.getString(R.string.link_legacy), resources.getString(R.string.link_updates), this)
            super.addEggMethods(false, null, true) { Attribouter.from(context).show(); true }

            val fpPw: Preference? = findPreference("password_fp")
            sp = PrefHelper.getDefaultSharedPreferences(activity)
            updatePasswordViews(fpPw)

            findPreference<Preference>("testpw")?.setOnPreferenceClickListener {
                startActivity(Intent(activity, AuthenticationActivity::class.java))
                false
            }

            findPreference<Preference>("hide_util")?.setOnPreferenceClickListener {
                startActivity(Intent(activity, ManageUtilityActivity::class.java))
                false
            }

            findPreference<Preference>("util_settings")?.setOnPreferenceClickListener {
                startActivity(Intent(activity, UtilitySettingsActivity::class.java))
                false
            }

            findPreference<Preference>(BiometricCompatHelper.APP_BIOMETRIC_COMPAT_ENABLED)?.setOnPreferenceChangeListener { _, newValue ->
                updatePasswordViews(fpPw, newValue as Boolean, 0)
                true
            }

            findPreference<Preference>(BiometricCompatHelper.SCREEN_LOCK_ENABLED)?.setOnPreferenceChangeListener { _, newValue ->
                updatePasswordViews(fpPw, newValue as Boolean, 1)
                true
            }

            findPreference<Preference>("firebase_signin")?.setOnPreferenceClickListener {
                val i = Intent(activity, FirebaseLoginActivity::class.java)
                i.putExtra("persist", true)
                startActivity(i)
                false
            }

            findPreference<Preference>("app_theme")?.setOnPreferenceChangeListener { _, newValue ->
                updateDarkModeSetting(newValue.toString())
                true
            }

        }

        companion object {
            fun updateDarkModeSetting(newValue: String) {
                when (newValue) {
                    "light" -> PrefHelper.changeDarkModeTheme(AppCompatDelegate.MODE_NIGHT_NO, "Light")
                    "dark" -> PrefHelper.changeDarkModeTheme(AppCompatDelegate.MODE_NIGHT_YES, "Dark")
                    else -> {
                        // Set as battery saver default if P and below
                        PrefHelper.changeDarkModeTheme(if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY else AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
                                "default " + if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) "battery" else "system")
                    }
                }
            }
        }

        override fun onResume() {
            super.onResume()

            updatePasswordViews(findPreference("password_fp"))
            val hasSL = BiometricCompatHelper.isScreenLockEnabled(activity!!)
            findPreference<Preference>(BiometricCompatHelper.SCREEN_LOCK_ENABLED)?.isEnabled = hasSL
            findPreference<Preference>(BiometricCompatHelper.APP_BIOMETRIC_COMPAT_ENABLED)?.isEnabled = hasSL
        }

        private fun updatePasswordViews(fpPw: Preference?, value: Boolean = BiometricCompatHelper.requireFPAuth(sp), type: Int = -1) {
            var isScreenLock = BiometricCompatHelper.isScreenLockProtectionEnabled(activity!!)
            var isFP = BiometricCompatHelper.requireFPAuth(sp)

            when (type) {
                0 -> isFP = value
                1 -> isScreenLock = value
            }
            var summary = "Unprotected"
            if (isScreenLock) {
                if (BiometricCompatHelper.isScreenLockEnabled(activity!!)) {
                    summary = "Protected with device screen lock"
                    if (isFP) {
                        if (BiometricCompatHelper.isBiometricFPRegistered(activity!!)) summary = "Protected with fingerprint + screen lock"
                        else summary += " (No fingerprint found on device)"
                    }
                } else summary = "Unprotected (No screen lock found)"
            } else if (isFP) {
                summary = if (!BiometricCompatHelper.isScreenLockEnabled(activity!!)) "Unprotected (No screen lock found)" // No FP without a screen lock
                else if (BiometricCompatHelper.isBiometricFPRegistered(activity!!)) "Protected with fingerprint"
                else "Unprotected (No fingerprint found on device)"
            }

            fpPw?.summary = summary
        }

        override fun getMusicResource(): Int {
            return R.raw.hello
        }

        override fun getStartEggMessage(): String {
            return "Hello!"
        }

        override fun getEndEggMessage(): String {
            return "Music Stopped"
        }

        override fun getStopEggButtonText(): String {
            return "SILENCE"
        }
    }
}