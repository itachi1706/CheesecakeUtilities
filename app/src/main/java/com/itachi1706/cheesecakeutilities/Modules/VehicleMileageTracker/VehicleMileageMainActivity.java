package com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.itachi1706.cheesecakeutilities.R;

public class VehicleMileageMainActivity extends AppCompatActivity {

    private FloatingActionButton car, record;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_mileage_main_activty);

        car = (FloatingActionButton) findViewById(R.id.veh_mileage_fab_car);
        record = (FloatingActionButton) findViewById(R.id.veh_mileage_fab_record);
        car.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Code Stub for Adding Vehicle Type
                Toast.makeText(v.getContext(), "Car", Toast.LENGTH_SHORT).show();
            }
        });

        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Code Stub for Adding new Record
                Toast.makeText(v.getContext(), "Record", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
