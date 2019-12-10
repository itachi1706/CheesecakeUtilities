package com.itachi1706.cheesecakeutilities.modules.listApplications.recyclerAdapters;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.itachi1706.cheesecakeutilities.modules.listApplications.objects.RestoreAppsItemsBase;
import com.itachi1706.cheesecakeutilities.modules.listApplications.objects.RestoreAppsItemsFooter;
import com.itachi1706.cheesecakeutilities.modules.listApplications.objects.RestoreAppsItemsHeader;
import com.itachi1706.cheesecakeutilities.R;
import com.itachi1706.cheesecakeutilities.util.LogHelper;

import java.io.File;
import java.util.Arrays;
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
            HeaderAppsViewHolder headerHolder = (HeaderAppsViewHolder) appsViewHolder;
            headerHolder.appName.setText(header.getAppName());
            headerHolder.count.setText(headerHolder.count.getContext().getString(R.string.number, header.getCount()));
            headerHolder.appIcon.setImageDrawable(header.getIcon());
        } else if (base instanceof RestoreAppsItemsFooter) {
            RestoreAppsItemsFooter detail = (RestoreAppsItemsFooter) base;
            DetailAppsViewHolder holder = (DetailAppsViewHolder) appsViewHolder;
            holder.version.setText(holder.version.getContext().getString(R.string.list_app_version, ": ", detail.getVersion()));
            holder.fullpath = detail.getFullpath();
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        switch (i) {
            case BASE_HEADER:
                View headerView = LayoutInflater.
                        from(viewGroup.getContext()).
                        inflate(R.layout.recyclerview_applist_apps_header, viewGroup, false);
                return new HeaderAppsViewHolder(headerView);
            case BASE_DETAIL:
                View detailsView = LayoutInflater.
                        from(viewGroup.getContext()).
                        inflate(R.layout.recyclerview_applist_apps_footer, viewGroup, false);
                return new DetailAppsViewHolder(detailsView);
        }

        return null;
    }

    @Override
    public int getItemViewType(int position){
        if (position == appsList.size()){
            return BASE_HEADER;
        }
        RestoreAppsItemsBase item = appsList.get(position);
        if (item instanceof RestoreAppsItemsHeader) return BASE_HEADER;
        if (item instanceof RestoreAppsItemsFooter) return BASE_DETAIL;
        return BASE_HEADER;
    }

    private int getHeaderIndex(RestoreAppsItemsHeader header){
        int index = -1;
        for (int i = 0; i < appsList.size(); i++){
            if (!(appsList.get(i) instanceof RestoreAppsItemsHeader)) continue;
            if (appsList.get(i) == header) index = i;
        }
        return index;
    }

    private void expand(RestoreAppsItemsHeader header){
        if (!header.hasChild()) return;
        List<RestoreAppsItemsFooter> child = header.getChild();

        int add = getHeaderIndex(header);
        if (add == -1) return;

        appsList.get(add).setExpanded(true);

        ListIterator iterator = child.listIterator(child.size());
        while (iterator.hasPrevious()){
            RestoreAppsItemsFooter statistics = (RestoreAppsItemsFooter) iterator.previous();
            appsList.add(add+1, statistics);
            notifyItemInserted(add + 1);
        }
    }

    private void retract(RestoreAppsItemsHeader header){
        if (!header.hasChild()) return;
        List<RestoreAppsItemsFooter> child = header.getChild();

        int remove = getHeaderIndex(header);
        if (remove == -1) return;

        appsList.get(remove).setExpanded(false);

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


    private class HeaderAppsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView appName, count;
        ImageView appIcon;

        HeaderAppsViewHolder(View v)
        {
            super(v);
            appName = v.findViewById(R.id.tvAppName);
            count = v.findViewById(R.id.tv_app_restore_count);
            appIcon = v.findViewById(R.id.iv_icon);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = this.getLayoutPosition();
            RestoreAppsItemsHeader item = (RestoreAppsItemsHeader) appsList.get(position);

            LogHelper.d("RestoreAppsAdapter", "App Name: " + appName.getText() + " isExpanded: " + item.isExpanded());

            if (item.isExpanded()){
                retract(item);
            } else {
                if (item.hasChild()){
                    expand(item);
                }
            }
        }
    }

    private class DetailAppsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView version;
        String fullpath;

        DetailAppsViewHolder(View v)
        {
            super(v);
            version = v.findViewById(R.id.tvVersion);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
            intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
            LogHelper.d("DEBUG", "Retrieving from " + fullpath);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                LogHelper.i("RestoreApps", "Post-Nougat: Using new Content URI method");
                LogHelper.i("Downloader", "Invoking Content Provider " + v.getContext().getPackageName() + ".provider");
                Uri contentUri = FileProvider.getUriForFile(v.getContext(), v.getContext().getPackageName() + ".provider", new File(fullpath));
                intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                LogHelper.i("RestoreApps", "Pre-Nougat: Fallbacking to old method as they dont support contenturis");
                intent.setDataAndType(Uri.fromFile(new File(fullpath)), "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            v.getContext().startActivity(intent);
        }
    }
}
