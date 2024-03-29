package com.itachi1706.cheesecakeutilities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.itachi1706.appupdater.AppUpdateInitializer;
import com.itachi1706.appupdater.object.CAAnalytics;
import com.itachi1706.appupdater.utils.AnalyticsHelper;
import com.itachi1706.cheesecakeutilities.features.biometric_auth.AuthenticationActivity;
import com.itachi1706.cheesecakeutilities.fragments.GamesFragment;
import com.itachi1706.cheesecakeutilities.fragments.UtilityFragment;
import com.itachi1706.cheesecakeutilities.recycleradapters.ViewPagerAdapter;
import com.itachi1706.cheesecakeutilities.util.CommonMethods;
import com.itachi1706.cheesecakeutilities.util.CommonVariables;
import com.itachi1706.cheesecakeutilities.util.LogInit;
import com.itachi1706.helperlib.helpers.PrefHelper;

public class MainMenuActivity extends AppCompatActivity {

    Toolbar toolbar;
    ViewPager pager;
    TabLayout tabLayout;
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sp = PrefHelper.getDefaultSharedPreferences(this);
        PrefHelper.handleDefaultThemeSwitch(sp.getString("app_theme", "batterydefault"));
        super.onCreate(savedInstanceState);

        // Error Handling
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG);
        LogInit.initLogger();
        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        AnalyticsHelper helper = new AnalyticsHelper(this, true);
        final Runnable analyticsRunner = () -> {
            CAAnalytics analytics = helper.getData();
            if (analytics != null) setAnalyticsData(true, firebaseAnalytics, analytics); // Update Firebase Analytics User Properties
            else setAnalyticsData(false, firebaseAnalytics, null);
        };
        analyticsRunner.run();
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, null);

        setContentView(R.layout.activity_main_menu_tabbed);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        pager = findViewById(R.id.main_viewpager);
        tabLayout = findViewById(R.id.main_tablayout);

        setupViewPager(pager);
        tabLayout.setupWithViewPager(pager);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);

        // Do Authentication
        if (!this.getIntent().getBooleanExtra("authagain", true)) {
            checkForUpdate();
            return;
        }
        if (CommonMethods.isGlobalLocked(sp)) startActivityForResult(new Intent(this, AuthenticationActivity.class), REQUEST_AUTH);
    }

    private void setAnalyticsData(boolean enabled, FirebaseAnalytics firebaseAnalytics, CAAnalytics analytics) {
        firebaseAnalytics.setUserProperty("debug_mode", (enabled) ? analytics.isDebug() + "" : null);
        firebaseAnalytics.setUserProperty("device_manufacturer", (enabled) ? analytics.getdManufacturer() : null);
        firebaseAnalytics.setUserProperty("device_codename", (enabled) ? analytics.getdCodename() : null);
        firebaseAnalytics.setUserProperty("device_fingerprint", (enabled) ? analytics.getdFingerprint() : null);
        firebaseAnalytics.setUserProperty("device_cpu_abi", (enabled) ? analytics.getdCPU() : null);
        firebaseAnalytics.setUserProperty("device_tags", (enabled) ? analytics.getdTags() : null);
        firebaseAnalytics.setUserProperty("app_version_code", (enabled) ? Integer.toString(analytics.getAppVerCode()) : null);
        firebaseAnalytics.setUserProperty("android_sec_patch", (enabled) ? analytics.getSdkPatch() : null);
        firebaseAnalytics.setUserProperty("AndroidOS", (enabled) ? Integer.toString(analytics.getSdkver()) : null);
    }

    private void setupViewPager(ViewPager viewPager)
    {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        adapter.addFrag(new UtilityFragment(), "Utilities");
        adapter.addFrag(new GamesFragment(), "Games");

        viewPager.setAdapter(adapter);
    }

    private final int REQUEST_AUTH = 2;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_AUTH) {
            if (resultCode == RESULT_CANCELED) finish();
            else if (resultCode == RESULT_OK) checkForUpdate();
        }
    }

    private void checkForUpdate() {
        new AppUpdateInitializer(this, sp, R.drawable.notification_icon, CommonVariables.BASE_SERVER_URL, true).setOnlyOnWifiCheck(true).checkForUpdate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.settings) {
            startActivity(new Intent(this, GeneralSettingsActivity.class));
            return true;
        } else if (id == R.id.exit) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
