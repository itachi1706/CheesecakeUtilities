package com.itachi1706.cheesecakeutilities.Modules.BarcodeTools;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;

import com.itachi1706.cheesecakeutilities.BaseActivity;
import com.itachi1706.cheesecakeutilities.Modules.BarcodeTools.Fragments.BarcodeScannerFragment;
import com.itachi1706.cheesecakeutilities.R;
import com.itachi1706.cheesecakeutilities.ViewPagerAdapter;

/**
 * Created by Kenneth on 24/12/2017.
 * for com.itachi1706.cheesecakeutilities.Modules.BarcodeTools in CheesecakeUtilities
 */

public class BarcodeParentActivity extends BaseActivity {

    Toolbar toolbar;
    ViewPager pager;
    TabLayout tabLayout;

    @Override
    public String getHelpDescription() {
        return "Utility used to create or scan barcode (2D/3D)\n\nExamples: ISBN, QR Code etc";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu_tabbed);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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

        adapter.addFrag(new BarcodeScannerFragment(), "Scan");
        adapter.addFrag(new BarcodeScannerFragment(), "Generate");

        viewPager.setAdapter(adapter);
    }
}
