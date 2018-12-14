package com.itachi1706.cheesecakeutilities.Features.FingerprintAuth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.itachi1706.cheesecakeutilities.R;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometrics.BiometricPrompt;

public class AuthenticationActivity extends AppCompatActivity {

    SharedPreferences sp;
    FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        PasswordHelper.migrateToBiometric(sp);
        if (BiometricCompatHelper.isBiometricFPRegistered(this) && BiometricCompatHelper.requireFPAuth(sp)) {
            // Has Fingerprint and requested for fingerprint auth
            BiometricPrompt.PromptInfo promptInfo = BiometricCompatHelper.createPromptObject();
            BiometricPrompt p = new BiometricPrompt(this, BiometricCompatHelper.getBiometricExecutor(), callback);
            p.authenticate(promptInfo);
        }
    }

    private BiometricPrompt.AuthenticationCallback callback = new BiometricPrompt.AuthenticationCallback() {
        @Override
        public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
            super.onAuthenticationError(errorCode, errString);
            Toast.makeText(getApplicationContext(), R.string.dialog_cancelled, Toast.LENGTH_SHORT).show();
            Log.e("Authentication", "Authentication Error (" + errorCode + "): " + errString);
            Intent intent = new Intent();
            intent.putExtra("message", "Auth Error");
            setResult(RESULT_CANCELED, intent);
            finish();
        }

        @Override
        public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
            super.onAuthenticationSucceeded(result);
            Toast.makeText(getApplicationContext(), R.string.dialog_authenticated, Toast.LENGTH_LONG).show();
            Log.i("Authentication", "User Authenticated");
            setResult(RESULT_OK);
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, null);
            finish();
        }

        @Override
        public void onAuthenticationFailed() {
            super.onAuthenticationFailed();
            Toast.makeText(getApplicationContext(), R.string.dialog_cancelled, Toast.LENGTH_SHORT).show();
            Log.i("Authentication", "User Cancelled Authentication");
            Intent intent = new Intent();
            intent.putExtra("message", "Dialog Cancelled");
            setResult(RESULT_CANCELED, intent);
            finish();
        }
    };
}
