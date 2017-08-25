package com.itachi1706.cheesecakeutilities.Features.UtilityManagement;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.itachi1706.cheesecakeutilities.Features.FingerprintAuth.AuthenticationActivity;
import com.itachi1706.cheesecakeutilities.R;
import com.itachi1706.cheesecakeutilities.RecyclerAdapters.ManageUtilAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ManageUtilityActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    FirebaseRemoteConfig mFirebaseRemoteConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_utility);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        recyclerView = findViewById(R.id.hide_util_recycler);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String hiddenUtil = sp.getString("utilHidden", "");
        String lockedUtil = sp.getString("utilLocked", "");

        // Set up layout
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);
        List<String> menuitemsList = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.mainmenu)));
        List<String> firebaseHidden = new ArrayList<>(Arrays.asList(mFirebaseRemoteConfig.getString("serverHide").split("\\|\\|\\|")));
        for (String s : firebaseHidden) {
            menuitemsList.remove(s);
        }
        String[] menuitems = menuitemsList.toArray(new String[menuitemsList.size()]);
        ManageUtilAdapter adapter = new ManageUtilAdapter(menuitems, hiddenUtil, lockedUtil);
        recyclerView.setAdapter(adapter);

        // Do Authentication
        startActivityForResult(new Intent(this, AuthenticationActivity.class), REQUEST_AUTH);

    }

    private final int REQUEST_AUTH = 3;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_AUTH) {
            if (resultCode == RESULT_CANCELED) {
                finish();
            }
        }
    }
}
