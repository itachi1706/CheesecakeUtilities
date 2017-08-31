package com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.itachi1706.cheesecakeutilities.Objects.DualLineString;
import com.itachi1706.cheesecakeutilities.R;
import com.itachi1706.cheesecakeutilities.RecyclerAdapters.DualLineStringRecyclerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kenneth on 31/8/2017.
 * for com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.Fragments in CheesecakeUtilities
 */

public class VehicleMileageGeneralStatsFragment extends Fragment {

    DualLineStringRecyclerAdapter adapter;


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
            adapter = new DualLineStringRecyclerAdapter(placeholder, false);
            recyclerView.setAdapter(adapter);
        }

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateStats();
    }

    public void updateStats() {

    }

}
