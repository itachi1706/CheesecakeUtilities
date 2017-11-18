package com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker;

import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.Objects.Vehicle;
import com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.Objects.VehicleClass;
import com.itachi1706.cheesecakeutilities.R;

public class AddNewVehicleActivity extends AppCompatActivity {

    private EditText name, longname;
    private Spinner vehClass;
    private LinearLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_vehicle);

        name = findViewById(R.id.etName);
        longname = findViewById(R.id.etLongName);
        vehClass = findViewById(R.id.spinnerVehType);
        Button addVehicle = findViewById(R.id.veh_mileage_add_veh);
        layout = findViewById(R.id.veh_mileage_layout_vehicle);
        addVehicle.setOnClickListener(v -> addVehicle());
    }

    private void addVehicle() {
        // Validation
        if (!validate()) {
            Snackbar.make(layout, "Please fill up all of the fields", Snackbar.LENGTH_SHORT).show();
            return;
        }

        // Add to Firebase
        VehicleClass.VehClass classV = VehicleClass.getClassTypeWithName(vehClass.getSelectedItem().toString());
        assert classV != null;
        String nameNoSpace = name.getText().toString().replace(" ", "");
        nameNoSpace = nameNoSpace.replaceAll("[^A-Za-z0-9]", "");
        Vehicle v = new Vehicle();
        v.setName(longname.getText().toString());
        v.setShortname(name.getText().toString());
        v.setVehicleClass(classV.getId());

        FirebaseUtils.getFirebaseDatabase().getReference().child("vehicles").child(v.getVehicleClass()).child(nameNoSpace).setValue(v);
        Toast.makeText(this, "Vehicle Added", Toast.LENGTH_SHORT).show();
        finish();
    }

    private boolean validate() {
        return !name.getText().toString().isEmpty() && !longname.getText().toString().isEmpty()
                && VehicleClass.getClassTypeWithName(vehClass.getSelectedItem().toString()) != null;
    }
}
