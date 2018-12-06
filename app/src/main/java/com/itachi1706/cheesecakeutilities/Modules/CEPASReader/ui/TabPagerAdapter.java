/*
 * TabPagerAdapter.java
 *
 * Copyright (C) 2011 Eric Butler <eric@codebutler.com>
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

package com.itachi1706.cheesecakeutilities.Modules.CEPASReader.ui;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Parcelable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.view.View;

import java.util.ArrayList;

import com.itachi1706.cheesecakeutilities.R;

public class TabPagerAdapter extends PagerAdapter implements ActionBar.TabListener, ViewPager.OnPageChangeListener {
    private final Activity mActivity;
    private final ActionBar mActionBar;
    private final ViewPager mViewPager;
    private final ArrayList<TabInfo> mTabs = new ArrayList<>();
    private FragmentTransaction mCurTransaction = null;

    public TabPagerAdapter(Activity activity, ViewPager pager) {
        mActivity = activity;
        mActionBar = activity.getActionBar();
        mViewPager = pager;
        mViewPager.setAdapter(this);
        mViewPager.setOnPageChangeListener(this);
    }

    public void addTab(ActionBar.Tab tab, Class<?> clss, Bundle args) {
        TabInfo info = new TabInfo(clss, args);
        tab.setTag(info);
        tab.setTabListener(this);
        mTabs.add(info);
        mActionBar.addTab(tab);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mTabs.size();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void startUpdate(View view) {
    }

    @SuppressLint("CommitTransaction")
    @Override
    @SuppressWarnings("deprecation")
    public Object instantiateItem(View view, int position) {
        TabInfo info = mTabs.get(position);

        if (mCurTransaction == null) {
            mCurTransaction = mActivity.getFragmentManager().beginTransaction();
        }

        Fragment fragment = Fragment.instantiate(mActivity, info.mClass.getName(), info.mArgs);
        mCurTransaction.add(R.id.pager, fragment);
        return fragment;
    }

    @SuppressLint("CommitTransaction")
    @Override
    @SuppressWarnings("deprecation")
    public void destroyItem(View view, int i, Object object) {
        if (mCurTransaction == null) {
            mCurTransaction = mActivity.getFragmentManager().beginTransaction();
        }
        mCurTransaction.hide((Fragment) object);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void finishUpdate(View view) {
        if (mCurTransaction != null) {
            mCurTransaction.commitAllowingStateLoss();
            mCurTransaction = null;
            mActivity.getFragmentManager().executePendingTransactions();
        }
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return ((Fragment) object).getView() == view;
    }

    @Override
    public Parcelable saveState() {
        return null;
    }

    @Override
    public void restoreState(Parcelable parcelable, ClassLoader classLoader) {
    }

    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    public void onPageSelected(int position) {
        mActionBar.setSelectedNavigationItem(position);
    }

    public void onPageScrollStateChanged(int state) {
    }

    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        Object tag = tab.getTag();
        for (int i = 0; i < mTabs.size(); i++) {
            if (mTabs.get(i) == tag) {
                mViewPager.setCurrentItem(i);
            }
        }
    }

    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    private static final class TabInfo {
        private final Class<?> mClass;
        private final Bundle mArgs;

        public TabInfo(Class<?> klass, Bundle args) {
            mClass = klass;
            mArgs = args;
        }
    }
}
