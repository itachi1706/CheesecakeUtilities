package com.itachi1706.cheesecakeutilities.Features.FingerprintAuth;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.itachi1706.cheesecakeutilities.R;

import java.util.concurrent.Executor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometrics.BiometricConstants;
import androidx.biometrics.BiometricPrompt;

public class AuthenticationActivity extends AppCompatActivity {

    SharedPreferences sp;
    FirebaseAnalytics mFirebaseAnalytics;
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mContext = this;

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        migrateToBiometric();
        if (BiometricCompatHelper.isBiometricFPRegistered(this) && BiometricCompatHelper.requireFPAuth(sp)) {
            // Has Fingerprint and requested for fingerprint auth
            Executor executor = BiometricCompatHelper.getBiometricExecutor();
            BiometricPrompt p = new BiometricPrompt(this, executor, callback);
            BiometricPrompt.PromptInfo promptInfo = BiometricCompatHelper.createPromptObject();
            p.authenticate(promptInfo);
        } else {
            // No biometric data, treat as authenticated
            Log.i("Authentication", "No Biometric Authentication Found. Presuming Authenticated");
            setResult(RESULT_OK);
            finish();
        }
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
                        Log.i("Authentication", "User Cancelled Authentication");
                        intent.putExtra("message", "Dialog Cancelled");
                        setResult(RESULT_CANCELED, intent);
                        finish();
                        return;
                    case BiometricConstants.ERROR_LOCKOUT:
                    case BiometricConstants.ERROR_LOCKOUT_PERMANENT:
                        Toast.makeText(mContext, R.string.dialog_cancelled, Toast.LENGTH_SHORT).show();
                        Log.i("Authentication", "User Lock out");
                        intent.putExtra("message", "Lockout");
                        new AlertDialog.Builder(mContext).setTitle("Fingerprint sensor disabled (Locked out)")
                                .setMessage("You have scanned an invalid fingerprint too many times and your fingerprint sensor has been disabled. \n\n" +
                                        "Please re-authenticate by unlocking or rebooting your phone again or disable fingerprints on your device")
                                .setCancelable(false).setPositiveButton(R.string.dialog_action_positive_close, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                setResult(RESULT_CANCELED, intent);
                                finish();
                            }
                        }).show();
                        return;
                    default:
                        Toast.makeText(mContext, R.string.dialog_cancelled, Toast.LENGTH_SHORT).show();
                        Log.e("Authentication", "Authentication Error (" + errorCode + "): " + errString);
                        intent.putExtra("message", "Authentication Error: " + errString);
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
                Log.i("Authentication", "User Authenticated");
                setResult(RESULT_OK);
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, null);
                finish();
            });
        }

        @Override
        public void onAuthenticationFailed() {
            super.onAuthenticationFailed();
            Log.i("Authentication", "Wrong Biometric detected");
        }
    };
}
