package com.itachi1706.cheesecakeutilities.Features.FingerprintAuth;

import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.itachi1706.cheesecakeutilities.R;

import java.util.concurrent.Executor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometrics.BiometricConstants;
import androidx.biometrics.BiometricPrompt;

public class AuthenticationActivity extends AppCompatActivity {

    SharedPreferences sp;
    FirebaseAnalytics mFirebaseAnalytics;
    Context mContext;

    public static final int INTENT_AUTH_SL = 2;

    private static final String TAG = "Authentication", INTENT_MSG = "message";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mContext = this;

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        migrateToBiometric();
        if (BiometricCompatHelper.Companion.isBiometricFPRegistered(this) && BiometricCompatHelper.Companion.requireFPAuth(sp)) {
            // Has Fingerprint and requested for fingerprint auth
            Executor executor = BiometricCompatHelper.Companion.getBiometricExecutor();
            BiometricPrompt p = new BiometricPrompt(this, executor, callback);
            BiometricPrompt.PromptInfo promptInfo = BiometricCompatHelper.Companion.createPromptObject();
            p.authenticate(promptInfo);
        } else if (BiometricCompatHelper.Companion.isScreenLockProtectionEnabled(this) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            authWithScreenLock();
        } else {
            // No biometric data, treat as authenticated
            Log.i(TAG, "No Biometric Authentication Found. Presuming Authenticated");
            setResult(RESULT_OK);
            finish();
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private void authWithScreenLock() {
        KeyguardManager km = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        if (km == null) {
            Toast.makeText(mContext, R.string.dialog_cancelled, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();
            Log.e(TAG, "Authentication Error (KEYGUARD NULL): KeyGuard is not ready");
            intent.putExtra(INTENT_MSG, "Authentication Error: KeyGuard is not ready");
            setResult(RESULT_CANCELED, intent);
            finish();
            return;
        }
        Intent slIntent = km.createConfirmDeviceCredentialIntent("Sign In", "Unlock your screen again to continue");
        startActivityForResult(slIntent, INTENT_AUTH_SL);
    }

    private void migrateToBiometric() {
        if (sp.contains("app_pw_unlock_enc")) {
            SharedPreferences.Editor edit = sp.edit();
            edit.putBoolean(BiometricCompatHelper.APP_BIOMETRIC_COMPAT_ENABLED, true).apply();
            edit.remove("app_pw_unlock_enc").apply();
            edit.remove("app_pw_unlock_key").apply();
            edit.apply();
        }
    }

    private BiometricPrompt.AuthenticationCallback callback = new BiometricPrompt.AuthenticationCallback() {
        @SuppressLint("SwitchIntDef")
        @Override
        public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
            super.onAuthenticationError(errorCode, errString);
            runOnUiThread(() -> {
                Intent intent = new Intent();
                switch (errorCode) {
                    case BiometricConstants.ERROR_NEGATIVE_BUTTON:
                    case BiometricConstants.ERROR_USER_CANCELED:
                    case BiometricConstants.ERROR_CANCELED:
                        Toast.makeText(mContext, R.string.dialog_cancelled, Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "User Cancelled Authentication");
                        intent.putExtra(INTENT_MSG, "Dialog Cancelled");
                        setResult(RESULT_CANCELED, intent);
                        finish();
                        return;
                    case BiometricConstants.ERROR_LOCKOUT:
                    case BiometricConstants.ERROR_LOCKOUT_PERMANENT:
                        if (BiometricCompatHelper.Companion.isScreenLockProtectionEnabled(mContext) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            authWithScreenLock();
                        } else {
                            Toast.makeText(mContext, R.string.dialog_cancelled, Toast.LENGTH_SHORT).show();
                            Log.i(TAG, "User Lock out");
                            intent.putExtra(INTENT_MSG, "Lockout");
                            new AlertDialog.Builder(mContext).setTitle("Fingerprint sensor disabled (Locked out)")
                                    .setMessage("You have scanned an invalid fingerprint too many times and your fingerprint sensor has been disabled. \n\n" +
                                            "Please re-authenticate by unlocking or rebooting your phone again or disable fingerprints on your device")
                                    .setCancelable(false).setPositiveButton(R.string.dialog_action_positive_close, (dialog, which) -> {
                                setResult(RESULT_CANCELED, intent);
                                finish();
                            }).show();
                        }
                        return;
                    default:
                        Toast.makeText(mContext, R.string.dialog_cancelled, Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Authentication Error (" + errorCode + "): " + errString);
                        intent.putExtra(INTENT_MSG, "Authentication Error: " + errString);
                        setResult(RESULT_CANCELED, intent);
                        finish();
                }
            });
        }

        @Override
        public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
            super.onAuthenticationSucceeded(result);
            runOnUiThread(() -> {
                Toast.makeText(mContext, R.string.dialog_authenticated, Toast.LENGTH_LONG).show();
                Log.i(TAG, "User Authenticated");
                setResult(RESULT_OK);
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, null);
                finish();
            });
        }

        @Override
        public void onAuthenticationFailed() {
            super.onAuthenticationFailed();
            Log.i(TAG, "Wrong Biometric detected");
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == INTENT_AUTH_SL) {
            switch (resultCode) {
                case RESULT_OK:
                    Toast.makeText(mContext, R.string.dialog_authenticated, Toast.LENGTH_LONG).show();
                    Log.i(TAG, "User Authenticated");
                    setResult(RESULT_OK);
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, null);
                    finish();
                    break;
                default:
                    Intent intent = new Intent();
                    Toast.makeText(mContext, R.string.dialog_cancelled, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Authentication Error (" + resultCode + "): ACTION_FAILED_OR_CANCELLED");
                    intent.putExtra(INTENT_MSG, "Authentication Error: ACTION_FAILED_OR_CANCELLED");
                    setResult(RESULT_CANCELED, intent);
                    finish();
            }
        } else super.onActivityResult(requestCode, resultCode, data);
    }
}
