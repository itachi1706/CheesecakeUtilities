package com.itachi1706.cheesecakeutilities.Modules.CEPASReader;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;

import com.codebutler.farebot.app.feature.main.MainActivity;
import com.itachi1706.cheesecakeutilities.BaseModuleActivity;

public class CEPASActivity extends BaseModuleActivity {

    public String getHelpDescription() {
        return "A Card reader that supports CEPAS cards. Click on Supported Cards to learn more";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO: Reimplement
        /*if (getSupportFragmentManager().findFragmentById(android.R.id.content)==null) {
            getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content, new CEPASCardScanFragment())
                    .commit();
        }*/

        if (NfcAdapter.getDefaultAdapter(this) == null)
            new AlertDialog.Builder(this).setTitle("NFC Unavailable").setMessage("NFC is not available on this device. This utility will now exit")
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> finish()).setCancelable(false).show();
        else
            startActivity(new Intent(this, MainActivity.class));
    }
}
