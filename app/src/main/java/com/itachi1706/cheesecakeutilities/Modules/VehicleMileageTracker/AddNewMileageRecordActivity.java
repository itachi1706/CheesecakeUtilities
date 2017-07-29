package com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.Objects.Record;
import com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.Objects.Vehicle;
import com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.Objects.VehicleClass;
import com.itachi1706.cheesecakeutilities.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.FirebaseUtils.formatTime;

public class AddNewMileageRecordActivity extends AppCompatActivity {

    private EditText locationTo, purpose, vehicleNumber, mileageBefore, mileageAfter, timeFrom, timeTo;
    private Spinner vehicle, classType;
    private CheckBox trainingMileage;
    private LinearLayout layout;

    private FirebaseDatabase database;

    private long fromTimeVal = 0, toTimeVal = 0;
    private String user_id = "";
    private Map<String, Vehicle> vehicleList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_mileage_record);

        if (!getIntent().hasExtra("uid")) {
            Toast.makeText(this, "Accessed activity without User ID, exiting...", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        user_id = getIntent().getStringExtra("uid");

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
        Button addRecord = (Button) findViewById(R.id.veh_mileage_add_veh);
        trainingMileage = (CheckBox) findViewById(R.id.cbTraining);
        layout = (LinearLayout) findViewById(R.id.veh_mileage_add_veh_layout);
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

        // Date Time Pickers
        timeFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFromDate();
            }
        });
        timeTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setToDate();
            }
        });
        addRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addRecordToFirebase();
            }
        });
    }

    private void addRecordToFirebase() {
        if (!validate()) return;

        Record r = new Record();
        r.setDatetimeFrom(fromTimeVal);
        r.setDateTimeTo(toTimeVal);
        r.setMileageFrom(Double.parseDouble(mileageBefore.getText().toString()));
        r.setMileageTo(Double.parseDouble(mileageAfter.getText().toString()));
        r.setDestination(locationTo.getText().toString());
        r.setPurpose(purpose.getText().toString());
        r.setVehicleNumber(vehicleNumber.getText().toString());
        r.setTrainingMileage(trainingMileage.isChecked());
        r.setVehicleId(getVehicleKey(vehicle.getSelectedItem().toString()));
        r.setVehicleClass(getVehicleClass(vehicle.getSelectedItem().toString()));
        r.updateMileage();
        r.updateTotalTime();
        r.setVersion(FirebaseUtils.RECORDS_VERSION);

        DatabaseReference newRec = FirebaseUtils.getFirebaseDatabase().getReference().child("users").child(user_id).child("records").push();
        newRec.setValue(r);
        Toast.makeText(this, "Record Added", Toast.LENGTH_SHORT).show();
        finish();
    }

    private String getVehicleKey(String vehicleName) {
        for (Map.Entry<String, Vehicle> k : vehicleList.entrySet()) {
            if (k.getValue().getName().equals(vehicleName)) {
                return k.getKey();
            }
        }
        return "";
    }

    private String getVehicleClass(String vehicleName) {
        for (Map.Entry<String, Vehicle> k : vehicleList.entrySet()) {
            if (k.getValue().getName().equals(vehicleName)) {
                return k.getValue().getVehicleClass();
            }
        }
        return "class3";
    }

    private boolean validate() {
        if (locationTo.getText().toString().isEmpty() || purpose.getText().toString().isEmpty()
                || mileageAfter.getText().toString().isEmpty() || mileageBefore.getText().toString().isEmpty() || fromTimeVal == 0
                || toTimeVal == 0) {
            Snackbar.make(layout, "Please fill up all of the fields and ensure that they are correct", Snackbar.LENGTH_SHORT).show();
            return false;
        }
        if (vehicle.getSelectedItem() == null) {
            Snackbar.make(layout, "Please select a vehicle type or create a new vehicle type", Snackbar.LENGTH_SHORT).show();
            return false;
        }
        if (fromTimeVal != 0 && toTimeVal != 0 && fromTimeVal > toTimeVal) {
            Snackbar.make(layout, "End time cannot be after start time", Snackbar.LENGTH_SHORT).show();
            return false;
        }
        if (!mileageBefore.getText().toString().isEmpty() && !mileageAfter.getText().toString().isEmpty()
                && Double.parseDouble(mileageBefore.getText().toString()) > Double.parseDouble(mileageAfter.getText().toString())) {
            Snackbar.make(layout, "Mileage after trip cannot be smaller than the mileage before trip", Snackbar.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void setFromDate() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, new FromTimeDate(), cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void setFromTime() {
        processFromTime(); // Process first in case user cancel
        Calendar cal = Calendar.getInstance();
        new TimePickerDialog(this, new FromTimeDate(), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
    }

    private void processFromTime() {
        timeFrom.setText(formatTime(fromTimeVal) + " hrs");
    }

    private class FromTimeDate implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(fromTimeVal);
            c.set(Calendar.YEAR, year);
            c.set(Calendar.MONTH, monthOfYear);
            c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            fromTimeVal = c.getTimeInMillis();
            setFromTime();
        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(fromTimeVal);
            c.set(Calendar.HOUR_OF_DAY, hourOfDay);
            c.set(Calendar.MINUTE, minute);
            fromTimeVal = c.getTimeInMillis();
            processFromTime();
        }
    }

    private void setToDate() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, new ToTimeDate(), cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void setToTime() {
        processToTime(); // Process first in case user cancel
        Calendar cal = Calendar.getInstance();
        new TimePickerDialog(this, new ToTimeDate(), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
    }

    private void processToTime() {
        timeTo.setText(formatTime(toTimeVal) + " hrs");
    }

    private class ToTimeDate implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(toTimeVal);
            c.set(Calendar.YEAR, year);
            c.set(Calendar.MONTH, monthOfYear);
            c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            toTimeVal = c.getTimeInMillis();
            setToTime();
        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(toTimeVal);
            c.set(Calendar.HOUR_OF_DAY, hourOfDay);
            c.set(Calendar.MINUTE, minute);
            toTimeVal = c.getTimeInMillis();
            processToTime();
        }
    }

    private void refreshVehicles(final DatabaseReference vehicles) {
        vehicles.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> vL = new ArrayList<>();
                if (vehicleList == null) vehicleList = new HashMap<>();
                else vehicleList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Vehicle v = ds.getValue(Vehicle.class);
                    assert v != null;
                    vehicleList.put(ds.getKey(), v);
                    vL.add(v.getName());
                }
                ArrayAdapter<String> adapter =
                        new ArrayAdapter<>(AddNewMileageRecordActivity.this, android.R.layout.simple_spinner_dropdown_item, vL);
                vehicle.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("VehMileageAddRec", "loadVehicles:" + vehicles.getKey() + ":onCancelled", databaseError.toException());
            }
        });
    }
}
