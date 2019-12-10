package com.itachi1706.cheesecakeutilities.Fragments;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.itachi1706.cheesecakeutilities.R;
import com.itachi1706.cheesecakeutilities.recyclerAdapters.GamesMenuAdapter;

/**
 * Created by Kenneth on 31/1/2017.
 * for com.itachi1706.cheesecakeutilities.Fragments in CheesecakeUtilities
 */

public class GamesFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_recycler_view, container, false);

        RecyclerView recyclerView = v.findViewById(R.id.main_menu_recycler_view);
        if (recyclerView != null) {
            recyclerView.setHasFixedSize(true);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());

            // Set up layout
            String[] menuitems = getResources().getStringArray(R.array.gamesmenu);
            GamesMenuAdapter adapter = new GamesMenuAdapter(getActivity(), menuitems);
            recyclerView.setAdapter(adapter);
        }

        return v;
    }

}
