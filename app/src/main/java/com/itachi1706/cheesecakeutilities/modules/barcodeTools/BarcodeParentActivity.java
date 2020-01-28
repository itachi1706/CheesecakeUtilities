package com.itachi1706.cheesecakeutilities.modules.barcodeTools;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.itachi1706.cheesecakeutilities.BaseModuleActivity;
import com.itachi1706.cheesecakeutilities.R;
import com.itachi1706.cheesecakeutilities.ViewPagerAdapter;
import com.itachi1706.cheesecakeutilities.modules.barcodeTools.fragments.BarcodeFragInterface;
import com.itachi1706.cheesecakeutilities.modules.barcodeTools.fragments.BarcodeGeneratorFragment;
import com.itachi1706.cheesecakeutilities.modules.barcodeTools.fragments.BarcodeScannerFragment;
import com.itachi1706.cheesecakeutilities.util.CommonMethods;
import com.itachi1706.helperlib.helpers.LogHelper;

import org.jetbrains.annotations.Nullable;

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
            startActivityForResult(new Intent(this, BarcodeHistoryActivity.class), RC_HIST);
            return true;
        }
        else return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == RC_HIST && resultCode == RESULT_OK && data != null) {
            // Handle and switch places
            String type = data.getStringExtra("barcodeType");
            String bcObject = data.getStringExtra("selection");
            ViewPagerAdapter adapter = (ViewPagerAdapter) pager.getAdapter();
            if (adapter == null) return;
            if (adapter.getCount() < 2) {
                LogHelper.e(TAG, "Error: Fragment Count Less than 2. Current Count: " + adapter.getCount());
                return;
            }
            int pos = (type.equals(BarcodeHelper.SP_BARCODE_SCANNED)) ? 0 : 1;
            Fragment frag = adapter.getItem(pos);
            if (!(frag instanceof BarcodeFragInterface)) return;
            BarcodeFragInterface bFrag = (BarcodeFragInterface) frag;
            bFrag.setHistoryBarcode(bcObject);
            pager.setCurrentItem(pos);
        }
        else super.onActivityResult(requestCode, resultCode, data);
    }

    private static final int RC_HIST = 10;
    private static final String TAG = "BarcodeParent";
}
