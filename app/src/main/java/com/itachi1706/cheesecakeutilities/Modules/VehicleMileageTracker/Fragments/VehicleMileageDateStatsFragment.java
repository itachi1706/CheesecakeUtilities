package com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.Fragments;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.FirebaseUtils.FB_REC_STATS;
import static com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.FirebaseUtils.FB_REC_USER;
import static com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.FirebaseUtils.MILEAGE_DEC;
import static com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.FirebaseUtils.parseData;

/**
 * Created by Kenneth on 31/8/2017.
 * for com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.Fragments in CheesecakeUtilities
 */

public class VehicleMileageDateStatsFragment extends Fragment {

    DualLineStringRecyclerAdapter adapter;
    SwipeRefreshLayout refreshLayout;
    SharedPreferences sp;
    private boolean decimal;

    public VehicleMileageDateStatsFragment() {
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
            decimal = sp.getBoolean(MILEAGE_DEC, true);
        }
        refreshLayout = v.findViewById(R.id.pull_to_refresh);
        refreshLayout.setOnRefreshListener(this::updateStats);
        refreshLayout.setColorSchemeResources(
                R.color.refresh_progress_1,
                R.color.refresh_progress_2);


        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateStats();
    }

    public void updateStats() {
        final String user_id = sp.getString("firebase_uid", "nien");
        if (user_id.equalsIgnoreCase("nien")) {
            // Fail, return to login activity
            Toast.makeText(getActivity(), "Invalid Login Token, please re-login", Toast.LENGTH_SHORT).show();
            return;
        }
        refreshLayout.setRefreshing(true);
        FirebaseUtils.getFirebaseDatabase().getReference().child(FB_REC_USER).child(user_id).child(FB_REC_STATS)
                .child("timeRecords").child("perDate").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<DualLineString> stats = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Date d = new Date(Long.parseLong(ds.getKey()));
                    SimpleDateFormat sd = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
                    stats.add(new DualLineString("Total Mileage for " + sd.format(d), parseData(ds.getValue(Double.class), decimal) + " km"));
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
