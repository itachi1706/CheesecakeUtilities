/*
 * CardInfoActivity.java
 *
 * Copyright (C) 2011 Eric Butler
 * Copyright 2015-2018 Michael Farrell <micolous+git@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.itachi1706.cheesecakeutilities.Modules.CEPASReader.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.itachi1706.cheesecakeutilities.CheesecakeUtilitiesApplication;
import com.itachi1706.cheesecakeutilities.Modules.CEPASReader.card.Card;
import com.itachi1706.cheesecakeutilities.Modules.CEPASReader.fragment.CardBalanceFragment;
import com.itachi1706.cheesecakeutilities.Modules.CEPASReader.fragment.CardInfoFragment;
import com.itachi1706.cheesecakeutilities.Modules.CEPASReader.fragment.CardTripsFragment;
import com.itachi1706.cheesecakeutilities.Modules.CEPASReader.fragment.UnauthorizedCardFragment;
import com.itachi1706.cheesecakeutilities.Modules.CEPASReader.transit.TransitData;
import com.itachi1706.cheesecakeutilities.Modules.CEPASReader.transit.unknown.UnauthorizedTransitData;
import com.itachi1706.cheesecakeutilities.Modules.CEPASReader.ui.TabPagerAdapter;
import com.itachi1706.cheesecakeutilities.Modules.CEPASReader.util.Utils;
import com.itachi1706.cheesecakeutilities.R;

import androidx.appcompat.app.ActionBar;
import androidx.viewpager.widget.ViewPager;

/**
 * @author Eric Butler
 */
public class CardInfoActivity extends SGCardReaderActivity {
    public static final String EXTRA_TRANSIT_DATA = "transit_data";
    public static final String EXTRA_CARD = "com.itachi1706.sgcardreader.EXTRA_CARD";

    private static final String KEY_SELECTED_TAB = "selected_tab";

    private Card mCard;
    private TransitData mTransitData;
    private TabPagerAdapter mTabsAdapter;

    private boolean mShowOnlineServices = false;
    private boolean mShowMoreInfo = false;
    private Menu mMenu = null;


    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_card_info);
        final ViewPager viewPager = findViewById(R.id.pager);
        mTabsAdapter = new TabPagerAdapter(this, viewPager);

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.loading);

        new AsyncTask<Void, Void, Void>() {
            private Exception mException;

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    String xml = getIntent().getStringExtra("card");
                    mCard = Card.fromXml(CheesecakeUtilitiesApplication.getInstance().getSerializer(), xml);
                    mTransitData = mCard.parseTransitData();
                } catch (Exception ex) {
                    mException = ex;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                findViewById(R.id.loading).setVisibility(View.GONE);
                findViewById(R.id.pager).setVisibility(View.VISIBLE);

                if (mException != null) {
                    if (mCard == null) {
                        Utils.showErrorAndFinish(CardInfoActivity.this, mException);
                    } else {
                        Log.e("CardInfoActivity", "Error parsing transit data", mException);
                        Toast.makeText(getApplicationContext(), "Error parsing transit data", Toast.LENGTH_LONG).show();
                        finish();
                    }
                    return;
                }

                if (mTransitData == null) {
                    Toast.makeText(getApplicationContext(), "This card is not supported", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }

                try {

                    String titleSerial = "";
                    actionBar.setTitle(mTransitData.getCardName());
                    actionBar.setSubtitle(titleSerial);

                    Bundle args = new Bundle();
                    args.putString(EXTRA_CARD,
                            mCard.toXml(CheesecakeUtilitiesApplication.getInstance().getSerializer()));
                    args.putParcelable(EXTRA_TRANSIT_DATA, mTransitData);

                    if (mTransitData instanceof UnauthorizedTransitData) {
                        mTabsAdapter.addTab(actionBar.newTab(), UnauthorizedCardFragment.class, args);
                        return;
                    }

                    if (mTransitData.getBalances() != null) {
                        mTabsAdapter.addTab(actionBar.newTab().setText(R.string.balances_and_subscriptions),
                                CardBalanceFragment.class, args);
                    }

                    if (mTransitData.getTrips() != null) {
                        mTabsAdapter.addTab(actionBar.newTab().setText(R.string.history), CardTripsFragment.class, args);
                    }

                    if (mTransitData.getInfo() != null) {
                        mTabsAdapter.addTab(actionBar.newTab().setText(R.string.info), CardInfoFragment.class, args);
                    }

                    if (mTabsAdapter.getCount() > 1) {
                        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
                    }

                    String w = mTransitData.getWarning();
                    boolean hasUnknownStation = mTransitData.hasUnknownStations();
                    if (w != null || hasUnknownStation) {
                        findViewById(R.id.need_stations).setVisibility(View.VISIBLE);
                        String txt = "";
                        if (hasUnknownStation)
                            txt = getString(R.string.need_stations);
                        if (w != null && txt.length() > 0)
                            txt += "\n";
                        if (w != null)
                            txt += w;

                        ((TextView) findViewById(R.id.need_stations_text)).setText(txt);
                        findViewById(R.id.need_stations_button).setVisibility(hasUnknownStation
                                ? View.VISIBLE : View.GONE);
                    }

                    mShowMoreInfo = mTransitData.getMoreInfoPage() != null;
                    mShowOnlineServices = mTransitData.getOnlineServicesPage() != null;

                    if (mMenu != null) {
                        mMenu.findItem(R.id.online_services).setVisible(mShowOnlineServices);
                        mMenu.findItem(R.id.more_info).setVisible(mShowMoreInfo);
                    }

                    if (savedInstanceState != null) {
                        viewPager.setCurrentItem(savedInstanceState.getInt(KEY_SELECTED_TAB, 0));
                    }
                } catch (Exception e) {
                    Log.e("CardInfoActivity", "Error parsing transit data", e);
                    Toast.makeText(getApplicationContext(), "Error parsing transit data", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }.execute();
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putInt(KEY_SELECTED_TAB, ((ViewPager) findViewById(R.id.pager)).getCurrentItem());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.module_cepas_cardinfo, menu);
        menu.findItem(R.id.online_services).setVisible(mShowOnlineServices);
        menu.findItem(R.id.more_info).setVisible(mShowMoreInfo);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;

            case R.id.more_info:
                if (mTransitData.getMoreInfoPage() != null) {
                    startActivity(new Intent(Intent.ACTION_VIEW, mTransitData.getMoreInfoPage()));
                    return true;
                }
                break;

            case R.id.online_services:
                if (mTransitData.getOnlineServicesPage() != null) {
                    startActivity(new Intent(Intent.ACTION_VIEW, mTransitData.getOnlineServicesPage()));
                    return true;
                }

        }

        return false;
    }
}
