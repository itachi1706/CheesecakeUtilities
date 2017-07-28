package com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.Objects.Vehicle;
import com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.Objects.VehicleClass;
import com.itachi1706.cheesecakeutilities.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AddNewMileageRecordActivity extends AppCompatActivity {

    private EditText locationTo, purpose, vehicleNumber, mileageBefore, mileageAfter, timeFrom, timeTo;
    private Spinner vehicle, classType;
    private Button addRecord;
    private CheckBox trainingMileage;

    private FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_mileage_record);

        // Init objects
        locationTo = (EditText) findViewById(R.id.veh_mileage_add_location);
        purpose = (EditText) findViewById(R.id.veh_mileage_add_purpose);
        vehicleNumber = (EditText) findViewById(R.id.veh_mileage_add_veh_num);
        mileageBefore = (EditText) findViewById(R.id.veh_mileage_add_mileage_before);
        mileageAfter = (EditText) findViewById(R.id.veh_mileage_add_mileage_after);
        timeFrom = (EditText) findViewById(R.id.veh_mileage_add_from_datetime);
        timeTo = (EditText) findViewById(R.id.veh_mileage_add_to_datetime);
        vehicle = (Spinner) findViewById(R.id.spinnerVeh);
        classType = (Spinner) findViewById(R.id.spinnerVehType);
        addRecord = (Button) findViewById(R.id.veh_mileage_add_veh);
        trainingMileage = (CheckBox) findViewById(R.id.cbTraining);
        database = FirebaseUtils.getFirebaseDatabase();

        // Init Spinner
        classType.setSelection(1); // Set to Class 3/3A default
        String defaultClassType = classType.getSelectedItem().toString();
        VehicleClass.VehClass vClass = VehicleClass.getClassTypeWithName(defaultClassType);
        assert vClass != null;
        DatabaseReference defaultVehicles = database.getReference().child("vehicles").child(vClass.getId());
        refreshVehicles(defaultVehicles);

        classType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                VehicleClass.VehClass v = VehicleClass.getClassTypeWithName(classType.getSelectedItem().toString());
                assert v != null;
                refreshVehicles(database.getReference().child("vehicles").child(v.getId()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void refreshVehicles(final DatabaseReference vehicles) {
        vehicles.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> vehicleList = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Vehicle v = ds.getValue(Vehicle.class);
                    assert v != null;
                    vehicleList.add(v.getName());
                }
                ArrayAdapter<String> adapter =
                        new ArrayAdapter<>(AddNewMileageRecordActivity.this, android.R.layout.simple_spinner_dropdown_item, vehicleList);
                vehicle.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("VehMileageAddRec", "loadVehicles:" + vehicles.getKey() + ":onCancelled", databaseError.toException());
            }
        });
    }
}
