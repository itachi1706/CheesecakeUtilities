package com.itachi1706.cheesecakeutilities.features.utilityManagement;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.itachi1706.helperlib.helpers.PrefHelper;
import com.itachi1706.cheesecakeutilities.R;
import com.itachi1706.cheesecakeutilities.features.biometricAuth.AuthenticationActivity;
import com.itachi1706.cheesecakeutilities.recyclerAdapters.ManageUtilAdapter;

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

        SharedPreferences sp = PrefHelper.getDefaultSharedPreferences(this);
        String hiddenUtil = sp.getString("utilHidden", "");
        String lockedUtil = sp.getString("utilLocked", "");

        // Set up layout
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);
        List<String> menuitemsList = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.mainmenu)));
        List<String> firebaseHidden = new ArrayList<>(Arrays.asList(mFirebaseRemoteConfig.getString("serverHide").split("\\|\\|\\|")));
        menuitemsList.removeAll(firebaseHidden);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
