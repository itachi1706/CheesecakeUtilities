package com.itachi1706.cheesecakeutilities.Modules.SGPsi;

import android.os.Bundle;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.itachi1706.cheesecakeutilities.R;
import com.itachi1706.cheesecakeutilities.ViewPagerAdapter;

import static android.content.res.Configuration.ORIENTATION_PORTRAIT;

public class PsiGraphActivity extends AppCompatActivity {

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

        if (getResources().getConfiguration().orientation == ORIENTATION_PORTRAIT)
            Toast.makeText(this, "Graphs are best viewed in landscape mode", Toast.LENGTH_SHORT).show();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
