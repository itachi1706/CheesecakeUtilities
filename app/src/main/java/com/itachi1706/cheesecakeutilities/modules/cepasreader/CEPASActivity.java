package com.itachi1706.cheesecakeutilities.modules.cepasreader;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;

import com.itachi1706.cepaslib.app.feature.main.MainActivity;
import com.itachi1706.cepaslib.CEPASLibBuilder;
import com.itachi1706.cheesecakeutilities.BaseModuleActivity;
import com.itachi1706.cheesecakeutilities.R;
import com.itachi1706.cheesecakeutilities.UtilitySettingsActivity;

public class CEPASActivity extends BaseModuleActivity {

    public String getHelpDescription() {
        return "A Card reader that supports CEPAS cards. Click on Supported Cards to learn more";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (NfcAdapter.getDefaultAdapter(this) == null) {
            new AlertDialog.Builder(this).setTitle("NFC Unavailable").setMessage("NFC is not available on this device. This utility will now exit")
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> finish()).setCancelable(false).show();
        }
        CEPASLibBuilder.INSTANCE.setPreferenceClass(UtilitySettingsActivity.class);
        CEPASLibBuilder.INSTANCE.shouldShowAboutMenuItem(true);
        CEPASLibBuilder.INSTANCE.updateTitleBarColor(R.color.colorPrimary);
        CEPASLibBuilder.INSTANCE.updateAccentColor(R.color.colorAccent);
        CEPASLibBuilder.INSTANCE.updateErrorColor(R.color.colorAccent);
        CEPASLibBuilder.INSTANCE.setCustomTitle("CEPAS Card Reader");
        CEPASLibBuilder.INSTANCE.setHomeScreenWithBackButton(true);
        CEPASLibBuilder.INSTANCE.registerMenuHandler((item, context) -> {
            if (item.getItemId() == com.itachi1706.cepaslib.R.id.about) {
                new android.app.AlertDialog.Builder(context)
                        .setMessage(getHelpDescription())
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.ok, null).show();
            }
        });
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
