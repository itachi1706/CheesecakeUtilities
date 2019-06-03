package com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.Fragments;


import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.VehMileageFirebaseUtils;
import com.itachi1706.cheesecakeutilities.Objects.DualLineString;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.VehMileageFirebaseUtils.FB_REC_STATS;
import static com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.VehMileageFirebaseUtils.FB_REC_USER;
import static com.itachi1706.cheesecakeutilities.Util.FirebaseUtils.Companion;

/**
 * Created by Kenneth on 31/8/2017.
 * for com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.Fragments in CheesecakeUtilities
 */

public class VehicleMileageDateStatsFragment extends VehicleMileageFragmentBase {

    public void updateStats() {
        final String user_id = sp.getString("firebase_uid", "nien");
        if (user_id.equalsIgnoreCase("nien")) {
            // Fail, return to login activity
            Toast.makeText(getActivity(), "Invalid Login Token, please re-login", Toast.LENGTH_SHORT).show();
            return;
        }
        refreshLayout.setRefreshing(true);
        VehMileageFirebaseUtils.Companion.getFirebaseDatabase().getReference().child(FB_REC_USER).child(user_id).child(FB_REC_STATS)
                .child("timeRecords").child("perDate").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<DualLineString> stats = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Date d = new Date(Long.parseLong(ds.getKey()));
                    SimpleDateFormat sd = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
                    stats.add(new DualLineString("Total Mileage for " + sd.format(d), Companion.parseData(ds.getValue(Double.class), decimal) + " km"));
                }
                Collections.reverse(stats);
                if (refreshLayout.isRefreshing()) refreshLayout.setRefreshing(false);
                adapter.update(stats);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
