package com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.Fragments.VehicleMileageDateStatsFragment;
import com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.Fragments.VehicleMileageGeneralStatsFragment;
import com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.Fragments.VehicleMileageMonthStatsFragment;
import com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.Fragments.VehicleMileageVNumberStatsFragment;
import com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.Fragments.VehicleMileageVTypeStatsFragment;
import com.itachi1706.cheesecakeutilities.R;
import com.itachi1706.cheesecakeutilities.ViewPagerAdapter;

public class VehicleMileageStatisticsActivity extends AppCompatActivity {

    Toolbar toolbar;
    ViewPager pager;
    TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu_tabbed);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null && getSupportActionBar().isShowing()) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        pager = findViewById(R.id.main_viewpager);
        tabLayout = findViewById(R.id.main_tablayout);

        setupViewPager(pager);
        tabLayout.setupWithViewPager(pager);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
            case R.id.exit:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}