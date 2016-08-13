package com.itachi1706.cheesecakeutilities.RecyclerAdapters;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.itachi1706.cheesecakeutilities.CameraDisablerActivity;
import com.itachi1706.cheesecakeutilities.FanfictionCompactorActivity;
import com.itachi1706.cheesecakeutilities.HtcSerialIdentificationActivity;
import com.itachi1706.cheesecakeutilities.ListApplicationsActivity;
import com.itachi1706.cheesecakeutilities.R;
import com.itachi1706.cheesecakeutilities.SpamMessages;
import com.itachi1706.cheesecakeutilities.StringToHexBin;

import java.util.Arrays;
import java.util.List;

/**
 * Created by itachi1706 on 2/20/2016.
 * For com.itachi1706.cheesecakeutilities.RecyclerAdapters in Cheesecake Utilities.
 */
public class MainMenuAdapter extends RecyclerView.Adapter<MainMenuAdapter.MainMenuHolder> {
    private List<String> stringList;
    private Activity mActivity;

    public MainMenuAdapter(Activity activity, List<String> strings)
    {
        this.mActivity = activity;
        this.stringList = strings;
    }

    public MainMenuAdapter(Activity activity, String[] strings)
    {
        this.mActivity = activity;
        this.stringList = Arrays.asList(strings);
    }

    @Override
    public int getItemCount()
    {
        return stringList.size();
    }

    @Override
    public void onBindViewHolder(MainMenuHolder stringViewHolder, int i)
    {
        String s  = stringList.get(i);
        stringViewHolder.title.setText(s);
    }

    @Override
    public MainMenuHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.recyclerview_default_simple_list_item_1, viewGroup, false);

        return new MainMenuHolder(itemView);
    }


    public class MainMenuHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        protected TextView title;

        public MainMenuHolder(View v)
        {
            super(v);
            title = (TextView) v.findViewById(R.id.text1);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (title.getText().toString()) {
                case "String to Binary/Hex Converter": mActivity.startActivity(new Intent(mActivity, StringToHexBin.class)); break;
                case "Message Spam": mActivity.startActivity(new Intent(mActivity, SpamMessages.class)); break;
                case "HTC Serial Number Identification": mActivity.startActivity(new Intent(mActivity, HtcSerialIdentificationActivity.class)); break;
                case "Fanfiction Compactor": mActivity.startActivity(new Intent(mActivity, FanfictionCompactorActivity.class)); break;
                case "Disable Camera": mActivity.startActivity(new Intent(mActivity, CameraDisablerActivity.class)); break;
                case "ListApplicationsActivity": mActivity.startActivity(new Intent(mActivity, ListApplicationsActivity.class)); break;
                default: Toast.makeText(v.getContext(), "This utility is unimplemented!", Toast.LENGTH_SHORT).show(); break;
            }
        }

    }
}
