package com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.database.DatabaseReference;
import com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.Fragments.VehicleMileageDateStatsFragment;
import com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.Fragments.VehicleMileageGeneralStatsFragment;
import com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.Fragments.VehicleMileageMonthStatsFragment;
import com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.Fragments.VehicleMileageVNumberStatsFragment;
import com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.Fragments.VehicleMileageVTypeStatsFragment;
import com.itachi1706.cheesecakeutilities.R;
import com.itachi1706.cheesecakeutilities.ViewPagerAdapter;

import static com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.FirebaseUtils.FB_REC_STATS;
import static com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.FirebaseUtils.FB_REC_USER;

public class VehicleMileageStatisticsActivity extends AppCompatActivity {

    Toolbar toolbar;
    ViewPager pager;
    TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu_tabbed);

        toolbar = findViewById(R.id.toolbar);
        ((AppBarLayout.LayoutParams) toolbar.getLayoutParams())
                .setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null && getSupportActionBar().isShowing()) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        pager = findViewById(R.id.main_viewpager);
        tabLayout = findViewById(R.id.main_tablayout);
        ((AppBarLayout.LayoutParams) tabLayout.getLayoutParams()).setScrollFlags(0);

        setupViewPager(pager);
        tabLayout.setupWithViewPager(pager);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);

        // Keep Firebase synced
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String user_id = sp.getString("firebase_uid", "nien");
        if (!user_id.equals("nien")) {
            DatabaseReference dbRef = FirebaseUtils.getFirebaseDatabase().getReference();
            dbRef.child(FB_REC_USER).child(user_id).child(FB_REC_STATS).keepSynced(true);
            dbRef.child("stat-legend").keepSynced(true);
            dbRef.child("vehicles").keepSynced(true);
        }
    }

    private void setupViewPager(ViewPager viewPager)
    {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        adapter.addFrag(new VehicleMileageGeneralStatsFragment(), "General");
        adapter.addFrag(new VehicleMileageDateStatsFragment(), "Date");
        adapter.addFrag(new VehicleMileageMonthStatsFragment(), "Month");
        adapter.addFrag(new VehicleMileageVNumberStatsFragment(), "Vehicle Number");
        adapter.addFrag(new VehicleMileageVTypeStatsFragment(), "Vehicle Type");

        viewPager.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.modules_veh_mileage_stats, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.generateReport:
                startActivity(new Intent(this, GenerateMileageRecordActivity.class));
                return true;
            case android.R.id.home:
            case R.id.exit:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
