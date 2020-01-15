package com.itachi1706.cheesecakeutilities.modules.barcodeTools;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.itachi1706.cheesecakeutilities.BaseModuleActivity;
import com.itachi1706.cheesecakeutilities.R;
import com.itachi1706.cheesecakeutilities.ViewPagerAdapter;
import com.itachi1706.cheesecakeutilities.modules.barcodeTools.fragments.BarcodeGeneratorFragment;
import com.itachi1706.cheesecakeutilities.modules.barcodeTools.fragments.BarcodeScannerFragment;
import com.itachi1706.cheesecakeutilities.util.CommonMethods;

/**
 * Created by Kenneth on 24/12/2017.
 * for com.itachi1706.cheesecakeutilities.modules.BarcodeTools in CheesecakeUtilities
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.modules_barcode, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.view_history) {
            startActivity(new Intent(this, BarcodeHistoryActivity.class));
            return true;
        }
        else return super.onOptionsItemSelected(item);
    }
}
