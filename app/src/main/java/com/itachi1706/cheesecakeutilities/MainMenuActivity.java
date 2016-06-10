package com.itachi1706.cheesecakeutilities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.itachi1706.appupdater.AppUpdateChecker;
import com.itachi1706.cheesecakeutilities.RecyclerAdapters.MainMenuAdapter;
import com.itachi1706.appupdater.Util.UpdaterHelper;
import com.itachi1706.cheesecakeutilities.Util.CommonVariables;

import io.fabric.sdk.android.Fabric;

public class MainMenuActivity extends AppCompatActivity {

    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crashlytics crashlyticsKit = new Crashlytics.Builder().core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build()).build();
        Fabric.with(this, crashlyticsKit);
        setContentView(R.layout.activity_main_menu);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.main_menu_recycler_view);
        if (recyclerView != null) {
            recyclerView.setHasFixedSize(true);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());

            // Set up layout
            String[] menuitems = getResources().getStringArray(R.array.mainmenu);
            MainMenuAdapter adapter = new MainMenuAdapter(this, menuitems);
            recyclerView.setAdapter(adapter);
        }

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        if (UpdaterHelper.canCheckUpdate(sp, this)) {
            Log.i("Updater", "Checking for new updates...");
            new AppUpdateChecker(this, sp, true, R.mipmap.ic_launcher, CommonVariables.BASE_SERVER_URL).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                startActivity(new Intent(this, GeneralSettingsActivity.class));
                return true;
            case R.id.exit:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
