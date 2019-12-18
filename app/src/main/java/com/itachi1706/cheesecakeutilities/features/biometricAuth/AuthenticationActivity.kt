package com.itachi1706.cheesecakeutilities.features.biometricAuth

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricConstants.*
import androidx.biometric.BiometricPrompt
import com.google.firebase.analytics.FirebaseAnalytics
import com.itachi1706.cheesecakeutilities.R
import com.itachi1706.cheesecakeutilities.util.LogHelper

/**
 * Created by Kenneth on 18/12/2019.
 * for com.itachi1706.cheesecakeutilities.features.biometricAuth in CheesecakeUtilities
 */
class AuthenticationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)

        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        migrateToBiometric(sp)
        if (BiometricCompatHelper.isBiometricRegistered(this) && BiometricCompatHelper.requireBiometricAuth(sp)) {
            // Has Biometrics and requested for biometric auth
            val executor = BiometricCompatHelper.biometricExecutor
            val p = BiometricPrompt(this, executor, callback)
            val promptInfo = BiometricCompatHelper.createPromptObject(requireConfirmation = false)
            p.authenticate(promptInfo)
        } else if (BiometricCompatHelper.isScreenLockProtectionEnabled(this) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) authWithScreenLock()
        else {
            // No biometric data, treat as authenticated
            LogHelper.i(TAG, "No Biometric Authentication FOund. Presuming Authenticated")
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == INTENT_AUTH_SL) {
            if (resultCode == Activity.RESULT_OK) authenticatedMessage()
            else {
                val intent = Intent().apply { putExtra(INTENT_MSG, "Authentication Error: ACTION_FAILED_OR_CANCELLED") }
                Toast.makeText(this, R.string.dialog_cancelled, Toast.LENGTH_SHORT).show()
                LogHelper.e(TAG, "Authentication Error ($resultCode): ACTION_FAILED_OR_CANCELLED")
                setResult(Activity.RESULT_CANCELED, intent)
                finish()
            }
        } else super.onActivityResult(requestCode, resultCode, data)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun authWithScreenLock() {
        // TODO (CUTILAND-402): Seems like it could also be caused by an android bug on Android 10 devices. See a/145231213 and a/142740104
        // TODO (CUTILAND-389): Deprecate with Android 10 targetSDK and compileSDK. See CUTILAND-402
        val km  = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager?
        if (km == null) {
            Toast.makeText(this, R.string.dialog_cancelled, Toast.LENGTH_SHORT).show()
            val intent = Intent().apply { putExtra(INTENT_MSG, "Authentication Error: KeyGuard is not ready") }
            LogHelper.e(TAG, "Authentication Error (KEYGUARD NULL): KeyGuard is not ready")
            setResult(Activity.RESULT_CANCELED, intent)
            finish()
            return
        }
        val s1Intent = km.createConfirmDeviceCredentialIntent("Application Locked", "Unlock your screen again to continue")
        startActivityForResult(s1Intent, INTENT_AUTH_SL)
    }

    private fun migrateToBiometric(sp: SharedPreferences) {
        if (sp.contains(SP_FP_LEGACY_ENABLED)) {
            val edit = sp.edit()
            edit.putBoolean(BiometricCompatHelper.APP_BIOMETRIC_COMPAT_ENABLED, true) // Insert new key
            edit.remove(SP_FP_LEGACY_ENABLED).remove(SP_FP_LEGACY_KEY) // Delete old keys
            edit.apply()
        }
    }

    private fun authenticatedMessage() {
        val mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        Toast.makeText(this, R.string.dialog_authenticated, Toast.LENGTH_LONG).show()
        LogHelper.i(TAG, "User Authenticated")
        setResult(Activity.RESULT_OK)
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, null)
        finish()
    }

    private fun throwError(intent: Intent, message: String, logMessage: String, toFinish: Boolean = true): Intent {
        Toast.makeText(this@AuthenticationActivity, R.string.dialog_cancelled, Toast.LENGTH_SHORT).show()
        LogHelper.i(TAG, logMessage)
        intent.putExtra(INTENT_MSG, message)
        if (toFinish) {
            setResult(Activity.RESULT_CANCELED, intent)
            finish()
        }
        return intent
    }

    private val callback = object: BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            LogHelper.d(TAG, "onAuthenticationError(): $errorCode ($errString)")
            runOnUiThread {
                var intent = Intent()
                when (errorCode) {
                    ERROR_NEGATIVE_BUTTON, ERROR_USER_CANCELED, ERROR_CANCELED -> {
                        throwError(intent, "Dialog Cancelled", "User Cancelled Authentication")
                    }
                    ERROR_LOCKOUT, ERROR_LOCKOUT_PERMANENT -> {
                        if (BiometricCompatHelper.isScreenLockProtectionEnabled(this@AuthenticationActivity) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                            authWithScreenLock()
                        else {
                            intent = throwError(intent, "Lockout", "User Lock out", false)
                            AlertDialog.Builder(this@AuthenticationActivity).setTitle("Biometric sensors disabled (Locked out)")
                                    .setMessage("You have attempted and failed biometric authentication too many times and your biometric sensors has been disabled. \n\n" +
                                            "Please re-authenticate by unlocking or rebooting your phone again or disable biometric authentication on your device")
                                    .setCancelable(false).setPositiveButton(R.string.dialog_action_positive_close) { _, _ ->
                                        setResult(Activity.RESULT_CANCELED, intent)
                                        finish()
                                    }.show()
                        }
                    }
                    else -> {
                        throwError(intent, "Authentication Error: $errString", "Authentication Error ($errorCode): $errString")
                    }
                }
            }
        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            LogHelper.d(TAG, "onAuthenticationSucceeded()")
            runOnUiThread { authenticatedMessage() }
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            LogHelper.i(TAG, "Wrong Biometric detected")
        }
    }

    companion object {
        const val INTENT_AUTH_SL = 2
        private const val TAG = "Authentication"
        private const val INTENT_MSG = "message"
        private const val SP_FP_LEGACY_ENABLED = "app_pw_unlock_enc"
        private const val SP_FP_LEGACY_KEY = "app_pw_unlock_key"
    }
}