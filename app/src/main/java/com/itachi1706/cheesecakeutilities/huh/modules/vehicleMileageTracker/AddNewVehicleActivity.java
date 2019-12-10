package com.itachi1706.cheesecakeutilities.huh.modules.vehicleMileageTracker;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.itachi1706.cheesecakeutilities.huh.modules.vehicleMileageTracker.objects.Vehicle;
import com.itachi1706.cheesecakeutilities.huh.modules.vehicleMileageTracker.objects.VehicleClass;
import com.itachi1706.cheesecakeutilities.R;
import com.itachi1706.cheesecakeutilities.huh.util.FirebaseValueEventListener;

public class AddNewVehicleActivity extends AppCompatActivity {

    private EditText name, longname;
    private Spinner vehClass;
    private LinearLayout layout;
    private boolean edit = false;
    private DatabaseReference editRef;
    private Vehicle editV = null;

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

        // Check if edit mode
        if (getIntent().hasExtra("edit") && getIntent().getBooleanExtra("edit", false)) {
            edit = true;
            String id = getIntent().getStringExtra("id");
            String selClass = getIntent().getStringExtra("class");
            VehMileageFirebaseUtils.getVehicleMileageDatabase().child("vehicles").child(selClass).addListenerForSingleValueEvent(new FirebaseValueEventListener("AddNewVeh", "getVehicle") {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot d : dataSnapshot.getChildren()) {
                        if (d.getKey().equals(id)) {
                            editV = d.getValue(Vehicle.class);
                            editRef = d.getRef();
                        }
                    }

                    name.setText(editV.getShortname()); // Disable shortname edit
                    name.setEnabled(false);
                    if (getSupportActionBar() != null) getSupportActionBar().setTitle("Edit Vehicle (" + editV.getShortname() + ")");
                    longname.setText(editV.getName());
                    vehClass.setVisibility(View.INVISIBLE);
                    findViewById(R.id.spinnerVehLbl).setVisibility(View.INVISIBLE);
                    addVehicle.setText("Edit Vehicle");
                }
            });
        }
    }

    private void addVehicle() {
        // Validation
        if (!validate()) {
            Snackbar.make(layout, "Please fill up all of the fields", Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (edit) {
            // Update record based on class
            editV.setName(longname.getText().toString());
            editRef.setValue(editV);
            Toast.makeText(this, "Edit successful", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Add to Firebase
        VehicleClass.VehClass classV = VehicleClass.INSTANCE.getClassTypeWithName(vehClass.getSelectedItem().toString());
        assert classV != null;
        String nameNoSpace = name.getText().toString().replace(" ", "");
        nameNoSpace = nameNoSpace.replaceAll("[^A-Za-z0-9]", "");
        Vehicle v = new Vehicle();
        v.setName(longname.getText().toString());
        v.setShortname(name.getText().toString());
        v.setVehicleClass(classV.getId());

        VehMileageFirebaseUtils.getVehicleMileageDatabase().child("vehicles").child(v.getVehicleClass()).child(nameNoSpace).setValue(v);
        Toast.makeText(this, "Vehicle Added", Toast.LENGTH_SHORT).show();
        finish();
    }

    private boolean validate() {
        return !name.getText().toString().isEmpty() && !longname.getText().toString().isEmpty()
                && VehicleClass.INSTANCE.getClassTypeWithName(vehClass.getSelectedItem().toString()) != null;
    }
}
