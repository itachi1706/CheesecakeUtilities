package com.itachi1706.cheesecakeutilities;

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

import com.itachi1706.cheesecakeutilities.RecyclerAdapters.MainMenuAdapter;
import com.itachi1706.cheesecakeutilities.Updater.AppUpdateChecker;
import com.itachi1706.cheesecakeutilities.Util.NotifyUserUtil;

public class MainMenuActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = (RecyclerView) findViewById(R.id.main_menu_recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // Set up layout
        String[] menuitems = getResources().getStringArray(R.array.mainmenu);
        MainMenuAdapter adapter = new MainMenuAdapter(this, menuitems);
        recyclerView.setAdapter(adapter);

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        Log.i("Updater", "Checking for new updates...");
        new AppUpdateChecker(this, sp, true).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
                NotifyUserUtil.createShortToast(this, "Settings has not been implemented yet. Hold tight!");
                return true;
            case R.id.exit:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
