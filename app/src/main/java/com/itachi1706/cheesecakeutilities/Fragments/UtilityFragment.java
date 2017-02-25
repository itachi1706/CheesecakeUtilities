package com.itachi1706.cheesecakeutilities.Fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
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
    FirebaseRemoteConfig mFirebaseRemoteConfig;
    private static final long FIREBASE_REFRESH_TIME = 10800L;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main_menu, container, false);

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

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);
        mFirebaseRemoteConfig.fetch(FIREBASE_REFRESH_TIME).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.i("RemoteConfig", "Values Updated from server");
                    mFirebaseRemoteConfig.activateFetched();
                    updateAdapter();
                } else
                    Log.i("RemoteConfig", "Values failed to update");
            }
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateAdapter();
    }

    private void updateAdapter() {
        // Remove hidden items
        List<String> menuitems = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.mainmenu)));
        List<String> hiddenitems = new ArrayList<>(Arrays.asList(sp.getString("utilHidden", "").split("\\|\\|\\|")));
        List<String> moreStuffToHide = new ArrayList<>(Arrays.asList(mFirebaseRemoteConfig.getString("serverHide").split("\\|\\|\\|")));
        hiddenitems.addAll(moreStuffToHide);
        for (String s : hiddenitems) {
            menuitems.remove(s);
        }

        adapter.updateList(menuitems);
        adapter.notifyDataSetChanged();
    }

}
