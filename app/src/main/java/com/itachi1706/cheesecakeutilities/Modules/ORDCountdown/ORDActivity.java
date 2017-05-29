package com.itachi1706.cheesecakeutilities.Modules.ORDCountdown;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.github.lzyzsd.circleprogress.ArcProgress;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.itachi1706.cheesecakeutilities.BaseActivity;
import com.itachi1706.cheesecakeutilities.R;
import com.itachi1706.cheesecakeutilities.RecyclerAdapters.StringRecyclerAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ORDActivity extends BaseActivity {

    RecyclerView recyclerView;
    TextView ordCounter, ordDaysLabel, ordProgress;
    ArcProgress progressBar;
    long ordDays, ptpDays, popDays, pdoption;

    String firebaseHolidayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ord);

        recyclerView = (RecyclerView) findViewById(R.id.ord_recycler_view);
        ordDaysLabel = (TextView) findViewById(R.id.ord_days_counter);
        ordCounter = (TextView) findViewById(R.id.ord_counter);
        ordProgress = (TextView) findViewById(R.id.ord_precentage);
        progressBar = (ArcProgress)  findViewById(R.id.ord_progressbar);

        // Get Holiday List from Firebase
        FirebaseRemoteConfig firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        firebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);
        firebaseHolidayList = firebaseRemoteConfig.getString("ord_holidays");
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
            pdoption = sp.getLong(ORDSettingsActivity.SP_PAYDAY, -1);
            long currentTime = System.currentTimeMillis();
            List<String> menuItems = new ArrayList<>();
            if (ptp != 0) {
                // Normal Batch
                if (currentTime > ptp) {
                    menuItems.add("PTP Phase Ended"); // PTP LOH
                } else {
                    long duration = ptp - currentTime;
                    ptpDays = TimeUnit.MILLISECONDS.toDays(duration) + 1;
                    menuItems.add(getResources().getQuantityString(R.plurals.ord_days_countdown, (int) ptpDays, ptpDays, "Start of BMT Phase"));
                }
            }

            if (pop != 0) {
                if (currentTime > pop) {
                    menuItems.add("POP LOH"); // POP LOH
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
                    ordDaysLabel.setText("LOH");
                    ordCounter.setText("ORD");
                    ordProgress.setText("100% Completed");
                    progressBar.setProgress(100);
                } else {
                    long duration = ord - currentTime;
                    ordDays = TimeUnit.MILLISECONDS.toDays(duration) + 1;
                    ordDaysLabel.setText(getResources().getQuantityString(R.plurals.ord_days, (int) ordDays));
                    ordCounter.setText(ordDays + "");

                    double difference = ((TimeUnit.MILLISECONDS.toDays(currentTime-enlist)) / (double)(TimeUnit.MILLISECONDS.toDays(ord - enlist))) * 100.0;
                    ordProgress.setText((Math.round(difference * 100.0)/100.0) + "% completed");
                    progressBar.setProgress((int)difference);

                    int weekends = calculateWeekends();
                    int weekdays = calculateWeekdays(weekends);
                    menuItems.add(weekdays + " Weekdays");
                    menuItems.add(weekends + " Weekends");
                }
            } else {
                menuItems.add("ORD Date not defined. Please define in settings");
            }

            if (pdoption != -1) {
                Calendar cal = Calendar.getInstance();
                switch ((int) pdoption) {
                    case ORDSettingsActivity.PAYDAY_10: cal.set(Calendar.DAY_OF_MONTH, 10); break;
                    case ORDSettingsActivity.PAYDAY_12: cal.set(Calendar.DAY_OF_MONTH, 12); break;
                }

                if (cal.getTimeInMillis() < currentTime) {
                    cal.add(Calendar.MONTH, 1);
                }

                long duration = cal.getTimeInMillis() - currentTime;
                long daysToPayday = TimeUnit.MILLISECONDS.toDays(duration);
                if (daysToPayday == 0) menuItems.add("PAY DAY!!!");
                else menuItems.add(getResources().getQuantityString(R.plurals.ord_payday, (int) daysToPayday, daysToPayday));
            }

            // Holidays Calculation
            ArrayMap<String, Holiday> holidays = getHolidayList();
            Holiday upcomingHoliday = null;

            // Set time to midnight
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(currentTime);
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            long holidayChecker = c.getTimeInMillis();
            // Get closest upcoming date
            for (ArrayMap.Entry<String, Holiday> holidayEntry : holidays.entrySet()) {
                Holiday h = holidayEntry.getValue();
                Log.d("Holiday", h.getHolidayName() + ": " + h.getTime() + " | " + holidayChecker);
                if (h.getTime() >= holidayChecker) {
                    // Upcoming
                    if (upcomingHoliday == null) upcomingHoliday = h;
                    else if (upcomingHoliday.getTime() > h.getTime()) upcomingHoliday = h;
                }
            }

            if (upcomingHoliday != null) {
                // Calculate Days remaining
                long duration = upcomingHoliday.getTime() - (holidayChecker);
                long daysToHoliday = TimeUnit.MILLISECONDS.toDays(duration);
                if (daysToHoliday == 0) menuItems.add("It's " + upcomingHoliday.getHolidayName());
                else menuItems.add(getResources().getQuantityString(R.plurals.ord_holidays, (int) daysToHoliday, daysToHoliday, upcomingHoliday.getHolidayName()));
            }


            StringRecyclerAdapter adapter = new StringRecyclerAdapter(menuItems, false);
            recyclerView.setAdapter(adapter);
        }
    }

    /**
     * Split Holiday List String from Firebase
     * Example String: New Year's:01-01-2017|Christmas:25-12-2017
     * @return Array Map of holidays
     */
    private ArrayMap<String, Holiday> getHolidayList() {
        ArrayMap<String, Holiday> holidays = new ArrayMap<>();
        String[] holidayList = firebaseHolidayList.split("\\|");
        for (String holidayItem : holidayList) {
            Holiday h = new Holiday(holidayItem);
            holidays.put(h.getHolidayName(), h);
        }
        return holidays;
    }

    private int calculateWeekends() {
        if (ordDays == 0) return 0;
        int weekends = 0;
        Calendar cal = Calendar.getInstance();
        for (int i = 0; i < ordDays; i++) {
            if (i != 0) {
                cal.add(Calendar.DAY_OF_WEEK, 1);
            }
            if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                weekends++;
            }
        }
        return weekends;
    }

    private int calculateWeekdays(int weekends) {
        if (ordDays == 0) return 0;
        return (int)ordDays - weekends;
    }

    @Override
    public String getHelpDescription() {
        return "A Basic ORD Countdown timer for Singapore NSF";
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
            case R.id.holiday:
                ArrayList<Holiday> holidayList = new ArrayList<>(getHolidayList().values());
                Collections.sort(holidayList, new Comparator<Holiday>() {
                    @Override
                    public int compare(Holiday o1, Holiday o2) {
                        return (o1.getTime() < o2.getTime()) ? -1 : ((o1.getTime() == o2.getTime()) ? 0 : 1);
                    }
                });
                StringBuilder b = new StringBuilder();
                for (Holiday h : holidayList) {
                    b.append(h.getHolidayName()).append(": ").append(h.getTimeString()).append("\n");
                }
                new AlertDialog.Builder(this).setTitle("Holiday List")
                        .setMessage(b.toString().trim()).setPositiveButton(R.string.dialog_action_positive_close, null)
                        .show();
                return true;
            default: return super.onOptionsItemSelected(item);
        }
    }
}
