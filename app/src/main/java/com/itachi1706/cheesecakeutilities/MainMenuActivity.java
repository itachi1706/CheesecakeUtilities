package com.itachi1706.cheesecakeutilities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.itachi1706.appupdater.AppUpdateInitializer;
import com.itachi1706.cheesecakeutilities.Features.FingerprintAuth.AuthenticationActivity;
import com.itachi1706.cheesecakeutilities.RecyclerAdapters.MainMenuAdapter;
import com.itachi1706.cheesecakeutilities.Util.CommonMethods;
import com.itachi1706.cheesecakeutilities.Util.CommonVariables;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.fabric.sdk.android.Fabric;

public class MainMenuActivity extends AppCompatActivity {

    SharedPreferences sp;
    MainMenuAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crashlytics crashlyticsKit = new Crashlytics.Builder().core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build()).build();
        Fabric.with(this, crashlyticsKit);
        setContentView(R.layout.activity_main_menu);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        sp = PreferenceManager.getDefaultSharedPreferences(this);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.main_menu_recycler_view);
        if (recyclerView != null) {
            recyclerView.setHasFixedSize(true);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());

            // Set up layout
            List<String> menuitems = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.mainmenu)));
            adapter = new MainMenuAdapter(this, menuitems);
            recyclerView.setAdapter(adapter);
        }

        new AppUpdateInitializer(this, sp, R.mipmap.ic_launcher, CommonVariables.BASE_SERVER_URL).checkForUpdate(true);

        // Do Authentication
        if (CommonMethods.isGlobalLocked(sp)) startActivityForResult(new Intent(this, AuthenticationActivity.class), REQUEST_AUTH);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Remove hidden items
        List<String> menuitems = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.mainmenu)));
        List<String> hiddenitems = new ArrayList<>(Arrays.asList(sp.getString("utilHidden", "").split("\\|\\|\\|")));
        for (String s : hiddenitems) {
            menuitems.remove(s);
        }

        adapter.updateList(menuitems);
        adapter.notifyDataSetChanged();

    }

    private final int REQUEST_AUTH = 2;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_AUTH) {
            if (resultCode == RESULT_CANCELED) {
                finish();
            }
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
