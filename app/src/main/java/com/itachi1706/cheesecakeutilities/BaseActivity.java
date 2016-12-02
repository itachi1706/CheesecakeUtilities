package com.itachi1706.cheesecakeutilities;

import android.app.AlertDialog;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.itachi1706.cheesecakeutilities.Features.FingerprintAuth.AuthenticationActivity;
import com.itachi1706.cheesecakeutilities.Util.CommonMethods;

/**
 * Created by Kenneth on 13/8/2016.
 * for com.itachi1706.cheesecakeutilities in CheesecakeUtilities
 */
public abstract class BaseActivity extends AppCompatActivity {
    public abstract String getHelpDescription();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null && getSupportActionBar().isShowing()) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        String menuitem = this.getIntent().hasExtra("menuitem") ? this.getIntent().getExtras().getString("menuitem", "") : "";
        boolean checkGlobal = this.getIntent().hasExtra("globalcheck") && this.getIntent().getExtras().getBoolean("globalcheck");
        boolean authagain = !this.getIntent().hasExtra("authagain") || this.getIntent().getExtras().getBoolean("authagain");
        if (!authagain) return;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        if (!(menuitem == null || menuitem.isEmpty() || menuitem.equals(""))) {
            if (!CommonMethods.isGlobalLocked(sp) && CommonMethods.isUtilityLocked(sp, menuitem)) {
                Log.i("Authentication", "Requesting Utility Authentication for " + menuitem);
                startActivityForResult(new Intent(this, AuthenticationActivity.class), REQUEST_AUTH);
            }
        }
        if (checkGlobal) {
            if (CommonMethods.isGlobalLocked(sp)) {
                Log.i("Authentication", "Requesting Authentication as app is locked globally");
                startActivityForResult(new Intent(this, AuthenticationActivity.class), REQUEST_AUTH_GLOBAL);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.modules_generic_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                new AlertDialog.Builder(this)
                        .setMessage(getHelpDescription())
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.ok, null).show();
                return true;
            case R.id.settings:
                startActivity(new Intent(this, GeneralSettingsActivity.class));
                return true;
            case android.R.id.home:
            case R.id.exit:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private final int REQUEST_AUTH = 3;
    private final int REQUEST_AUTH_GLOBAL = 4;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_AUTH) {
            if (resultCode == RESULT_CANCELED) {
                finish();
            }
        }
        if (requestCode == REQUEST_AUTH_GLOBAL) {
            if (resultCode == RESULT_CANCELED) {
                finishAffinity();
            } else if (resultCode == RESULT_OK) {
                TaskStackBuilder.create(this)
                        .addParentStack(MainMenuActivity.class)
                        .addNextIntent(new Intent(this, MainMenuActivity.class).putExtra("authagain", false))
                        .addNextIntent(new Intent(this, this.getClass()).putExtra("authagain", false))
                .startActivities();
            }
        }
    }

}
