package com.itachi1706.cheesecakeutilities.Modules.ORDCountdown;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.github.lzyzsd.circleprogress.ArcProgress;
import com.google.gson.Gson;
import com.itachi1706.appupdater.Util.PrefHelper;
import com.itachi1706.appupdater.Util.UpdaterHelper;
import com.itachi1706.cheesecakeutilities.BaseModuleActivity;
import com.itachi1706.cheesecakeutilities.Modules.ORDCountdown.json.GCalHoliday;
import com.itachi1706.cheesecakeutilities.Modules.ORDCountdown.json.GCalHolidayItem;
import com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.FirebaseUtils;
import com.itachi1706.cheesecakeutilities.R;
import com.itachi1706.cheesecakeutilities.RecyclerAdapters.StringRecyclerAdapter;
import com.itachi1706.cheesecakeutilities.Util.JSONHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ORDActivity extends BaseModuleActivity {

    RecyclerView recyclerView;
    TextView ordCounter, ordDaysLabel, ordProgress;
    ArcProgress progressBar;
    long ordDays, ptpDays, popDays, milestoneDays, pdoption;

    private static final String ORD_HOLIDAY_PREF = "ord_sg_holidays";
    private static final long ORD_HOLIDAY_TIMEOUT = 86400000; // 24 hours timeout

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ord);

        recyclerView = findViewById(R.id.ord_recycler_view);
        ordDaysLabel = findViewById(R.id.ord_days_counter);
        ordCounter = findViewById(R.id.ord_counter);
        ordProgress = findViewById(R.id.ord_precentage);
        progressBar = findViewById(R.id.ord_progressbar);
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
            repopulateAdapter();
        }
    }

    private void repopulateAdapter() {
        SharedPreferences sp = PrefHelper.getDefaultSharedPreferences(this);
        long ord = sp.getLong(ORDSettingsActivity.SP_ORD, 0);
        long ptp = sp.getLong(ORDSettingsActivity.SP_PTP, 0);
        long pop = sp.getLong(ORDSettingsActivity.SP_POP, 0);
        long enlist = sp.getLong(ORDSettingsActivity.SP_ENLIST, 0);
        long milestone = sp.getLong(ORDSettingsActivity.SP_MILESTONE, 0);
        int off = sp.getInt(ORDSettingsActivity.SP_OFF, 0);
        int leave = sp.getInt(ORDSettingsActivity.SP_LEAVE, 0);
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

        if (milestone != 0) {
            if (currentTime > milestone) {
                menuItems.add("Completed Milestone Parade"); // PTP LOH
            } else {
                long duration = milestone - currentTime;
                milestoneDays = TimeUnit.MILLISECONDS.toDays(duration) + 1;
                menuItems.add(getResources().getQuantityString(R.plurals.ord_days_countdown, (int) milestoneDays, milestoneDays, "Milestone Parade"));
            }
        }

        int weekdays = 0;
        boolean ordloh = false;

        if (ord != 0) {
            if (currentTime > ord) {
                // ORD LOH
                ordloh = true;
                ordDaysLabel.setText(R.string.ord_loh);
                ordCounter.setText(R.string.ord_hint);
                ordProgress.setText(getString(R.string.ord_complete, "100"));
                progressBar.setProgress(100);
            } else {
                long duration = ord - currentTime;
                ordDays = TimeUnit.MILLISECONDS.toDays(duration) + 1;
                ordDaysLabel.setText(getResources().getQuantityString(R.plurals.ord_days, (int) ordDays));
                ordCounter.setText(getString(R.string.number, ordDays));

                double difference = ((TimeUnit.MILLISECONDS.toDays(currentTime-enlist)) / (double)(TimeUnit.MILLISECONDS.toDays(ord - enlist))) * 100.0;
                ordProgress.setText(getString(R.string.ord_complete, Math.round(difference * 100.0)/100.0 + ""));
                progressBar.setProgress((int)difference);

                int weekends = calculateWeekends();
                weekdays = calculateWeekdays(weekends);
                menuItems.add(weekdays + " Weekdays, " + weekends + " Weekends");
            }
        } else {
            ordloh = true;
            menuItems.add("ORD Date not defined. Please define in settings");
        }

        // Offs and Leaves
        int offAndLeave = off + leave;
        String offLeaveString = offAndLeave + " Rest Day(s)";
        if (off > 0 || leave > 0) {
            offLeaveString += " (";

            if (leave > 0) offLeaveString += leave + " Leave";
            else offLeaveString += off + " Off";
            if (leave > 0 && off > 0) offLeaveString += ", " + off + " Off";
            offLeaveString += ")";
        }
        menuItems.add(offLeaveString);

        // Working days calculation
        int workingDays = weekdays - offAndLeave;
        if (workingDays > 0) menuItems.add(getResources().getQuantityString(R.plurals.ord_wdays, workingDays, workingDays));
        else if (!ordloh) menuItems.add("No more working days until ORD");


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
        menuItems.add(getNextHoliday());

        StringRecyclerAdapter adapter = new StringRecyclerAdapter(menuItems, false);
        recyclerView.setAdapter(adapter);
    }

    private String getNextHoliday() {
        GCalHoliday holiday = getHolidays();
        if (holiday == null) return "Retrieving holiday data...";

        GCalHolidayItem[] holidays = holiday.getOutput();
        GCalHolidayItem upcoming = null;

        // Set time to midnight
        long currentTime = System.currentTimeMillis();
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(currentTime);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        long holidayChecker = c.getTimeInMillis();

        // Get closest upcoming date
        for (GCalHolidayItem h : holidays) {
            Log.d("Holiday", h.getName() + ": " + h.getDateInMillis() + " (" + h.getDate() + ") | " + holidayChecker);
            if (h.getDateInMillis() >= holidayChecker) {
                // Upcoming
                if (upcoming == null) upcoming= h;
                else if (upcoming.getDateInMillis() > h.getDateInMillis()) upcoming = h;
            }
        }

        if (upcoming != null) {
            // Calculate Days remaining
            long duration = upcoming.getDateInMillis() - (holidayChecker);
            long daysToHoliday = TimeUnit.MILLISECONDS.toDays(duration);
            if (daysToHoliday == 0) return "It's " + upcoming.getName();
            return getResources().getQuantityString(R.plurals.ord_holidays, (int) daysToHoliday, daysToHoliday, upcoming.getName());
        }

        return "Unable to retrieve holiday data";
    }

    /**
     * Get list of holiday from internal cache or API
     * @return GCalHoliday Holiday list object
     */
    private GCalHoliday getHolidays() {
        SharedPreferences sp = PrefHelper.getDefaultSharedPreferences(this);
        String t = sp.getString(ORD_HOLIDAY_PREF, "---");
        if (t.equals("---") || !JSONHelper.isJsonValid(t)) {
            new CalendarHolidayTask().execute();
            return null;
        }
        // Retrieve from cache and determine if its viable
        GCalHoliday hol = new Gson().fromJson(t, GCalHoliday.class);
        long curTime = System.currentTimeMillis();
        if (curTime - hol.getTimestampLong() > ORD_HOLIDAY_TIMEOUT) { // Greater than timeout, invalidate it as well
            new CalendarHolidayTask().execute();
        }
        return hol;
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
        return "A Basic ORD Countdown timer for Singapore NSF" +
                "\n\n*NOTE: Working days refers to weekdays (Monday - Friday) INCLUDING holidays that happens on a weekday";
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
                GCalHoliday holiday = getHolidays();
                if (holiday == null) {
                    Toast.makeText(this, "Retrieving holiday list, try again later", Toast.LENGTH_SHORT).show();
                    return true;
                }
                ArrayList<GCalHolidayItem> holidayList = new ArrayList<>(Arrays.asList(holiday.getOutput()));
                Collections.sort(holidayList, (o1, o2) -> Long.compare(o1.getDateInMillis(), o2.getDateInMillis()));
                StringBuilder b = new StringBuilder();
                for (GCalHolidayItem h : holidayList) {
                    String[] tmp = h.getDate().split("-");
                    b.append(h.getName()).append(": ").append(tmp[2]).append("-").append(tmp[1]).append("-").append(tmp[0]).append("\n");
                }

                b.append("\nLast Updated: ").append(FirebaseUtils.formatTime(holiday.getTimestampLong(), "dd MMMM yyyy HH:mm:ss"));
                b.append("\nServer Cached Data: ").append(holiday.isCache());
                new AlertDialog.Builder(this).setTitle("Holiday List (" + holiday.getYearRange() + ")")
                        .setMessage(b.toString().trim()).setPositiveButton(R.string.dialog_action_positive_close, null)
                        .setNeutralButton("Refresh", (dialog, which) -> {
                            new CalendarHolidayTask().execute();
                            Toast.makeText(getApplicationContext(), "Refreshing holiday list...", Toast.LENGTH_SHORT).show();
                        })
                        .show();
                return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    private class CalendarHolidayTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            String url = "http://api.itachi1706.com/api/gcal_sg_holidays.php";
            String tmp;
            try {
                URL urlConn = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) urlConn.openConnection();
                conn.setConnectTimeout(UpdaterHelper.HTTP_QUERY_TIMEOUT);
                conn.setReadTimeout(UpdaterHelper.HTTP_QUERY_TIMEOUT);
                InputStream in = conn.getInputStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder str = new StringBuilder();
                String line;
                while((line = reader.readLine()) != null)
                {
                    str.append(line).append("\n");
                }
                in.close();
                tmp = str.toString();

                SharedPreferences sp = PrefHelper.getDefaultSharedPreferences(getApplicationContext());
                sp.edit().putString(ORD_HOLIDAY_PREF, tmp).apply();

                runOnUiThread(ORDActivity.this::repopulateAdapter);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
