package com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.Fragments;


import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.collection.ArrayMap;

import com.google.firebase.database.DataSnapshot;
import com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.VehMileageFirebaseUtils;
import com.itachi1706.cheesecakeutilities.Util.FirebaseValueEventListener;
import com.itachi1706.cheesecakeutilities.objects.DualLineString;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.VehMileageFirebaseUtils.FB_REC_STATS;
import static com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.VehMileageFirebaseUtils.FB_REC_USER;
import static com.itachi1706.cheesecakeutilities.Util.FirebaseUtils.Companion;

/**
 * Created by Kenneth on 31/8/2017.
 * for com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.Fragments in CheesecakeUtilities
 */

public class VehicleMileageGeneralStatsFragment extends VehicleMileageFragmentBase {

    private ArrayMap<String, String> legend;
    private boolean done = false;

    @Override
    public void onResume() {
        super.onResume();
        if (legend == null) {
            refreshLayout.setRefreshing(true);
            VehMileageFirebaseUtils.getVehicleMileageDatabase().child("stat-legend")
                    .addListenerForSingleValueEvent(new FirebaseValueEventListener("VehicleMileageStats", "loadStatisticsGeneral-Legend") {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    legend = new ArrayMap<>();
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        legend.put(ds.getKey(), ds.getValue(String.class));
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
        final String user_id = sp.getString("firebase_uid", "nien");
        if (user_id.equalsIgnoreCase("nien")) {
            // Fail, return to login activity
            Toast.makeText(getActivity(), "Invalid Login Token, please re-login", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!refreshLayout.isRefreshing()) refreshLayout.setRefreshing(true);
        VehMileageFirebaseUtils.getVehicleMileageDatabase().child(FB_REC_USER).child(user_id).child(FB_REC_STATS)
                .addListenerForSingleValueEvent(new FirebaseValueEventListener("VehicleMileageStats", "loadStatisticsGeneral-Data") {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<DualLineString> stats = new ArrayList<>();
                for (Map.Entry<String, String> i : legend.entrySet()) {
                    if (!dataSnapshot.hasChild(i.getKey())) continue;
                    stats.add(new DualLineString(i.getValue(), Companion.parseData(dataSnapshot.child(i.getKey()).getValue(Double.class), decimal) + " km"));
                }
                if (refreshLayout.isRefreshing()) refreshLayout.setRefreshing(false);
                adapter.update(stats);
                adapter.notifyDataSetChanged();
            }
        });
    }

}
