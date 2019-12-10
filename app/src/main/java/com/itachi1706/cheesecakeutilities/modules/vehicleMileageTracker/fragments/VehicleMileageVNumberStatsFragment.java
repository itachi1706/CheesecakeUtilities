package com.itachi1706.cheesecakeutilities.modules.vehicleMileageTracker.fragments;


import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.itachi1706.cheesecakeutilities.modules.vehicleMileageTracker.VehMileageFirebaseUtils;
import com.itachi1706.cheesecakeutilities.util.FirebaseValueEventListener;
import com.itachi1706.cheesecakeutilities.objects.DualLineString;

import java.util.ArrayList;
import java.util.List;

import static com.itachi1706.cheesecakeutilities.modules.vehicleMileageTracker.VehMileageFirebaseUtils.FB_REC_STATS;
import static com.itachi1706.cheesecakeutilities.modules.vehicleMileageTracker.VehMileageFirebaseUtils.FB_REC_USER;
import static com.itachi1706.cheesecakeutilities.util.FirebaseUtils.Companion;

/**
 * Created by Kenneth on 31/8/2017.
 * for com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.Fragments in CheesecakeUtilities
 */

public class VehicleMileageVNumberStatsFragment extends VehicleMileageFragmentBase {

    public void updateStats() {
        final String user_id = VehMileageFirebaseUtils.getFirebaseUIDFromSharedPref(sp);
        if (user_id.equalsIgnoreCase("nien")) {
            // Fail, return to login activity
            Toast.makeText(getActivity(), "Invalid Login Token, please re-login", Toast.LENGTH_SHORT).show();
            return;
        }
        refreshLayout.setRefreshing(true);
        VehMileageFirebaseUtils.getVehicleMileageDatabase().child(FB_REC_USER).child(user_id).child(FB_REC_STATS)
                .child("vehicleNumberRecords").addListenerForSingleValueEvent(new FirebaseValueEventListener("VehicleMileageStats", "loadStatisticsVehNumber") {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<DualLineString> stats = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    stats.add(new DualLineString("Total Mileage with " + ds.getKey(), Companion.parseData(ds.getValue(Double.class), decimal) + " km"));
                }
                if (refreshLayout.isRefreshing()) refreshLayout.setRefreshing(false);
                adapter.update(stats);
                adapter.notifyDataSetChanged();
            }
        });
    }

}
