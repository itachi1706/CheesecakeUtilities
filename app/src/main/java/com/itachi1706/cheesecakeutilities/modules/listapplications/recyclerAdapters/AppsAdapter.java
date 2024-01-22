package com.itachi1706.cheesecakeutilities.modules.listapplications.recyclerAdapters;

import android.content.Intent;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.itachi1706.cheesecakeutilities.modules.listapplications.ListApplicationsDetailActivity;
import com.itachi1706.cheesecakeutilities.modules.listapplications.objects.AppsItem;
import com.itachi1706.cheesecakeutilities.R;
import com.turingtechnologies.materialscrollbar.ICustomAdapter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by itachi1706 on 2/20/2016.
 * For com.itachi1706.cheesecakeutilities.modules.ListApplications.RecyclerAdapters in Cheesecake Utilities.
 */
public class AppsAdapter extends RecyclerView.Adapter<AppsAdapter.AppsViewHolder> implements ICustomAdapter {
    private List<AppsItem> appsList;
    public static final String TAG = "AppsAdapter";

    private static final int SORT_NAME = 0;
    public static final int SORT_API = 1;

    private boolean sortByName = false;

    public AppsAdapter(List<AppsItem> strings)
    {
        this.appsList = strings;
    }

    public AppsAdapter(AppsItem[] strings)
    {
        this.appsList = Arrays.asList(strings);
    }

    public void sort() {
        sort(SORT_NAME);
    }

    public void sort(int type) {
        switch (type) {
            case SORT_API: sortByApiVersion(); sortByName = false; break;
            case SORT_NAME:
            default: sortByName(); sortByName = true; break;
        }
    }

    private void sortByName() {
        Collections.sort(appsList, (o1, o2) -> o1.getAppName().compareTo(o2.getAppName()));
    }

    private void sortByApiVersion() {
        Collections.sort(appsList, (o1, o2) -> compare(o2.getApiVersion(), o1.getApiVersion()));
    }

    private static int compare(int x, int y) {
        return (x < y) ? -1 : ((x == y) ? 0 : 1);
    }

    @Override
    public int getItemCount()
    {
        return appsList.size();
    }

    @Override
    public void onBindViewHolder(AppsViewHolder appsViewHolder, int i)
    {
        AppsItem s = appsList.get(i);
        appsViewHolder.appName.setText(s.getAppName());
        appsViewHolder.appName.setSelected(true);
        appsViewHolder.appApiVersion.setText(appsViewHolder.appApiVersion.getContext().getString(R.string.number, s.getApiVersion()));
        appsViewHolder.appPackageName.setText(s.getPackageName());
        appsViewHolder.appIcon.setImageDrawable(s.getIcon());
        appsViewHolder.appVersion.setText(appsViewHolder.appVersion.getContext().getString(R.string.list_app_version, ": ", s.getVersion()));
    }

    @Override
    public AppsViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.recyclerview_applist_apps, viewGroup, false);

        return new AppsViewHolder(itemView);
    }

    @Override
    public String getCustomStringForElement(int element) {
        if (sortByName)
            return appsList.get(element).getAppName().charAt(0) + "";
        else
            return appsList.get(element).getApiVersion() + "";
    }


    class AppsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView appName, appPackageName, appApiVersion, appVersion;
        ImageView appIcon;

        AppsViewHolder(View v)
        {
            super(v);
            appName = v.findViewById(R.id.tvAppName);
            appName.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            appName.setMarqueeRepeatLimit(-1);
            appName.setHorizontallyScrolling(true);
            appPackageName = v.findViewById(R.id.tvPackageName);
            appApiVersion = v.findViewById(R.id.tvAPI);
            appIcon = v.findViewById(R.id.iv_icon);
            appVersion = v.findViewById(R.id.tvVersion);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Intent intent1 = new Intent(v.getContext(), ListApplicationsDetailActivity.class);
            intent1.putExtra("packageName", appPackageName.getText().toString());
            v.getContext().startActivity(intent1);
        }
    }
}
