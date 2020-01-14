package com.itachi1706.cheesecakeutilities.features.biometricAuth

import android.annotation.TargetApi
import android.app.KeyguardManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.preference.PreferenceManager
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import com.itachi1706.helperlib.helpers.LogHelper
import java.util.concurrent.Executor

/**
 * Created by Kenneth on 14/12/2018.
 * for com.itachi1706.features.biometricAuth in CheesecakeUtilities
 */
class BiometricCompatHelper private constructor() {

    init {
        throw IllegalStateException("Utility class. Do not instantiate")
    }

    companion object {

        const val APP_BIOMETRIC_COMPAT_ENABLED = "app_bio_compat_enable"
        const val SCREEN_LOCK_ENABLED = "app_screen_lock_protection"

        /**
         * Check if we need to authenticate with Biometrics through the BiometricPromptCompat API
         * @param sp Shared Preference Object
         * @return true if require biometrics, false otherwise
         */
        fun requireBiometricAuth(sp: SharedPreferences): Boolean {
            return sp.getBoolean(APP_BIOMETRIC_COMPAT_ENABLED, false)
        }

        @TargetApi(Build.VERSION_CODES.M)
        @Throws(NullPointerException::class)
        fun isBiometricRegistered(context: Context): Boolean {
            if (!isBiometricAuthAvailable(context)) return false


            val km = context.getSystemService(KeyguardManager::class.java)
            if (km == null) {
                LogHelper.e("BioCompat", "Keyguard died!")
                return false
            }
            val biometricManager = BiometricManager.from(context)
            return km.isKeyguardSecure && biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS
        }

        val biometricExecutor: Executor
            get() = Executor { it.run() }

        private fun isBiometricAuthAvailable(context: Context): Boolean {
            return when (BiometricManager.from(context).canAuthenticate()) {
                BiometricManager.BIOMETRIC_SUCCESS, BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED, BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> true
                else -> false
            }
        }

        fun createPromptObject(title: String = "Application Locked", subtitle: String? = null, description: String = "Confirm biometrics to continue",
                               requireConfirmation: Boolean = true, negativeBtn: String = "Cancel"): BiometricPrompt.PromptInfo {
            return BiometricPrompt.PromptInfo.Builder().setTitle(title).setSubtitle(subtitle).setConfirmationRequired(requireConfirmation)
                    .setDescription(description).setNegativeButtonText(negativeBtn).build()
        }

        fun isScreenLockEnabled(context: Context): Boolean {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return false // Not supported prior to Lollipop
            return (context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager).isKeyguardSecure
        }

        fun isScreenLockProtectionEnabled(context: Context): Boolean {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return false // Not supported prior to Lollipop

            val sp = PreferenceManager.getDefaultSharedPreferences(context)
            return if (!sp.contains(SCREEN_LOCK_ENABLED) || !sp.getBoolean(SCREEN_LOCK_ENABLED, false)) false else isScreenLockEnabled(context)
        }
    }
}
