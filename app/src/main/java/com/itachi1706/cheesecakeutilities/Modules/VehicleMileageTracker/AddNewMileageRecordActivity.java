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
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
    }

    private long fromTimeVal = 0, toTimeVal = 0;

    private void setFromDate() {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog dpd = DatePickerDialog.newInstance(new FromTimeDate(),
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        dpd.show(getFragmentManager(), "DatepickerdialogF");
        dpd.dismissOnPause(true);
    }

    private void setFromTime() {
        Calendar cal = Calendar.getInstance();
        TimePickerDialog tpd = TimePickerDialog.newInstance(new FromTimeDate(),
                cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true);
        tpd.show(getFragmentManager(), "TimepickerdialogF");
        tpd.dismissOnPause(true);
    }

    private void processFromTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy HH:mm", Locale.US);
        Date dt = new Date();
        dt.setTime(fromTimeVal);
        String date = sdf.format(dt);
        timeFrom.setText(date + " hrs");
    }

    private class FromTimeDate implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

        @Override
        public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(fromTimeVal);
            c.set(Calendar.YEAR, year);
            c.set(Calendar.MONTH, monthOfYear);
            c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            fromTimeVal = c.getTimeInMillis();
            setFromTime();
        }

        @Override
        public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
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
        DatePickerDialog dpd = DatePickerDialog.newInstance(new ToTimeDate(),
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        dpd.show(getFragmentManager(), "DatepickerdialogT");
        dpd.dismissOnPause(true);
    }

    private void setToTime() {
        Calendar cal = Calendar.getInstance();
        TimePickerDialog tpd = TimePickerDialog.newInstance(new ToTimeDate(),
                cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true);
        tpd.show(getFragmentManager(), "TimepickerdialogT");
        tpd.dismissOnPause(true);
    }

    private void processToTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy HH:mm", Locale.US);
        Date dt = new Date();
        dt.setTime(toTimeVal);
        String date = sdf.format(dt);
        timeTo.setText(date + " hrs");
    }

    private class ToTimeDate implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

        @Override
        public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(toTimeVal);
            c.set(Calendar.YEAR, year);
            c.set(Calendar.MONTH, monthOfYear);
            c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            toTimeVal = c.getTimeInMillis();
            setToTime();
        }

        @Override
        public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
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
