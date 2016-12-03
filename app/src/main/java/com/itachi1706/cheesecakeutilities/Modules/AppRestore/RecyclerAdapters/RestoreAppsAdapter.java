package com.itachi1706.cheesecakeutilities.Modules.AppRestore.RecyclerAdapters;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.itachi1706.cheesecakeutilities.Modules.AppRestore.Objects.RestoreAppsItemsBase;
import com.itachi1706.cheesecakeutilities.Modules.AppRestore.Objects.RestoreAppsItemsFooter;
import com.itachi1706.cheesecakeutilities.Modules.AppRestore.Objects.RestoreAppsItemsHeader;
import com.itachi1706.cheesecakeutilities.Modules.ListApplications.ListApplicationsDetailActivity;
import com.itachi1706.cheesecakeutilities.Modules.ListApplications.Objects.AppsItem;
import com.itachi1706.cheesecakeutilities.R;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by itachi1706 on 3/12/2016.
 * For com.itachi1706.cheesecakeutilities.Modules.AppRestore.RecyclerAdapters in Cheesecake Utilities.
 */
public class RestoreAppsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<RestoreAppsItemsBase> appsList;
    
    private static final int BASE_HEADER = 0;
    private static final int BASE_DETAIL = 1;

    public RestoreAppsAdapter(List<RestoreAppsItemsBase> strings)
    {
        this.appsList = strings;
    }

    public RestoreAppsAdapter(RestoreAppsItemsBase[] strings)
    {
        this.appsList = Arrays.asList(strings);
    }

    @Override
    public int getItemCount()
    {
        return appsList.size();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder appsViewHolder, int i)
    {
        RestoreAppsItemsBase base = appsList.get(i);
        if (base instanceof RestoreAppsItemsHeader) {
            RestoreAppsItemsHeader header = (RestoreAppsItemsHeader) base;
            HeaderAppsViewHolder holder = (HeaderAppsViewHolder) appsViewHolder;
            holder.appName.setText(header.getAppName());
            holder.appApiVersion.setText(header.getApiVersion() + "");
            holder.appPackageName.setText(header.getPackageName());
            holder.appIcon.setImageDrawable(header.getIcon());
            holder.appVersion.setText("Version: " + header.getVersion());
        } else if (base instanceof RestoreAppsItemsFooter) {
            RestoreAppsItemsFooter detail = (RestoreAppsItemsFooter) base;
            DetailAppsViewHolder holder = (DetailAppsViewHolder) appsViewHolder;
            holder.appName.setText(detail.getAppName());
            holder.appApiVersion.setText(detail.getApiVersion() + "");
            holder.appPackageName.setText(detail.getPackageName());
            holder.appIcon.setImageDrawable(detail.getIcon());
            holder.appVersion.setText("Version: " + detail.getVersion());
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        switch (i) {
            case BASE_HEADER:
                View headerView = LayoutInflater.
                        from(viewGroup.getContext()).
                        inflate(R.layout.recyclerview_applist_apps, viewGroup, false);
                return new HeaderAppsViewHolder(headerView);
            case BASE_DETAIL:
                View detailsView = LayoutInflater.
                        from(viewGroup.getContext()).
                        inflate(R.layout.recyclerview_applist_apps, viewGroup, false);
                return new DetailAppsViewHolder(detailsView);
        }

        return null;
    }

    public int getHeaderIndex(RestoreAppsItemsHeader header){
        int index = -1;
        for (int i = 0; i < appsList.size(); i++){
            if (!(appsList.get(i) instanceof RestoreAppsItemsHeader)) continue;
            if (appsList.get(i) == header) index = i;
        }
        return index;
    }

    // TODO: Stub
    public void expand(RestoreAppsItemsHeader header){
        if (!header.hasChild()) return;
        List<RestoreAppsItemsFooter> child = header.getChild();

        int add = getHeaderIndex(header);
        if (add == -1) return;

        appsList.get(add).setIsExpanded(true);

        ListIterator iterator = child.listIterator(child.size());
        while (iterator.hasPrevious()){
            RestoreAppsItemsFooter statistics = (RestoreAppsItemsFooter) iterator.previous();
            appsList.add(add+1, statistics);
            notifyItemInserted(add + 1);
        }
    }

    // TODO: Stub
    public void retract(RestoreAppsItemsHeader header){
        if (!header.hasChild()) return;
        List<RestoreAppsItemsFooter> child = header.getChild();

        int remove = getHeaderIndex(header);
        if (remove == -1) return;

        appsList.get(remove).setIsExpanded(false);

        for (Iterator<RestoreAppsItemsBase> iterator = appsList.iterator(); iterator.hasNext();){
            RestoreAppsItemsBase base = iterator.next();
            if (base instanceof RestoreAppsItemsHeader) continue;

            RestoreAppsItemsFooter item = (RestoreAppsItemsFooter) base;
            for (RestoreAppsItemsFooter childItems : child){
                if (childItems == item){
                    iterator.remove();
                    notifyItemRemoved(remove + 1);
                    break;
                }
            }
        }
    }


    class HeaderAppsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView appName, count;
        ImageView appIcon;

        HeaderAppsViewHolder(View v)
        {
            super(v);
            appName = (TextView) v.findViewById(R.id.tvAppName);
            appIcon = (ImageView) v.findViewById(R.id.iv_icon);
            count = (TextView) v.findViewById(R.id.tvCount);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            // TODO: Expand detail
        }
    }

    class DetailAppsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView appName;

        DetailAppsViewHolder(View v)
        {
            super(v);
            appName = (TextView) v.findViewById(R.id.tvAppName);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            // TODO: Restore
        }
    }
}
