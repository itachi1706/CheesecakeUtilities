package com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.Objects.Vehicle;
import com.itachi1706.cheesecakeutilities.Objects.DualLineString;
import com.itachi1706.cheesecakeutilities.R;
import com.itachi1706.cheesecakeutilities.RecyclerAdapters.DualLineStringRecyclerAdapter;

import java.util.ArrayList;
import java.util.List;

public class ViewVehicleActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_vehicle_activity);

        if (getSupportActionBar() != null && getSupportActionBar().isShowing()) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = findViewById(R.id.recycler_view);
        if (recyclerView != null) {
            recyclerView.setHasFixedSize(true);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Set up layout
        if (recyclerView != null) updateRecords(recyclerView);
    }

    private String selectEdit, selectClass;

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Log.d("Test", "Something is clicked");
        if (item.getTitle().equals("Edit")) {
            Intent editIntent = new Intent(this, AddNewVehicleActivity.class);
            editIntent.putExtra("edit", true);
            editIntent.putExtra("id", selectEdit);
            editIntent.putExtra("class", selectClass);
            startActivity(editIntent);
            return true;
        } else if (item.getTitle().equals("Delete")) {
            new AlertDialog.Builder(this).setTitle("Confirm Deletion")
                    .setMessage("Are you sure you want to delete " + selectClass + " vehicle " + selectEdit + "?\n\nTHIS ACTION CANNOT BE UNDONE!")
                    .setNegativeButton(android.R.string.cancel, null).setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        FirebaseUtils.getFirebaseDatabase().getReference().child("vehicles")
                                .child(selectClass).child(selectEdit).removeValue();
                        Toast.makeText(getApplicationContext(), "Vehicle Deleted", Toast.LENGTH_SHORT).show();
                        updateRecords(recyclerView);
                    }).show();
            return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateRecords(RecyclerView recyclerView) {
        FirebaseUtils.getFirebaseDatabase().getReference().child("vehicles").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChildren()) return;
                List<DualLineString> vehicleList = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (ds.hasChildren()) {
                        for (DataSnapshot d : ds.getChildren()) {
                            Vehicle v = d.getValue(Vehicle.class);
                            if (v == null) continue;
                            vehicleList.add(new DualLineString(v.getName(), v.getShortname() + " (" + v.getVehicleClass().toUpperCase() + ")"));
                        }
                    }
                }
                DualLineStringRecyclerAdapter adapter = new DualLineStringRecyclerAdapter(vehicleList, false);
                adapter.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
                    TextView test = v.findViewById(android.R.id.text2);
                    String[] tmp = test.getText().toString().split(" ");
                    StringBuilder strBuilder = new StringBuilder();
                    for (int i = 0; i < tmp.length - 1; i++) {
                        strBuilder.append(tmp[i]).append(" ");
                    }
                    selectEdit = strBuilder.toString().trim().replaceAll("[^A-Za-z0-9]", "");
                    selectClass = tmp[tmp.length - 1].substring(1, tmp[tmp.length - 1].length() - 1).toLowerCase();
                    menu.setHeaderTitle("Edit Vehicle " + selectEdit);
                    menu.add("Edit");
                    menu.add("Delete");
                });
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("ViewVehicleAct", "loadVehicles:onCancelled", databaseError.toException());
            }
        });
    }
}
