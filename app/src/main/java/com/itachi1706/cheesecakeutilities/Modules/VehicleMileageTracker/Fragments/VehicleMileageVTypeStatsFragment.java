package com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.Fragments;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.util.ArrayMap;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.FirebaseUtils;
import com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.Objects.Vehicle;
import com.itachi1706.cheesecakeutilities.Objects.DualLineString;
import com.itachi1706.cheesecakeutilities.R;
import com.itachi1706.cheesecakeutilities.RecyclerAdapters.DualLineStringRecyclerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kenneth on 31/8/2017.
 * for com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.Fragments in CheesecakeUtilities
 */

public class VehicleMileageVTypeStatsFragment extends Fragment {

    DualLineStringRecyclerAdapter adapter;
    SwipeRefreshLayout refreshLayout;
    SharedPreferences sp;

    private ArrayMap<String, String> vehicles;


    public VehicleMileageVTypeStatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_refreshable_recycler_view, container, false);

        RecyclerView recyclerView = v.findViewById(R.id.recycler_view);
        if (recyclerView != null) {
            recyclerView.setHasFixedSize(true);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());

            // Set up blank layout for now
            List<DualLineString> placeholder = new ArrayList<>();
            placeholder.add(new DualLineString("Loading...", "Calculating statistics..."));
            adapter = new DualLineStringRecyclerAdapter(placeholder, false);
            recyclerView.setAdapter(adapter);

            sp = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        }
        refreshLayout = v.findViewById(R.id.pull_to_refresh);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateStats();
            }
        });
        refreshLayout.setColorSchemeResources(
                R.color.refresh_progress_1,
                R.color.refresh_progress_2);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (vehicles == null) {
            refreshLayout.setRefreshing(true);
            FirebaseUtils.getFirebaseDatabase().getReference().child("vehicles").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
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
                    updateStats();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("VehicleMileageStats", "Error in Firebase DB call (VType-Legend): " + databaseError.getDetails());
                }
            });
        } else updateStats();
    }

    public void updateStats() {
        final String user_id = sp.getString("firebase_uid", "nien");
        if (user_id.equalsIgnoreCase("nien")) {
            // Fail, return to login activity
            Toast.makeText(getActivity(), "Invalid Login Token, please re-login", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!refreshLayout.isRefreshing()) refreshLayout.setRefreshing(true);
        FirebaseUtils.getFirebaseDatabase().getReference().child("users").child(user_id).child("statistics")
                .child("vehicleTypes").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<DualLineString> stats = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (vehicles.containsKey(ds.getKey()))
                        stats.add(new DualLineString("Total Mileage with " + vehicles.get(ds.getKey()), ds.getValue(Double.class) + " km"));
                }
                if (refreshLayout.isRefreshing()) refreshLayout.setRefreshing(false);
                adapter.update(stats);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("VehicleMileageStats", "Error in Firebase DB call (VType-Data): " + databaseError.getDetails());
            }
        });
    }

}
