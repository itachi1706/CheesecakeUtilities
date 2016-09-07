package com.itachi1706.cheesecakeutilities.RecyclerAdapters;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.itachi1706.cheesecakeutilities.R;

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
            String link = title.getText().toString();
            Log.i("MainMenuAdapter", "Clicked on " + link);
            int index = Arrays.asList(v.getContext().getResources().getStringArray(R.array.mainmenu)).indexOf(link);
            String className = v.getContext().getResources().getStringArray(R.array.mainmenulink)[index];
            Log.i("MainMenuAdapter", "Attempting to navigate to " + className);
            try {
                Class classObj = Class.forName(className);
                mActivity.startActivity(new Intent(mActivity, classObj));
            } catch (ClassNotFoundException e) {
                Log.e("MainMenuAdapter", "Class Not Found: " + className);
                e.printStackTrace();
            }
        }

    }
}
