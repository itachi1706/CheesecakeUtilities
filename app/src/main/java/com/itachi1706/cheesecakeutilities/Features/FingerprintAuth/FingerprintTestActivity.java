package com.itachi1706.cheesecakeutilities.Features.FingerprintAuth;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.digitus.Digitus;
import com.afollestad.digitus.DigitusCallback;
import com.afollestad.digitus.DigitusErrorType;
import com.afollestad.digitus.FingerprintDialog;
import com.itachi1706.cheesecakeutilities.R;

public class FingerprintTestActivity extends AppCompatActivity implements DigitusCallback, FingerprintDialog.Callback {

    private TextView mStatus;
    private Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fingerprint_test);

        mStatus = (TextView) findViewById(R.id.status);
        mButton = (Button) findViewById(R.id.beginAuthentication);

        findViewById(R.id.useDialog).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FingerprintDialog.show(FingerprintTestActivity.this, getString(R.string.app_name), 69);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Digitus.init(this, getString(R.string.app_name), 69, this);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Starts listening for a fingerprint
                Digitus.get().startListening();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Calling this method automatically makes a call to stopListening() if necessary
        Digitus.deinit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Digitus.get().handleResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onDigitusReady(Digitus digitus) {
        mStatus.setText(R.string.status_ready);
        mButton.setEnabled(true);
    }

    @Override
    public void onDigitusListening(boolean newFingerprint) {
        mButton.setText(R.string.stop_listening);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Stop listening
                Digitus.get().stopListening();
                mStatus.setText(R.string.status_ready);
                // Clicking the button again will start listening again
                mButton.setText(R.string.start_listening);
                mButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Digitus.get().startListening();
                    }
                });
            }
        });

        mStatus.setText(newFingerprint ? R.string.status_listening_new : R.string.status_listening);
    }

    @Override
    public void onDigitusAuthenticated(Digitus digitus) {
        // Update status message,
        mStatus.setText(R.string.status_authenticated);

        // Setup button to start listening again
        mButton.setText(R.string.start_listening);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Digitus.get().startListening();
            }
        });
    }

    @Override
    public void onDigitusError(Digitus digitus, DigitusErrorType type, Exception e) {
        // You could choose to do something different in each of these cases
        switch (type) {
            case FINGERPRINT_NOT_RECOGNIZED:
                mStatus.setText(getString(R.string.status_error, e.getMessage()));
                break;
            case FINGERPRINTS_UNSUPPORTED:
                mStatus.setText(getString(R.string.status_error, e.getMessage()));
                break;
            case HELP_ERROR:
                mStatus.setText(getString(R.string.status_error, e.getMessage()));
                break;
            case PERMISSION_DENIED:
                mStatus.setText(getString(R.string.status_error, e.getMessage()));
                break;
            case REGISTRATION_NEEDED:
                mStatus.setText(getString(R.string.status_error, e.getMessage()));
                mButton.setText(R.string.open_security_settings);
                mButton.setEnabled(true);
                mButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mButton.setText(R.string.start_listening);
                        Digitus.get().openSecuritySettings();
                    }
                });
                break;
            case UNRECOVERABLE_ERROR:
                mStatus.setText(getString(R.string.status_error, e.getMessage()));
                break;
        }
    }

    @Override
    public void onFingerprintDialogAuthenticated() {
        Toast.makeText(this, R.string.dialog_authenticated, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onFingerprintDialogVerifyPassword(final FingerprintDialog dialog, final String password) {
        // Simulate server contact
        mButton.postDelayed(new Runnable() {
            @Override
            public void run() {
                dialog.notifyPasswordValidation(password.equals("password"));
            }
        }, 1500);
    }

    @Override
    public void onFingerprintDialogStageUpdated(FingerprintDialog dialog, FingerprintDialog.Stage stage) {
        Log.d("Digitus", "Dialog stage: " + stage.name());
    }

    @Override
    public void onFingerprintDialogCancelled() {
        Toast.makeText(this, R.string.dialog_cancelled, Toast.LENGTH_SHORT).show();
    }
}
