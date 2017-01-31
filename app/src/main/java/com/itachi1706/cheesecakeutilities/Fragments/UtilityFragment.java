package com.itachi1706.cheesecakeutilities.Fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.lzyzsd.circleprogress.DonutProgress;
import com.itachi1706.cheesecakeutilities.R;
import com.itachi1706.cheesecakeutilities.RecyclerAdapters.MainMenuAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Kenneth on 31/1/2017.
 * for com.itachi1706.cheesecakeutilities.Fragments in CheesecakeUtilities
 */

public class UtilityFragment extends Fragment {

    MainMenuAdapter adapter;
    SharedPreferences sp;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.content_main_menu, container, false);

        sp = PreferenceManager.getDefaultSharedPreferences(getActivity());

        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.main_menu_recycler_view);
        if (recyclerView != null) {
            recyclerView.setHasFixedSize(true);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());

            // Set up layout
            List<String> menuitems = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.mainmenu)));
            adapter = new MainMenuAdapter(getActivity(), menuitems);
            recyclerView.setAdapter(adapter);
        }

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Remove hidden items
        List<String> menuitems = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.mainmenu)));
        List<String> hiddenitems = new ArrayList<>(Arrays.asList(sp.getString("utilHidden", "").split("\\|\\|\\|")));
        for (String s : hiddenitems) {
            menuitems.remove(s);
        }

        adapter.updateList(menuitems);
        adapter.notifyDataSetChanged();

    }

}
