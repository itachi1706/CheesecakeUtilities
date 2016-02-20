package com.itachi1706.cheesecakeutilities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.itachi1706.cheesecakeutilities.RecyclerAdapters.MainMenuAdapter;
import com.itachi1706.cheesecakeutilities.RecyclerAdapters.StringRecyclerAdapter;
import com.itachi1706.cheesecakeutilities.Updater.AppUpdateChecker;

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
        MainMenuAdapter adapter = new MainMenuAdapter(menuitems);
        recyclerView.setAdapter(adapter);

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        Log.i("Updater", "Checking for new updates...");
        new AppUpdateChecker(this, sp, true).execute();
    }
}
