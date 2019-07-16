package com.itachi1706.cheesecakeutilities.RecyclerAdapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.itachi1706.appupdater.Util.DeprecationHelper;
import com.itachi1706.cheesecakeutilities.R;
import com.itachi1706.cheesecakeutilities.objects.DualLineString;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

/**
 * Created by itachi1706 on 2/20/2016.
 * For com.itachi1706.cheesecakeutilities.RecyclerAdapters in Cheesecake Utilities.
 */
public class DualLineStringRecyclerAdapter extends RecyclerView.Adapter<DualLineStringRecyclerAdapter.StringViewHolder> {
    private List<DualLineString> stringList;
    private boolean announce = false, htmlformat = false;
    private View.OnClickListener onClickListener = null;
    private View.OnLongClickListener onLongClickListener = null;
    private View.OnCreateContextMenuListener createContextMenuListener = null;

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

    public void setOnClickListener(View.OnClickListener listener) {
        onClickListener = listener;
    }

    public void setOnLongClickListener(View.OnLongClickListener listener) { onLongClickListener = listener; }

    public void setOnCreateContextMenuListener(View.OnCreateContextMenuListener listener) { createContextMenuListener = listener; }

    public void update(List<DualLineString> strings) {
        this.stringList = strings;
    }

    public void update(DualLineString[] strings) {
        this.update(Arrays.asList(strings));
    }

    @Override
    public int getItemCount()
    {
        return stringList.size();
    }

    @Override
    public void onBindViewHolder(StringViewHolder stringViewHolder, int i)
    {
        DualLineString s  = stringList.get(i);
        stringViewHolder.title.setText((htmlformat) ? DeprecationHelper.Html.fromHtml(s.getMain()) : s.getMain());
        stringViewHolder.subtitle.setText((htmlformat) ? DeprecationHelper.Html.fromHtml(s.getSub()) : s.getSub());
    }

    @Override
    public StringViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.recyclerview_default_simple_list_item_2, viewGroup, false);

        return new StringViewHolder(itemView, onClickListener, onLongClickListener);
    }


    public class StringViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        protected TextView title, subtitle;

        public StringViewHolder(View v, @Nullable View.OnClickListener listener, @Nullable View.OnLongClickListener longClickListener)
        {
            super(v);
            title = v.findViewById(android.R.id.text1);
            subtitle = v.findViewById(android.R.id.text2);
            v.setOnClickListener((listener == null) ? this : listener);
            v.setTag(this);
            if (longClickListener != null) v.setOnLongClickListener(longClickListener);
            if (createContextMenuListener != null) v.setOnCreateContextMenuListener(createContextMenuListener);
        }

        @Override
        public void onClick(View v) {
            if (announce) Toast.makeText(v.getContext(), title.getText(), Toast.LENGTH_SHORT).show();
        }

    }
}
