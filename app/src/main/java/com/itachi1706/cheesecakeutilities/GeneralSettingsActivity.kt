package com.itachi1706.cheesecakeutilities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.Preference
import com.itachi1706.appupdater.EasterEggResMultiMusicPrefFragment
import com.itachi1706.appupdater.SettingsInitializer
import com.itachi1706.cheesecakeutilities.features.biometricAuth.AuthenticationActivity
import com.itachi1706.cheesecakeutilities.features.biometricAuth.BiometricCompatHelper
import com.itachi1706.cheesecakeutilities.features.utilityManagement.ManageUtilityActivity
import com.itachi1706.cheesecakeutilities.util.CommonVariables
import com.itachi1706.helperlib.helpers.PrefHelper
import me.jfenn.attribouter.Attribouter

/**
 * Created by Kenneth on 24/7/2019.
 * for com.itachi1706.cheesecakeutilities in CheesecakeUtilities
 */
class GeneralSettingsActivity : AppCompatActivity() {
    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, GeneralPreferenceFragment())
                .commit()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean { return if (item?.itemId == android.R.id.home) { finish(); true } else super.onOptionsItemSelected(item); }

    class GeneralPreferenceFragment : EasterEggResMultiMusicPrefFragment() {

        private lateinit var sp: SharedPreferences
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.pref_general)

            SettingsInitializer().setFullscreen(true).explodeUpdaterSettings(activity, R.drawable.notification_icon, CommonVariables.BASE_SERVER_URL,
                    resources.getString(R.string.link_legacy), resources.getString(R.string.link_updates), this)
                    .setAboutApp(true) { Attribouter.from(context).show(); true }
                    .setIssueTracking(true, "https://itachi1706.atlassian.net/browse/CUTILAND")
                    .setBugReporting(true, "https://itachi1706.atlassian.net/servicedesk/customer/portal/3")
                    .setFDroidRepo(true, "fdroidrepos://fdroid.itachi1706.com/repo?fingerprint=B321F84BCAC7C296CF50923FF98965B11019BB5FD30C8B8F3A39F2F649AF9691")
                    .explodeInfoSettings(this)
            super.init()

            val bioPw: Preference? = findPreference("password_fp")
            sp = PrefHelper.getDefaultSharedPreferences(activity)
            updatePasswordViews(bioPw)

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
                updatePasswordViews(bioPw, newValue as Boolean, 0)
                true
            }

            findPreference<Preference>(BiometricCompatHelper.SCREEN_LOCK_ENABLED)?.setOnPreferenceChangeListener { _, newValue ->
                updatePasswordViews(bioPw, newValue as Boolean, 1)
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

        private fun updatePasswordViews(bioPw: Preference?, value: Boolean = BiometricCompatHelper.requireBiometricAuth(sp), type: Int = -1) {
            var isScreenLock = BiometricCompatHelper.isScreenLockProtectionEnabled(activity!!)
            var isBiometric = BiometricCompatHelper.requireBiometricAuth(sp)

            when (type) {
                0 -> isBiometric = value
                1 -> isScreenLock = value
            }
            var summary = "Unprotected"
            if (isScreenLock) {
                if (BiometricCompatHelper.isScreenLockEnabled(activity!!)) {
                    summary = "Protected with device screen lock"
                    if (isBiometric) {
                        if (BiometricCompatHelper.isBiometricRegistered(activity!!)) summary = "Protected with biometrics + screen lock"
                        else summary += " (No biometric data found on device)"
                    }
                } else summary = "Unprotected (No screen lock found)"
            } else if (isBiometric) {
                summary = if (!BiometricCompatHelper.isScreenLockEnabled(activity!!)) "Unprotected (No screen lock found)" // No Biometrics without a screen lock
                else if (BiometricCompatHelper.isBiometricRegistered(activity!!)) "Protected with biometrics"
                else "Unprotected (No biometric data found on device)"
            }

            bioPw?.summary = summary
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