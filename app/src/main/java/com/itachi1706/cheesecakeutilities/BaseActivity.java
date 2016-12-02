package com.itachi1706.cheesecakeutilities;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
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
        if (!(menuitem == null || menuitem.isEmpty() || menuitem.equals(""))) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            if (!CommonMethods.isGlobalLocked(sp) && CommonMethods.isUtilityLocked(sp, menuitem)) {
                Log.i("Authentication", "Requesting Utility Authentication for " + menuitem);
                startActivityForResult(new Intent(this, AuthenticationActivity.class), REQUEST_AUTH);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_AUTH) {
            if (resultCode == RESULT_CANCELED) {
                finish();
            }
        }
    }

}
