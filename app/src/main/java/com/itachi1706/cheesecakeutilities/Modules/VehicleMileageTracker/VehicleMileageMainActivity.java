package com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VehicleMileageMainActivity extends BaseActivity {

    private static final String TAG = "VehMileageMain";

    private DatabaseReference userdata;
    private VehicleMileageRecordsAdapter adapter;


    @Override
    public String getHelpDescription() {
        return "A utility to track vehicle mileage";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_mileage_main_activty);

        // Do Firebase Setup
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
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
        userdata = database.getReference().child("users").child(user_id);

        findViewById(R.id.veh_mileage_fab_car).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(v.getContext(), AddNewVehicleActivity.class));
            }
        });
        findViewById(R.id.veh_mileage_fab_record).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(v.getContext(), AddNewMileageRecordActivity.class);
                i.putExtra("uid", user_id);
                startActivity(i);
            }
        });

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.veh_mileage_main_list);
        if (recyclerView != null) {
            recyclerView.setHasFixedSize(true);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());

            // Set up layout
            adapter = new VehicleMileageRecordsAdapter(new ArrayList<Record>(), null);
            recyclerView.setAdapter(adapter);
        }

        // Listen to changes and update accordingly
        userdata.child("records").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i(TAG, "Records has been updated. Processing...");
                final List<Record> records = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Record recList = ds.getValue(Record.class);
                    // Update records
                    assert recList != null;
                    if (recList.getVersion() < FirebaseUtils.RECORDS_VERSION) {
                        // Migrate records
                        recList = migrateRecord(recList);
                        userdata.child("records").child(ds.getKey()).setValue(recList);
                    }
                    records.add(recList);
                }
                Log.i(TAG, "Records: " + records.size());
                Collections.reverse(records);
                FirebaseUtils.getFirebaseDatabase().getReference().child("vehicles").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        adapter.updateRecords(records);
                        adapter.updateSnapshot(dataSnapshot);
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
        return record;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.modules_veh_mileage, menu);
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
            default: return super.onOptionsItemSelected(item);
        }
    }
}
