package com.itachi1706.cheesecakeutilities.RecyclerAdapters;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.itachi1706.cheesecakeutilities.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by itachi1706 on 2/20/2016.
 * For com.itachi1706.cheesecakeutilities.RecyclerAdapters in Cheesecake Utilities.
 */
public class HideUtilAdapter extends RecyclerView.Adapter<HideUtilAdapter.HideUtilHolder> {
    private List<String> stringList;
    private String hiddenUtil;

    public HideUtilAdapter(List<String> strings, String hiddenUtil)
    {
        this.stringList = strings;
        this.hiddenUtil = hiddenUtil;
    }

    public HideUtilAdapter(String[] strings, String hiddenUtil)
    {
        this.stringList = Arrays.asList(strings);
        this.hiddenUtil = hiddenUtil;
    }

    @Override
    public int getItemCount()
    {
        return stringList.size();
    }

    @Override
    public void onBindViewHolder(HideUtilHolder stringViewHolder, int i)
    {
        String s  = stringList.get(i);
        stringViewHolder.title.setText(s);
        stringViewHolder.checkBox.setChecked(stringViewHolder.isHidden(s));
    }

    @Override
    public HideUtilHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.recyclerview_hide_utility, viewGroup, false);

        return new HideUtilHolder(itemView);
    }


    class HideUtilHolder extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener {

        protected TextView title;
        CheckBox checkBox;
        protected SharedPreferences sp;

        HideUtilHolder(View v)
        {
            super(v);
            sp = PreferenceManager.getDefaultSharedPreferences(v.getContext());
            title = (TextView) v.findViewById(R.id.text1);
            checkBox = (CheckBox) v.findViewById(R.id.checkbox);
            checkBox.setOnCheckedChangeListener(this);
        }

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            String link = title.getText().toString();

            if (b) {
                // Hide Utility
                List<String> utils = getHiddenAsArray();
                if (!utils.contains(link)) utils.add(link);
                hiddenUtil = convertHiddenArrayToString(utils);
                sp.edit().putString("utilHidden", hiddenUtil).apply();
                Log.i("HideUtilAdapter", link + " hidden");
            } else {
                // Unhide Utility
                List<String> utils = getHiddenAsArray();
                utils.remove(link);
                hiddenUtil = convertHiddenArrayToString(utils);
                sp.edit().putString("utilHidden", hiddenUtil).apply();
                Log.i("HideUtilAdapter", link + " shown");
            }
        }

        private List<String> getHiddenAsArray() {
            return new ArrayList<>(Arrays.asList(hiddenUtil.split("\\|\\|\\|")));
        }
        private String convertHiddenArrayToString(List<String> array) {
            String res = "";
            for (String s : array) {
                if (res.isEmpty()) res += s;
                else res += "|||" + s;
            }
            return res;
        }

        private boolean isHidden(String util) {
            List<String> hide = getHiddenAsArray();
            return hide.contains(util);
        }
    }
}
