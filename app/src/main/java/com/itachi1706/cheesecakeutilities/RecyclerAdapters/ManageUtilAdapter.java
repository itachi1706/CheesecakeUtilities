package com.itachi1706.cheesecakeutilities.RecyclerAdapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.itachi1706.cheesecakeutilities.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by itachi1706 on 2/20/2016.
 * For com.itachi1706.cheesecakeutilities.RecyclerAdapters in Cheesecake Utilities.
 */
public class ManageUtilAdapter extends RecyclerView.Adapter<ManageUtilAdapter.ManageUtilHolder> {
    private List<String> stringList;
    private String hiddenUtil, lockedUtil;

    public ManageUtilAdapter(List<String> strings, String hiddenUtil, String lockedUtil)
    {
        this.stringList = strings;
        this.hiddenUtil = hiddenUtil;
        this.lockedUtil = lockedUtil;
    }

    public ManageUtilAdapter(String[] strings, String hiddenUtil, String lockedUtil)
    {
        this.stringList = Arrays.asList(strings);
        this.hiddenUtil = hiddenUtil;
        this.lockedUtil = lockedUtil;
    }

    @Override
    public int getItemCount()
    {
        return stringList.size();
    }

    @Override
    public void onBindViewHolder(ManageUtilHolder stringViewHolder, int i)
    {
        String s  = stringList.get(i);
        stringViewHolder.title.setText(s);
        stringViewHolder.isVisible = !stringViewHolder.isHidden(s);
        stringViewHolder.isLocked = stringViewHolder.isLocked(s);
        stringViewHolder.updateIcon();
    }

    @Override
    public ManageUtilHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.recyclerview_manage_utility, viewGroup, false);

        return new ManageUtilHolder(itemView);
    }


    class ManageUtilHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        protected TextView title;
        ImageButton visibleToggle, lockToggle;
        protected SharedPreferences sp;
        Context mContext;
        boolean isVisible = true, isLocked = false;

        ManageUtilHolder(View v)
        {
            super(v);
            mContext = v.getContext();
            sp = PreferenceManager.getDefaultSharedPreferences(v.getContext());
            title = (TextView) v.findViewById(R.id.text1);
            visibleToggle = (ImageButton) v.findViewById(R.id.checkbox);
            visibleToggle.setOnClickListener(this);
            lockToggle = (ImageButton) v.findViewById(R.id.lockutilbutton);
            lockToggle.setOnClickListener(this);
            if (sp.getBoolean("global_applock", true)) lockToggle.setVisibility(View.GONE);
        }

        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.checkbox) {
                String link = title.getText().toString();

                if (isVisible) {
                    // Hide Utility
                    List<String> utils = getHiddenAsArray();
                    if (!utils.contains(link)) utils.add(link);
                    hiddenUtil = convertHiddenOrLockedArrayToString(utils);
                    sp.edit().putString("utilHidden", hiddenUtil).apply();
                    isVisible = false;
                    Log.i("ManageUtilAdapter", link + " hidden");
                } else {
                    List<String> utils = getHiddenAsArray();
                    utils.remove(link);
                    hiddenUtil = convertHiddenOrLockedArrayToString(utils);
                    sp.edit().putString("utilHidden", hiddenUtil).apply();
                    Log.i("ManageUtilAdapter", link + " shown");
                    isVisible = true;
                }
                updateIcon();
            } else if (view.getId() == R.id.lockutilbutton) {
                String link = title.getText().toString();

                if (!isLocked) {
                    // Hide Utility
                    List<String> utils = getLockedAsArray();
                    if (!utils.contains(link)) utils.add(link);
                    lockedUtil = convertHiddenOrLockedArrayToString(utils);
                    sp.edit().putString("utilLocked", lockedUtil).apply();
                    isLocked = true;
                    Log.i("ManageUtilAdapter", link + " protected");
                } else {
                    List<String> utils = getLockedAsArray();
                    utils.remove(link);
                    lockedUtil = convertHiddenOrLockedArrayToString(utils);
                    sp.edit().putString("utilLocked", lockedUtil).apply();
                    Log.i("ManageUtilAdapter", link + " unprotected");
                    isLocked = false;
                }
                updateIcon();
            }

        }

        private List<String> getHiddenAsArray() {
            return new ArrayList<>(Arrays.asList(hiddenUtil.split("\\|\\|\\|")));
        }

        private List<String> getLockedAsArray() {
            return new ArrayList<>(Arrays.asList(lockedUtil.split("\\|\\|\\|")));
        }

        private String convertHiddenOrLockedArrayToString(List<String> array) {
            String res = "";
            for (String s : array) {
                if (res.isEmpty()) res += s;
                else res += "|||" + s;
            }
            return res;
        }

        private void updateIcon() {
            visibleToggle.setImageDrawable(isVisible ?
                    VectorDrawableCompat.create(mContext.getResources(), R.drawable.eye, mContext.getTheme()) :
                    VectorDrawableCompat.create(mContext.getResources(), R.drawable.eye_off, mContext.getTheme()));
            lockToggle.setImageDrawable(isLocked ?
                    VectorDrawableCompat.create(mContext.getResources(), R.drawable.lock, mContext.getTheme()) :
                    VectorDrawableCompat.create(mContext.getResources(), R.drawable.lock_open, mContext.getTheme()));
        }

        private boolean isHidden(String util) {
            List<String> hide = getHiddenAsArray();
            return hide.contains(util);
        }

        private boolean isLocked(String util) {
            List<String> lock = getLockedAsArray();
            return lock.contains(util);
        }
    }
}
