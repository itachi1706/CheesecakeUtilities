package com.itachi1706.cheesecakeutilities.RecyclerAdapters;

import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.itachi1706.cheesecakeutilities.Objects.DualLineString;
import com.itachi1706.cheesecakeutilities.R;

import java.util.Arrays;
import java.util.List;

/**
 * Created by itachi1706 on 2/20/2016.
 * For com.itachi1706.cheesecakeutilities.RecyclerAdapters in Cheesecake Utilities.
 */
public class DualLineStringRecyclerAdapter extends RecyclerView.Adapter<DualLineStringRecyclerAdapter.StringViewHolder> {
    private List<DualLineString> stringList;
    private boolean announce = false, htmlformat = false;

    public DualLineStringRecyclerAdapter(List<DualLineString> strings) {
        this(strings, true);
    }

    public DualLineStringRecyclerAdapter(DualLineString[] strings) {
        this(strings, true);
    }

    public DualLineStringRecyclerAdapter setHtmlFormat(boolean htmlformat) {
        this.htmlformat = htmlformat;
        return this;
    }

    public DualLineStringRecyclerAdapter(List<DualLineString> strings, boolean announce)
    {
        this.stringList = strings;
        this.announce = announce;
    }

    public DualLineStringRecyclerAdapter(DualLineString[] strings, boolean announce)
    {
        this.stringList = Arrays.asList(strings);
        this.announce = announce;
    }

    @Override
    public int getItemCount()
    {
        return stringList.size();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onBindViewHolder(StringViewHolder stringViewHolder, int i)
    {
        DualLineString s  = stringList.get(i);
        stringViewHolder.title.setText((htmlformat) ? Html.fromHtml(s.getMain()) : s.getMain());
        stringViewHolder.subtitle.setText((htmlformat) ? Html.fromHtml(s.getSub()) : s.getSub());
    }

    @Override
    public StringViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.recyclerview_default_simple_list_item_2, viewGroup, false);

        return new StringViewHolder(itemView);
    }


    public class StringViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        protected TextView title, subtitle;

        public StringViewHolder(View v)
        {
            super(v);
            title = (TextView) v.findViewById(android.R.id.text1);
            subtitle = (TextView) v.findViewById(android.R.id.text2);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (announce) Toast.makeText(v.getContext(), title.getText(), Toast.LENGTH_SHORT).show();
        }

    }
}
