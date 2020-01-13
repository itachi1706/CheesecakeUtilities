package com.itachi1706.cheesecakeutilities.recyclerAdapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.itachi1706.cheesecakeutilities.R;
import com.itachi1706.cheesecakeutilities.util.LogHelper;
import com.itachi1706.helperlib.helpers.PrefHelper;

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
    private static final String TAG = "ManageUtilAdapter";

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

    @NonNull
    @Override
    public ManageUtilHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.recyclerview_manage_utility, viewGroup, false);

        return new ManageUtilHolder(itemView);
    }


    class ManageUtilHolder extends RecyclerView.ViewHolder implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

        protected TextView title;
        ImageButton lockToggle;
        CheckBox enabledToggle;
        LinearLayout linearLayout;
        protected SharedPreferences sp;
        Context mContext;
        boolean isVisible = true, isLocked = false;

        ManageUtilHolder(View v)
        {
            super(v);
            mContext = v.getContext();
            sp = PrefHelper.getDefaultSharedPreferences(v.getContext());
            title = v.findViewById(R.id.text1);
            enabledToggle = v.findViewById(R.id.enabled);
            enabledToggle.setOnCheckedChangeListener(this);
            lockToggle = v.findViewById(R.id.lockutilbutton);
            lockToggle.setOnClickListener(this);
            linearLayout = v.findViewById(R.id.recycler_layout);
            linearLayout.setOnClickListener(this);
            if (sp.getBoolean("global_applock", true)) lockToggle.setVisibility(View.GONE);
        }

        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.lockutilbutton) {
                String link = title.getText().toString();

                List<String> utils = getLockedAsArray();
                if (!isLocked) {
                    // Protect Utility
                    if (!utils.contains(link)) utils.add(link);
                    LogHelper.i(TAG, link + " protected");
                    isLocked = true;
                } else {
                    utils.remove(link);
                    LogHelper.i(TAG, link + " unprotected");
                    isLocked = false;
                }
                lockedUtil = convertHiddenOrLockedArrayToString(utils);
                sp.edit().putString("utilLocked", lockedUtil).apply();
                updateIcon();
            } else if (view.getId() == R.id.recycler_layout) {
                enabledToggle.setChecked(!enabledToggle.isChecked());
            }
        }

        private boolean ignoreChanges = false;

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (ignoreChanges) return;
            String link = title.getText().toString();
            List<String> utils = getHiddenAsArray();
            if (!isChecked) {
                // Hide Utility
                if (!utils.contains(link)) utils.add(link);
                isVisible = false;
                LogHelper.i(TAG, link + " hidden");
            } else {
                utils.remove(link);
                isVisible = true;
                LogHelper.i(TAG, link + " shown");
            }
            hiddenUtil = convertHiddenOrLockedArrayToString(utils);
            sp.edit().putString("utilHidden", hiddenUtil).apply();
            updateIcon();
        }

        private List<String> getHiddenAsArray() {
            return new ArrayList<>(Arrays.asList(hiddenUtil.split("\\|\\|\\|")));
        }

        private List<String> getLockedAsArray() {
            return new ArrayList<>(Arrays.asList(lockedUtil.split("\\|\\|\\|")));
        }

        private String convertHiddenOrLockedArrayToString(List<String> array) {
            StringBuilder res = new StringBuilder();
            for (String s : array) {
                if (res.length() == 0) res.append(s);
                else res.append("|||").append(s);
            }
            return res.toString();
        }

        private void updateIcon() {
            lockToggle.setImageDrawable(isLocked ?
                    VectorDrawableCompat.create(mContext.getResources(), R.drawable.ic_lock, mContext.getTheme()) :
                    VectorDrawableCompat.create(mContext.getResources(), R.drawable.ic_lock_open, mContext.getTheme()));
            ignoreChanges = true;
            enabledToggle.setChecked(isVisible);
            ignoreChanges = false;
            if (PrefHelper.isNightModeEnabled(mContext)) {
                // Set to white color
                int white = ContextCompat.getColor(mContext, R.color.white);
                ImageViewCompat.setImageTintList(lockToggle, ColorStateList.valueOf(white));
            }
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
