package com.itachi1706.cheesecakeutilities.Features.UtilityManagement;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.itachi1706.cheesecakeutilities.Features.FingerprintAuth.AuthenticationActivity;
import com.itachi1706.cheesecakeutilities.R;
import com.itachi1706.cheesecakeutilities.RecyclerAdapters.ManageUtilAdapter;

public class ManageUtilityActivity extends AppCompatActivity {

    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_utility);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        recyclerView = (RecyclerView) findViewById(R.id.hide_util_recycler);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String hiddenUtil = sp.getString("utilHidden", "");

        // Set up layout
        String[] menuitems = getResources().getStringArray(R.array.mainmenu);
        ManageUtilAdapter adapter = new ManageUtilAdapter(menuitems, hiddenUtil);
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
