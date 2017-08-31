package com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.Fragments;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.FirebaseUtils;
import com.itachi1706.cheesecakeutilities.Objects.DualLineString;
import com.itachi1706.cheesecakeutilities.R;
import com.itachi1706.cheesecakeutilities.RecyclerAdapters.DualLineStringRecyclerAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Kenneth on 31/8/2017.
 * for com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.Fragments in CheesecakeUtilities
 */

public class VehicleMileageGeneralStatsFragment extends Fragment {

    DualLineStringRecyclerAdapter adapter;
    SharedPreferences sp;

    private boolean ready = false;
    private ArrayMap<String, String> legend;


    public VehicleMileageGeneralStatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_recycler_view, container, false);

        RecyclerView recyclerView = v.findViewById(R.id.main_menu_recycler_view);
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
            FirebaseUtils.getFirebaseDatabase().getReference().child("stat-legend").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    legend = new ArrayMap<>();
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        legend.put(ds.getKey(), ds.getValue(String.class));
                    }
                    ready = true;
                    updateStats();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    ready = false;
                }
            });
        }

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ready) updateStats();
    }

    public void updateStats() {
        final String user_id = sp.getString("firebase_uid", "nien");
        if (user_id.equalsIgnoreCase("nien")) {
            // Fail, return to login activity
            Toast.makeText(getActivity(), "Invalid Login Token, please re-login", Toast.LENGTH_SHORT).show();
            return;
        }
        FirebaseUtils.getFirebaseDatabase().getReference().child("users").child(user_id).child("statistics").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<DualLineString> stats = new ArrayList<>();
                for (Map.Entry<String, String> i : legend.entrySet()) {
                    if (!dataSnapshot.hasChild(i.getKey())) continue;
                    stats.add(new DualLineString(i.getValue(), dataSnapshot.child(i.getKey()).getValue(Double.class) + " km"));
                }
                adapter.update(stats);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
