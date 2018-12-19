package com.itachi1706.cheesecakeutilities.Features.FingerprintAuth

import android.Manifest
import android.annotation.TargetApi
import android.app.KeyguardManager
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.preference.PreferenceManager
import android.util.Log
import androidx.biometrics.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import java.util.concurrent.Executor

/**
 * Created by Kenneth on 14/12/2018.
 * for com.itachi1706.appupdater.extlib.fingerprint in CheesecakeUtilities
 */
class BiometricCompatHelper private constructor() {

    init {
        throw IllegalStateException("Utility class. Do not instantiate")
    }

    companion object {

        const val APP_BIOMETRIC_COMPAT_ENABLED = "app_bio_compat_enable"
        const val SCREEN_LOCK_ENABLED = "app_screen_lock_protection"

        /**
         * Check if we need to authenticate with Fingerprint through the BiometricPromptCompat API
         * @param sp Shared Preference Object
         * @return true if require fingerprint, false otherwise
         */
        fun requireFPAuth(sp: SharedPreferences): Boolean {
            return sp.getBoolean(APP_BIOMETRIC_COMPAT_ENABLED, false)
        }

        @TargetApi(Build.VERSION_CODES.M)
        @Throws(NullPointerException::class)
        fun isBiometricFPRegistered(context: Context): Boolean {
            if (!isBiometricAuthFPAvailable(context)) return false


            val km = context.getSystemService(KeyguardManager::class.java)
            if (km == null) {
                Log.e("BioCompat", "Keyguard died!")
                return false
            }
            val fpCompat = FingerprintManagerCompat.from(context)
            return km.isKeyguardSecure && fpCompat.hasEnrolledFingerprints()
        }

        val biometricExecutor: Executor
            get() = Executor { it.run() }

        private fun isBiometricAuthFPAvailable(context: Context): Boolean {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return false
            val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                val granted2 = ContextCompat.checkSelfPermission(context, Manifest.permission.USE_BIOMETRIC)
                if (granted2 != PackageManager.PERMISSION_GRANTED) return false
            }
            if (granted != PackageManager.PERMISSION_GRANTED) return false

            val fpCompat = FingerprintManagerCompat.from(context)
            return fpCompat.isHardwareDetected && fpCompat.hasEnrolledFingerprints()
        }

        @JvmOverloads
        fun createPromptObject(title: String = "Sign In", subtitle: String? = null, description: String = "Confirm fingerprint to continue", negativeBtn: String = "Cancel"): BiometricPrompt.PromptInfo {
            return BiometricPrompt.PromptInfo.Builder().setTitle(title).setSubtitle(subtitle).setDescription(description).setNegativeButtonText(negativeBtn).build()
        }

        fun isScreenLockEnabled(context: Context): Boolean {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return false // Not supported prior to Lollipop

            val km = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            if (km == null) {
                Log.e("BioCompat", "Keyguard died!")
                return false
            }

            return km.isKeyguardSecure
        }

        fun isScreenLockProtectionEnabled(context: Context): Boolean {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return false // Not supported prior to Lollipop

            val sp = PreferenceManager.getDefaultSharedPreferences(context)
            return if (!sp.contains(SCREEN_LOCK_ENABLED) || !sp.getBoolean(SCREEN_LOCK_ENABLED, false)) false else isScreenLockEnabled(context)
        }
    }
}