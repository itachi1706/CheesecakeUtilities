package com.itachi1706.cheesecakeutilities.Modules.UnicodeKeyboard.RecyclerAdapters;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.itachi1706.cheesecakeutilities.R;

import java.util.Arrays;
import java.util.List;

/**
 * Created by itachi1706 on 12/30/2016.
 * For com.itachi1706.cheesecakeutilities.Modules.UnicodeKeyboard.RecyclerAdapters in Cheesecake Utilities.
 */
public class UnicodeMenuAdapter extends RecyclerView.Adapter<UnicodeMenuAdapter.UnicodeMenuHolder> {
    private List<String> stringList;

    public UnicodeMenuAdapter(List<String> strings)
    {
        this.stringList = strings;
    }

    public UnicodeMenuAdapter(String[] strings)
    {
        this.stringList = Arrays.asList(strings);
    }

    @Override
    public int getItemCount()
    {
        return stringList.size();
    }

    @Override
    public void onBindViewHolder(UnicodeMenuHolder stringViewHolder, int i)
    {
        String s  = stringList.get(i);
        stringViewHolder.title.setText(s);
    }

    @Override
    public UnicodeMenuHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.recyclerview_default_simple_list_item_1, viewGroup, false);

        return new UnicodeMenuHolder(itemView);
    }


    class UnicodeMenuHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        protected TextView title;

        UnicodeMenuHolder(View v)
        {
            super(v);
            title = (TextView) v.findViewById(R.id.text1);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            String link = title.getText().toString();
            ClipboardManager clipboard = (ClipboardManager) v.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("unicode", link);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(v.getContext(), "Copied to clipboard", Toast.LENGTH_LONG).show();
        }

    }
}
