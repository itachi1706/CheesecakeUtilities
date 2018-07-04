package com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_vehicle_activity);

        if (getSupportActionBar() != null && getSupportActionBar().isShowing()) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        final RecyclerView recyclerView = findViewById(R.id.recycler_view);
        if (recyclerView != null) {
            recyclerView.setHasFixedSize(true);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());

            // Set up layout
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
                                vehicleList.add(new DualLineString(v.getName() + " (" + v.getVehicleClass().toUpperCase() + ")", v.getShortname()));
                            }
                        }
                    }
                    DualLineStringRecyclerAdapter adapter = new DualLineStringRecyclerAdapter(vehicleList, false);
                    adapter.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
                        TextView test = v.findViewById(android.R.id.text2);
                        selectEdit = test.getText().toString().replaceAll("[^A-Za-z0-9]", "");
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

    private String selectEdit;

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Log.d("Test", "Something is clicked");
        if (item.getTitle().equals("Edit")) {
            Intent editIntent = new Intent(this, AddNewVehicleActivity.class);
            editIntent.putExtra("edit", true);
            editIntent.putExtra("id", selectEdit);
            startActivity(editIntent);
            return true;
        } else if (item.getTitle().equals("Delete")) {
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
}
