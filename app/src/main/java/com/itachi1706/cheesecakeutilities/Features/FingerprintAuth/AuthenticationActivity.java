package com.itachi1706.cheesecakeutilities.Features.FingerprintAuth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.itachi1706.appupdater.extlib.fingerprint.FingerprintDialog;
import com.itachi1706.cheesecakeutilities.R;

import java.security.InvalidKeyException;

import androidx.appcompat.app.AppCompatActivity;

public class AuthenticationActivity extends AppCompatActivity implements FingerprintDialog.Callback {

    SharedPreferences sp;
    FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        if (!PasswordHelper.hasPassword(sp)) {
            // No password, treat as authenticated
            Log.i("Authentication", "No Password Found. Presuming Authenticated");
            setResult(RESULT_OK);
            finish();
        }
        // TODO: Migrate to FP Authentication with BiometricPromptCompat
        PasswordHelper.migrateToBiometric(sp);
        FingerprintDialog.show(this, getString(R.string.app_name), 10);
    }

    @Override
    public void onFingerprintDialogAuthenticated() {
        Toast.makeText(this, R.string.dialog_authenticated, Toast.LENGTH_LONG).show();
        Log.i("Authentication", "User Authenticated");
        setResult(RESULT_OK);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, null);
        finish();
    }

    @Override
    public void onFingerprintDialogVerifyPassword(final FingerprintDialog dialog, final String password) {
        boolean result;
        try {
            result = PasswordHelper.verifyPassword(sp, password);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            Toast.makeText(this, "Invalid Password saved. Your data may have been tampered or corrupted. Please clear app data if you cannot advance", Toast.LENGTH_LONG).show();
            Intent intent = new Intent();
            intent.putExtra("message", "Invalid Password");
            setResult(RESULT_CANCELED, intent);
            finish();
            return;
        }
        dialog.notifyPasswordValidation(result);
    }

    @Override
    public void onFingerprintDialogStageUpdated(FingerprintDialog dialog, FingerprintDialog.Stage stage) {
        Log.d("Authentication", "Dialog stage: " + stage.name());
    }

    @Override
    public void onFingerprintDialogCancelled() {
        Toast.makeText(this, R.string.dialog_cancelled, Toast.LENGTH_SHORT).show();
        Log.i("Authentication", "User Cancelled Authentication");
        Intent intent = new Intent();
        intent.putExtra("message", "Dialog Cancelled");
        setResult(RESULT_CANCELED, intent);
        finish();
    }
}
