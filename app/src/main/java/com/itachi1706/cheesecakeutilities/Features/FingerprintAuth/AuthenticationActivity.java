package com.itachi1706.cheesecakeutilities.Features.FingerprintAuth;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.itachi1706.cheesecakeutilities.R;

import java.security.KeyStore;
import java.security.KeyStoreException;

public class AuthenticationActivity extends AppCompatActivity {

    private KeyStore keyStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
        } catch (KeyStoreException e) {
            e.printStackTrace();
            Log.e("Authentication", "CRITICAL: No Keystore, Presuming OK to go");
            setResult(RESULT_OK);
            finish();
        }


    }
}
