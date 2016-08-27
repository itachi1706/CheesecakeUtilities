package com.itachi1706.cheesecakeutilities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.itachi1706.cheesecakeutilities.RecyclerAdapters.StringRecyclerAdapter;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ORDActivity extends BaseActivity {

    RecyclerView recyclerView;
    TextView ordCounter, ordDaysLabel, ordProgress;
    CircularProgressBar progressBar;
    int animationDuration = 2500; // ms
    long ordDays, ptpDays, popDays;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ord);

        recyclerView = (RecyclerView) findViewById(R.id.ord_recycler_view);
        ordDaysLabel = (TextView) findViewById(R.id.ord_days_counter);
        ordCounter = (TextView) findViewById(R.id.ord_counter);
        ordProgress = (TextView) findViewById(R.id.ord_precentage);
        progressBar = (CircularProgressBar)  findViewById(R.id.ord_progressbar);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (recyclerView != null) {
            recyclerView.setHasFixedSize(true);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());

            // Set up layout
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            long ord = sp.getLong(ORDSettingsActivity.SP_ORD, 0);
            long ptp = sp.getLong(ORDSettingsActivity.SP_PTP, 0);
            long pop = sp.getLong(ORDSettingsActivity.SP_POP, 0);
            long enlist = sp.getLong(ORDSettingsActivity.SP_ENLIST, 0);
            long currentTime = System.currentTimeMillis();
            List<String> menuItems = new ArrayList<>();
            if (ptp != 0) {
                // Non Enhanced Batch
                if (currentTime > ptp) {
                    // PTP LOH
                    menuItems.add("PTP Phase Ended");
                } else {
                    long duration = ptp - currentTime;
                    ptpDays = TimeUnit.MILLISECONDS.toDays(duration) + 1;
                    menuItems.add(getResources().getQuantityString(R.plurals.ord_days_countdown, (int) ptpDays, ptpDays, "Start of BMT Phase"));
                }
            }

            if (pop != 0) {
                if (currentTime > pop) {
                    // POP LOH
                    menuItems.add("POP LOH");
                } else {
                    long duration = pop - currentTime;
                    popDays = TimeUnit.MILLISECONDS.toDays(duration) + 1;
                    menuItems.add(getResources().getQuantityString(R.plurals.ord_days_countdown, (int) popDays, popDays, "POP"));
                }
            } else {
                menuItems.add("POP Date not defined. Please define in settings");
            }

            if (ord != 0) {
                if (currentTime > ord) {
                    // ORD LOH
                    ordDaysLabel.setText(getResources().getQuantityString(R.plurals.ord_days, 0));
                    ordCounter.setText("ORD");
                    progressBar.setProgressWithAnimation(100, animationDuration);
                } else {
                    long duration = ord - currentTime;
                    ordDays = TimeUnit.MILLISECONDS.toDays(duration) + 1;
                    ordDaysLabel.setText(getResources().getQuantityString(R.plurals.ord_days, (int) ordDays));
                    ordCounter.setText(ordDays + "");

                    double difference = ((TimeUnit.MILLISECONDS.toDays(currentTime-enlist)) / (double)(TimeUnit.MILLISECONDS.toDays(ord - enlist))) * 100.0;
                    ordProgress.setText((Math.round(difference * 100.0)/100.0) + "% completed");
                    progressBar.setProgressWithAnimation((float) difference, animationDuration);
                }
            } else {
                menuItems.add("ORD Date not defined. Please define in settings");
            }


            StringRecyclerAdapter adapter = new StringRecyclerAdapter(menuItems);
            recyclerView.setAdapter(adapter);
        }
    }

    @Override
    String getHelpDescription() {
        return "ORD Calculator (WIP)";
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.modules_ord, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.modify_settings: startActivity(new Intent(getApplicationContext(), ORDSettingsActivity.class)); return true;
            default: return super.onOptionsItemSelected(item);
        }
    }
}
