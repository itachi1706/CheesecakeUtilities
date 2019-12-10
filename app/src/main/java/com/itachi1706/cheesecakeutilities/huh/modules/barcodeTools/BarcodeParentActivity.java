package com.itachi1706.cheesecakeutilities.huh.modules.barcodeTools;

import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.widget.Toolbar;

import com.itachi1706.cheesecakeutilities.BaseModuleActivity;
import com.itachi1706.cheesecakeutilities.huh.modules.barcodeTools.fragments.BarcodeGeneratorFragment;
import com.itachi1706.cheesecakeutilities.huh.modules.barcodeTools.fragments.BarcodeScannerFragment;
import com.itachi1706.cheesecakeutilities.R;
import com.itachi1706.cheesecakeutilities.huh.util.CommonMethods;
import com.itachi1706.cheesecakeutilities.ViewPagerAdapter;

/**
 * Created by Kenneth on 24/12/2017.
 * for com.itachi1706.cheesecakeutilities.Modules.BarcodeTools in CheesecakeUtilities
 */

public class BarcodeParentActivity extends BaseModuleActivity {

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
        CommonMethods.disableAutofill(getWindow().getDecorView());

        setupViewPager(pager);
        tabLayout.setupWithViewPager(pager);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
    }

    private void setupViewPager(ViewPager viewPager)
    {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        adapter.addFrag(new BarcodeScannerFragment(), "Scan");
        adapter.addFrag(new BarcodeGeneratorFragment(), "Generate");

        viewPager.setAdapter(adapter);
    }
}
