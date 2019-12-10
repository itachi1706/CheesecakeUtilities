package com.itachi1706.cheesecakeutilities.huh.modules.listApplications;

import android.os.Bundle;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.widget.Toolbar;

import com.itachi1706.cheesecakeutilities.BaseModuleActivity;
import com.itachi1706.cheesecakeutilities.huh.modules.listApplications.fragments.ListApplicationFragment;
import com.itachi1706.cheesecakeutilities.huh.modules.listApplications.fragments.RestoreAppFragment;
import com.itachi1706.cheesecakeutilities.R;
import com.itachi1706.cheesecakeutilities.ViewPagerAdapter;

public class ListApplicationsActivity extends BaseModuleActivity {

    @Override
    public String getHelpDescription() {
        return "List all applications installed on your device and their targeted API Levels " +
                "and allows you to restore applications you previously backed up.\n\nComing Soon: " +
                "Showing a more understandable API level";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu_tabbed);

        Toolbar toolbar = findViewById(R.id.toolbar);
        ((AppBarLayout.LayoutParams) toolbar.getLayoutParams())
                .setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null && getSupportActionBar().isShowing()) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ViewPager pager = findViewById(R.id.main_viewpager);
        TabLayout tabLayout = findViewById(R.id.main_tablayout);

        setupViewPager(pager);
        tabLayout.setupWithViewPager(pager);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        ((AppBarLayout.LayoutParams)tabLayout.getLayoutParams()).setScrollFlags(0);
    }

    private void setupViewPager(ViewPager viewPager)
    {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new ListApplicationFragment(), "App List");
        adapter.addFrag(new RestoreAppFragment(), "Restore App");
        viewPager.setAdapter(adapter);
    }
}
