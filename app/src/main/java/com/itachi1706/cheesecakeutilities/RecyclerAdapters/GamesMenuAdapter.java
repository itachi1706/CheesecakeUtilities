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
 * Created by itachi1706 on 10/29/2016.
 * For com.itachi1706.cheesecakeutilities.RecyclerAdapters in Cheesecake Utilities.
 */
public class GamesMenuAdapter extends RecyclerView.Adapter<GamesMenuAdapter.GamesMenuHolder> {
    private List<String> stringList;
    private Activity mActivity;

    public GamesMenuAdapter(Activity activity, List<String> strings)
    {
        this.mActivity = activity;
        this.stringList = strings;
    }

    public GamesMenuAdapter(Activity activity, String[] strings)
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
    public void onBindViewHolder(GamesMenuHolder stringViewHolder, int i)
    {
        String s  = stringList.get(i);
        stringViewHolder.title.setText(s);
    }

    @Override
    public GamesMenuHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.recyclerview_default_simple_list_item_1, viewGroup, false);

        return new GamesMenuHolder(itemView);
    }


    class GamesMenuHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        protected TextView title;

        GamesMenuHolder(View v)
        {
            super(v);
            title = (TextView) v.findViewById(R.id.text1);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            String link = title.getText().toString();
            Log.i("GamesMenuAdapter", "Clicked on " + link);
            int index = Arrays.asList(v.getContext().getResources().getStringArray(R.array.gamesmenu)).indexOf(link);
            String className = v.getContext().getResources().getStringArray(R.array.gamesmenulink)[index];
            Log.i("GamesMenuAdapter", "Attempting to navigate to " + className);
            try {
                Class classObj = Class.forName(className);
                mActivity.startActivity(new Intent(mActivity, classObj));
            } catch (ClassNotFoundException e) {
                Log.e("GamesMenuAdapter", "Class Not Found: " + className);
                e.printStackTrace();
            }
        }

    }
}
