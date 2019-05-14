package com.itachi1706.cheesecakeutilities.Modules.CEPASReader;

import android.os.Bundle;

import com.itachi1706.cepaslib.fragment.CEPASCardScanFragment;
import com.itachi1706.cheesecakeutilities.BaseModuleActivity;

public class CEPASActivity extends BaseModuleActivity {

    public String getHelpDescription() {
        return "A Card reader that supports CEPAS cards. Click on Supported Cards to learn more";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportFragmentManager().findFragmentById(android.R.id.content)==null) {
            getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content, new CEPASCardScanFragment())
                    .commit();
        }
    }
}
