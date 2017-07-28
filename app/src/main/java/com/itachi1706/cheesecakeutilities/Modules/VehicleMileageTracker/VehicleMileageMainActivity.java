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
import android.view.View;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.Objects.Records;
import com.itachi1706.cheesecakeutilities.R;
import com.itachi1706.cheesecakeutilities.RecyclerAdapters.StringRecyclerAdapter;

public class VehicleMileageMainActivity extends AppCompatActivity {

    private FloatingActionButton car, record;
    private RecyclerView recyclerView;
    private static final String TAG = "VehMileageMain";

    private FirebaseDatabase database;
    private DatabaseReference userdata;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_mileage_main_activty);

        car = (FloatingActionButton) findViewById(R.id.veh_mileage_fab_car);
        record = (FloatingActionButton) findViewById(R.id.veh_mileage_fab_record);
        car.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(v.getContext(), AddNewVehicleActivity.class));
            }
        });

        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Code Stub for Adding new Record
                Toast.makeText(v.getContext(), "Record", Toast.LENGTH_SHORT).show();
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.veh_mileage_main_list);
        if (recyclerView != null) {
            recyclerView.setHasFixedSize(true);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());

            // Set up layout
            StringRecyclerAdapter adapter = new StringRecyclerAdapter(new String[0]);
            recyclerView.setAdapter(adapter);
        }

        // Do Firebase Setup
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String user_id = sp.getString("firebase_uid", "nien");
        if (user_id.equalsIgnoreCase("nien")) {
            // Fail, return to login activity
            Toast.makeText(this, "Invalid Login Token", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, VehicleMileageTrackerLoginActivity.class));
            finish();
            return;
        }

        database = FirebaseUtils.getFirebaseDatabase();
        userdata = database.getReference().child("users").child(user_id);
        // Listen to changes and update accordingly
        userdata.child("records").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Records recList = dataSnapshot.getValue(Records.class);
                // TODO: Update Recycler
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        });
    }
}
