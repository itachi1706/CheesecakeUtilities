package com.itachi1706.cheesecakeutilities.Modules.SystemInformation;

import android.graphics.Color;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.WindowManager;

import com.itachi1706.cheesecakeutilities.BaseActivity;
import com.itachi1706.cheesecakeutilities.Modules.SystemInformation.Fragments.BatteryFragment;
import com.itachi1706.cheesecakeutilities.Modules.SystemInformation.Fragments.DeviceFragment;
import com.itachi1706.cheesecakeutilities.Modules.SystemInformation.Fragments.GpuFragment;
import com.itachi1706.cheesecakeutilities.Modules.SystemInformation.Fragments.NetworkFragment;
import com.itachi1706.cheesecakeutilities.Modules.SystemInformation.Fragments.SensorsFragment;
import com.itachi1706.cheesecakeutilities.Modules.SystemInformation.Fragments.StorageFragment;
import com.itachi1706.cheesecakeutilities.Modules.SystemInformation.Fragments.SystemFragment;
import com.itachi1706.cheesecakeutilities.R;

import java.util.Locale;

public class ParentFragmentActivity extends BaseActivity {

    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: return new SystemFragment();
                case 1: return new DeviceFragment();
                case 2: return new BatteryFragment();
                case 3: return new NetworkFragment();
                case 4: return new SensorsFragment();
                case 5: return new GpuFragment();
                case 6: return new StorageFragment();
                default: return null;
            }
        }

        @Override
        public int getCount() {
            return 7;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0: return ParentFragmentActivity.this.getString(R.string.title_section1).toUpperCase(l);
                case 1: return ParentFragmentActivity.this.getString(R.string.title_section2).toUpperCase(l);
                case 2: return ParentFragmentActivity.this.getString(R.string.title_section3).toUpperCase(l);
                case 3: return ParentFragmentActivity.this.getString(R.string.title_section4).toUpperCase(l);
                case 4: return ParentFragmentActivity.this.getString(R.string.title_section5).toUpperCase(l);
                case 5: return ParentFragmentActivity.this.getString(R.string.title_section6).toUpperCase(l);
                case 6: return ParentFragmentActivity.this.getString(R.string.title_section7).toUpperCase(l);
                default: return null;
            }
        }
    }

    @Override
    public String getHelpDescription() {
        return "Retrieves current system information of your device";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_info);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        this.mViewPager = (ViewPager) findViewById(R.id.pager);
        this.mViewPager.setAdapter(this.mSectionsPagerAdapter);

        TabLayout layout = (TabLayout) findViewById(R.id.sliding_tabs);
        layout.setupWithViewPager(this.mViewPager);
        layout.setTabTextColors(Color.LTGRAY, Color.WHITE);
        if (VERSION.SDK_INT >= 19) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }
}
