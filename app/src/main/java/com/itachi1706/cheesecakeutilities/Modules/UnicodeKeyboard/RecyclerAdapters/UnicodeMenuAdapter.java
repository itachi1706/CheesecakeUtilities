package com.itachi1706.cheesecakeutilities.Modules.UnicodeKeyboard.RecyclerAdapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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
        stringViewHolder.title.setSelected(true);
    }

    @Override
    public UnicodeMenuHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.recyclerview_default_simple_list_item_1, viewGroup, false);

        return new UnicodeMenuHolder(itemView);
    }


    class UnicodeMenuHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        protected TextView title;

        UnicodeMenuHolder(View v)
        {
            super(v);
            title = (TextView) v.findViewById(R.id.text1);
            title.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            title.setMarqueeRepeatLimit(-1);
            title.setHorizontallyScrolling(true);
            v.setOnClickListener(this);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                v.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            String emoji = title.getText().toString();
            ClipboardManager clipboard = (ClipboardManager) v.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("unicode", emoji);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(v.getContext(), emoji + "\ncopied to clipboard", Toast.LENGTH_LONG).show();
        }

        @Override
        @RequiresApi(api = android.os.Build.VERSION_CODES.N)
        public boolean onLongClick(View v) {
            String emoji = title.getText().toString();
            ClipData clip = ClipData.newPlainText("unicode", emoji);
            View.DragShadowBuilder dragShadowBuilder = new View.DragShadowBuilder(v);
            v.startDragAndDrop(clip, dragShadowBuilder, true, View.DRAG_FLAG_GLOBAL|View.DRAG_FLAG_GLOBAL_URI_READ|
                    View.DRAG_FLAG_GLOBAL_PERSISTABLE_URI_PERMISSION);
            return true;
        }
    }
}
