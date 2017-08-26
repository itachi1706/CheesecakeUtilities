package com.itachi1706.cheesecakeutilities.RecyclerAdapters;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.itachi1706.cheesecakeutilities.CreateShortcuts;
import com.itachi1706.cheesecakeutilities.R;

import java.util.Arrays;
import java.util.List;

/**
 * Created by itachi1706 on 2/20/2016.
 * For com.itachi1706.cheesecakeutilities.RecyclerAdapters in Cheesecake Utilities.
 */
public class CreateShortcutsAdapter extends RecyclerView.Adapter<CreateShortcutsAdapter.CreateShortcutsHolder> {
    private List<String> stringList;
    private Handler mHandler;

    public CreateShortcutsAdapter(List<String> strings, Handler handler)
    {
        this.stringList = strings;
        this.mHandler = handler;
    }

    @Override
    public int getItemCount()
    {
        return stringList.size();
    }

    @Override
    public void onBindViewHolder(CreateShortcutsHolder stringViewHolder, int i)
    {
        String s  = stringList.get(i);
        stringViewHolder.title.setText(s);
    }

    @Override
    public CreateShortcutsHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.recyclerview_default_simple_list_item_1, viewGroup, false);

        return new CreateShortcutsHolder(itemView);
    }


    class CreateShortcutsHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        protected TextView title;

        CreateShortcutsHolder(View v)
        {
            super(v);
            title = v.findViewById(R.id.text1);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            String link = title.getText().toString();
            Log.i("CreateShortcutsAdapter", "Clicked on " + link);
            int index = Arrays.asList(v.getContext().getResources().getStringArray(R.array.mainmenu)).indexOf(link);
            boolean isGame = false;
            if (index == -1) {
                // Games array
                isGame = true;
                index = Arrays.asList(v.getContext().getResources().getStringArray(R.array.gamesmenu)).indexOf(link);
                if (index == -1) {
                    Message msg = Message.obtain();
                    msg.what = CreateShortcuts.CREATE_SHORTCUT_ADAPTER_FAIL;
                    mHandler.sendMessage(msg);
                    return;
                }
            }
            String className = (isGame) ? v.getContext().getResources().getStringArray(R.array.gamesmenulink)[index] :
                    v.getContext().getResources().getStringArray(R.array.mainmenulink)[index];
            if (className.startsWith(".")) className = "com.itachi1706.cheesecakeutilities" + className;
            Message msg = Message.obtain();
            msg.what = CreateShortcuts.CREATE_SHORTCUT_ADAPTER_DONE;
            Bundle bundle = new Bundle();
            bundle.putString("class", className);
            bundle.putString("title", link);
            msg.setData(bundle);
            mHandler.sendMessage(msg);
        }

    }
}
