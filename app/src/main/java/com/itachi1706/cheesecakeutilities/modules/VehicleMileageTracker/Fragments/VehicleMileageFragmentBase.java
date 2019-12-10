package com.itachi1706.cheesecakeutilities.modules.VehicleMileageTracker.Fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.itachi1706.appupdater.Util.PrefHelper;
import com.itachi1706.cheesecakeutilities.objects.DualLineString;
import com.itachi1706.cheesecakeutilities.R;
import com.itachi1706.cheesecakeutilities.recyclerAdapters.DualLineStringRecyclerAdapter;

import java.util.ArrayList;
import java.util.List;

import static com.itachi1706.cheesecakeutilities.modules.VehicleMileageTracker.VehMileageFirebaseUtils.MILEAGE_DEC;

/**
 * Created by Kenneth on 18/4/2018.
 * for com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.Fragments in CheesecakeUtilities
 */
public abstract class VehicleMileageFragmentBase extends Fragment {

    public DualLineStringRecyclerAdapter adapter;
    public SwipeRefreshLayout refreshLayout;
    public SharedPreferences sp;
    public boolean decimal;

    public VehicleMileageFragmentBase() {
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

            sp = PrefHelper.getDefaultSharedPreferences(getActivity().getApplicationContext());
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

    public abstract void updateStats();

}
