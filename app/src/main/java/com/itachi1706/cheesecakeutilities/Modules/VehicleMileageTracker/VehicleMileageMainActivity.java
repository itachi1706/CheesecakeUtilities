package com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker;

import android.content.Context;
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
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.itachi1706.cheesecakeutilities.BaseActivity;
import com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.Objects.Record;
import com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.RecyclerAdapters.VehicleMileageRecordsAdapter;
import com.itachi1706.cheesecakeutilities.R;
import com.turingtechnologies.materialscrollbar.DateAndTimeIndicator;
import com.turingtechnologies.materialscrollbar.TouchScrollBar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

import static com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.FirebaseUtils.FB_REC_RECORDS;
import static com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.FirebaseUtils.FB_REC_USER;
import static com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.FirebaseUtils.MILEAGE_DEC;

public class VehicleMileageMainActivity extends BaseActivity {

    private static final String TAG = "VehMileageMain";

    private DatabaseReference userdata;
    private VehicleMileageRecordsAdapter adapter;
    private SharedPreferences sp;
    private String lastRecord;

    private TouchScrollBar scrollBar;


    @Override
    public String getHelpDescription() {
        return "A utility to track vehicle mileage";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_mileage_main_activty);

        // Do Firebase Setup
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        final String user_id = sp.getString("firebase_uid", "nien");
        if (user_id.equalsIgnoreCase("nien")) {
            // Fail, return to login activity
            Toast.makeText(this, "Invalid Login Token", Toast.LENGTH_SHORT).show();
            Intent logoutIntent = new Intent(this, VehicleMileageTrackerLoginActivity.class);
            logoutIntent.putExtra("logout", true);
            startActivity(logoutIntent);
            finish();
            return;
        }

        final FirebaseDatabase database = FirebaseUtils.getFirebaseDatabase();
        userdata = database.getReference().child(FB_REC_USER).child(user_id);

        findViewById(R.id.veh_mileage_fab_car).setOnClickListener(v -> startActivity(new Intent(v.getContext(), AddNewVehicleActivity.class)));
        findViewById(R.id.veh_mileage_fab_record).setOnClickListener(v -> launchAddRecordActivity(v.getContext(), user_id, null));
        findViewById(R.id.veh_mileage_fab_record_cont).setOnClickListener(v -> {
            if (lastRecord == null || lastRecord.isEmpty() || lastRecord.equals("")) {
                Toast.makeText(v.getContext(), "Unable to query last record", Toast.LENGTH_LONG).show();
                return;
            }
            launchAddRecordActivity(v.getContext(), user_id, lastRecord);
        });

        RecyclerView recyclerView = findViewById(R.id.veh_mileage_main_list);
        if (recyclerView != null) {
            recyclerView.setHasFixedSize(true);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());

            // Set up layout
            adapter = new VehicleMileageRecordsAdapter(new ArrayList<>(), new ArrayList<>(), null, sp.getBoolean(MILEAGE_DEC, true));
            recyclerView.setAdapter(adapter);
        }
        scrollBar = findViewById(R.id.scrollBar);
        scrollBar.setIndicator(new DateAndTimeIndicator(this, true, true, true, false), true);
    }

    private void launchAddRecordActivity(Context context, String userid, @Nullable String lastRecord) {
        Intent i = new Intent(context, AddNewMileageRecordActivity.class);
        i.putExtra("uid", userid);
        if (lastRecord != null) i.putExtra("cont", lastRecord);
        startActivity(i);
    }

    private ValueEventListener listener;

    @Override
    protected void onStart() {
        super.onStart();
        // Listen to changes and update accordingly
        if (listener != null) {
            FirebaseDatabase.getInstance().getReference().removeEventListener(listener);
            listener = null;
            Log.i(TAG, "Firebase DB Listeners exist when it should not, force terminating it");
        }
        Log.i(TAG, "Registering Firebase DB listeners");
        listener = userdata.child(FB_REC_RECORDS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i(TAG, "Records has been updated. Processing...");
                final List<Record> records = new ArrayList<>();
                final List<String> tags = new ArrayList<>();
                HashMap<String, Object> migratedRecords = null;
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Record record = ds.getValue(Record.class);
                    // Update records
                    assert record != null;
                    if (record.getVersion() < FirebaseUtils.RECORDS_VERSION) {
                        // Migrate records
                        record = migrateRecord(record);
                        if (migratedRecords == null) migratedRecords = new HashMap<>();
                        migratedRecords.put(ds.getKey(), record);
                    }
                    records.add(record);
                    tags.add(ds.getKey());
                }
                if (migratedRecords != null)
                    userdata.child(FB_REC_RECORDS).updateChildren(migratedRecords);
                Log.i(TAG, "Records: " + records.size());
                Collections.reverse(records);
                Collections.reverse(tags);
                if (tags.size() > 0) lastRecord = tags.get(0);
                FirebaseUtils.getFirebaseDatabase().getReference().child("vehicles").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        adapter.updateRecords(records, tags);
                        adapter.updateSnapshot(dataSnapshot);
                        adapter.setHideTraining(sp.getBoolean(HIDE_TRAINING, false));
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w("VehMileageAdapter", "loadVehicles:onCancelled", databaseError.toException());
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadRecords:onCancelled", databaseError.toException());
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (listener != null) {
            FirebaseDatabase.getInstance().getReference().removeEventListener(listener);
            Log.i(TAG, "Unregistered Firebase Listeners");
            listener = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((FloatingActionsMenu)findViewById(R.id.veh_mileage_fab)).collapseImmediately();
    }

    private Record migrateRecord(Record record) {
        int oldV = record.getVersion();
        if (oldV < 1) {
            // Add Total Mileage and Total Time Taken
            record.updateTotalTime();
            record.updateMileage();
            record.setVersion(1);
        }
        if (oldV < 4) {
            // Add timezone offset
            Calendar c = Calendar.getInstance();
            long offset = c.getTimeZone().getOffset(record.getDatetimeFrom());
            if (record.getTimezone() == null) record.setTimezone(offset);
            record.setVersion(4);
        }
        return record;
    }

    private static final String HIDE_TRAINING = "veh_mileage_hide_training";

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.modules_veh_mileage, menu);
        menu.findItem(R.id.hide_training).setChecked(sp.getBoolean(HIDE_TRAINING, false));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                Intent logoutIntent = new Intent(this, VehicleMileageTrackerLoginActivity.class);
                logoutIntent.putExtra("logout", true);
                startActivity(logoutIntent);
                finish();
                return true;
            case R.id.hide_training:
                item.setChecked(!item.isChecked());
                if (item.isChecked()) sp.edit().putBoolean(HIDE_TRAINING, true).apply();
                else sp.edit().putBoolean(HIDE_TRAINING, false).apply();
                adapter.setHideTraining(sp.getBoolean(HIDE_TRAINING, false));
                adapter.notifyDataSetChanged();
                return true;
            case R.id.view_vehicles:
                startActivity(new Intent(this, ViewVehicleActivity.class));
                return true;
            case R.id.view_statistics:
                startActivity(new Intent(this, VehicleMileageStatisticsActivity.class));
                return true;
            default: return super.onOptionsItemSelected(item);
        }
    }
}
