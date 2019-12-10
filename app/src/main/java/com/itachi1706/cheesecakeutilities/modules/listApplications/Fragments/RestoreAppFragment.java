package com.itachi1706.cheesecakeutilities.modules.listApplications.Fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.itachi1706.cheesecakeutilities.modules.listApplications.objects.RestoreAppsItemsBase;
import com.itachi1706.cheesecakeutilities.modules.listApplications.recyclerAdapters.LoadAppsRestoreTask;
import com.itachi1706.cheesecakeutilities.modules.listApplications.recyclerAdapters.RestoreAppsAdapter;
import com.itachi1706.cheesecakeutilities.R;

/**
 * Created by Kenneth on 18/4/2018.
 * for com.itachi1706.cheesecakeutilities.Modules.ListApplications.Fragments in CheesecakeUtilities
 */
public class RestoreAppFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar bar;
    private TextView label;

    public RestoreAppFragment() {
        // Required Constructor for Fragments
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_list_applications, container, false);

        recyclerView = v.findViewById(R.id.list_app_recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        bar = v.findViewById(R.id.list_app_pb);
        label = v.findViewById(R.id.list_app_pb_label);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        RestoreAppsAdapter adapter = new RestoreAppsAdapter(new RestoreAppsItemsBase[0]);
        recyclerView.setAdapter(adapter);
        bar.setVisibility(View.VISIBLE);
        label.setVisibility(View.VISIBLE);
        if (getContext() == null) return;
        new LoadAppsRestoreTask(getContext(), finalAdapter -> {
            if (finalAdapter != null) recyclerView.setAdapter(finalAdapter);
            bar.setVisibility(View.GONE);
            label.setVisibility(View.GONE);
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
