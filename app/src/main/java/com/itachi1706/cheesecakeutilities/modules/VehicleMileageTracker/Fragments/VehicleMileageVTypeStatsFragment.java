package com.itachi1706.cheesecakeutilities.modules.VehicleMileageTracker.Fragments;


import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.collection.ArrayMap;

import com.google.firebase.database.DataSnapshot;
import com.itachi1706.cheesecakeutilities.modules.VehicleMileageTracker.Objects.Vehicle;
import com.itachi1706.cheesecakeutilities.modules.VehicleMileageTracker.VehMileageFirebaseUtils;
import com.itachi1706.cheesecakeutilities.util.FirebaseValueEventListener;
import com.itachi1706.cheesecakeutilities.objects.DualLineString;

import java.util.ArrayList;
import java.util.List;

import static com.itachi1706.cheesecakeutilities.modules.VehicleMileageTracker.VehMileageFirebaseUtils.FB_REC_STATS;
import static com.itachi1706.cheesecakeutilities.modules.VehicleMileageTracker.VehMileageFirebaseUtils.FB_REC_USER;
import static com.itachi1706.cheesecakeutilities.util.FirebaseUtils.Companion;

/**
 * Created by Kenneth on 31/8/2017.
 * for com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.Fragments in CheesecakeUtilities
 */

public class VehicleMileageVTypeStatsFragment extends VehicleMileageFragmentBase {

    private ArrayMap<String, String> vehicles;
    private boolean done = false;

    @Override
    public void onResume() {
        super.onResume();
        if (vehicles == null) {
            refreshLayout.setRefreshing(true);
            VehMileageFirebaseUtils.getVehicleMileageDatabase().child("vehicles")
                    .addListenerForSingleValueEvent(new FirebaseValueEventListener("VehicleMileageStats", "getStatisticsVehType-Legend") {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    vehicles = new ArrayMap<>();
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        if (ds.hasChildren()) {
                            for (DataSnapshot ds1 : ds.getChildren()) {
                                Vehicle v = ds1.getValue(Vehicle.class);
                                assert v != null;
                                vehicles.put(ds1.getKey(), v.getName());
                            }
                        }
                    }
                    done = true;
                    updateStats();
                }
            });
        } else {
            done = true;
            updateStats();
        }
    }

    public void updateStats() {
        if (!done) return;
        final String user_id = VehMileageFirebaseUtils.getFirebaseUIDFromSharedPref(sp);
        if (user_id.equalsIgnoreCase("nien")) {
            // Fail, return to login activity
            Toast.makeText(getActivity(), "Invalid Login Token, please re-login", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!refreshLayout.isRefreshing()) refreshLayout.setRefreshing(true);
        VehMileageFirebaseUtils.getVehicleMileageDatabase().child(FB_REC_USER).child(user_id).child(FB_REC_STATS)
                .child("vehicleTypes").addListenerForSingleValueEvent(new FirebaseValueEventListener("VehicleMileageStats", "getStatisticsVehType-Data") {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<DualLineString> stats = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (vehicles.containsKey(ds.getKey()))
                        stats.add(new DualLineString("Total Mileage with " + vehicles.get(ds.getKey()), Companion.parseData(ds.getValue(Double.class), decimal) + " km"));
                }
                if (refreshLayout.isRefreshing()) refreshLayout.setRefreshing(false);
                adapter.update(stats);
                adapter.notifyDataSetChanged();
            }
        });
    }

}
