package com.itachi1706.cheesecakeutilities.Modules.SGPsi;

import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.itachi1706.cheesecakeutilities.Fragments.GamesFragment;
import com.itachi1706.cheesecakeutilities.Fragments.UtilityFragment;
import com.itachi1706.cheesecakeutilities.R;
import com.itachi1706.cheesecakeutilities.ViewPagerAdapter;

public class PsiGraphActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu_tabbed);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ViewPager pager = findViewById(R.id.main_viewpager);
        TabLayout tabLayout = findViewById(R.id.main_tablayout);

        setupViewPager(pager);
        tabLayout.setupWithViewPager(pager);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
    }

    private void setupViewPager(ViewPager viewPager)
    {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        Bundle pmB = new Bundle();
        Bundle psiB = new Bundle();
        pmB.putString("key", "pm");
        psiB.putString("key", "psi");
        Fragment f1 = new PsiGraphFragment();
        Fragment f2 = new PsiGraphFragment();
        f1.setArguments(pmB);
        f2.setArguments(psiB);
        adapter.addFrag(f1, "1-Hour PM 2.5");
        adapter.addFrag(f2, "24-Hours PSI");

        viewPager.setAdapter(adapter);
    }
}
