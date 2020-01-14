package com.itachi1706.cheesecakeutilities.recyclerAdapters;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.itachi1706.cheesecakeutilities.R;
import com.itachi1706.helperlib.helpers.LogHelper;

import java.util.Arrays;
import java.util.LinkedList;
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

    public void updateList(List<String> newList) {
        this.stringList = newList;
    }

    @Override
    public MainMenuHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.recyclerview_default_simple_list_item_1, viewGroup, false);

        return new MainMenuHolder(itemView);
    }


    class MainMenuHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        protected TextView title;

        MainMenuHolder(View v)
        {
            super(v);
            title = v.findViewById(R.id.text1);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            String link = title.getText().toString();
            LogHelper.i("MainMenuAdapter", "Clicked on " + link);
            int index = Arrays.asList(v.getContext().getResources().getStringArray(R.array.mainmenu)).indexOf(link);
            String className = v.getContext().getResources().getStringArray(R.array.mainmenulink)[index];
            if (className.startsWith(".")) className = "com.itachi1706.cheesecakeutilities" + className;
            LogHelper.i("MainMenuAdapter", "Attempting to navigate to " + className);
            try {
                Class classObj = Class.forName(className);
                Intent intent = new Intent(mActivity, classObj);
                intent.putExtra("menuitem", link);
                mActivity.startActivity(intent);

                // Firebase Analytics Event Logging
                FirebaseAnalytics analytics = FirebaseAnalytics.getInstance(mActivity);
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, link);
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "utility_launched");
                analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                LogHelper.i("Firebase", "Logged Event Utility Launched: " + link);

                // Add dynamic shortcuts
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                    ShortcutManager shortcutManager = v.getContext().getSystemService(ShortcutManager.class);
                    LinkedList<ShortcutInfo> infos = new LinkedList<>(shortcutManager.getDynamicShortcuts());
                    final int shortcutCount = shortcutManager.getMaxShortcutCountPerActivity() - 2;
                    if (infos.size() >= shortcutCount) {
                        LogHelper.i("ShortcutManager", "Dynamic Shortcuts more than " + shortcutCount
                                + ". Removing extras");
                        do {
                            infos.removeLast();
                        } while (infos.size() > shortcutCount);
                    }
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.putExtra("globalcheck", true);
                    ShortcutInfo newShortcut = new ShortcutInfo.Builder(v.getContext(), link.replace(" ", ""))
                            .setShortLabel(link).setLongLabel(link)
                            .setIcon(Icon.createWithResource(v.getContext(), R.mipmap.ic_launcher_round))
                            .setIntent(intent).build();

                    infos.add(newShortcut);
                    shortcutManager.setDynamicShortcuts(infos);
                }
            } catch (ClassNotFoundException e) {
                LogHelper.e("MainMenuAdapter", "Class Not Found: " + className);
                e.printStackTrace();
            }
        }

    }
}
