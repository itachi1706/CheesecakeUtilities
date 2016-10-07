package com.itachi1706.cheesecakeutilities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

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

}
