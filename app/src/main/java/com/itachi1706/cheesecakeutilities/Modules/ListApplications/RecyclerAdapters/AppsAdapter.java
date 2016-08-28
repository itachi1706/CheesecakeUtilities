package com.itachi1706.cheesecakeutilities.Modules.ListApplications.RecyclerAdapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.itachi1706.cheesecakeutilities.Modules.ListApplications.Broadcasts.ListAppBroadcast;
import com.itachi1706.cheesecakeutilities.Modules.ListApplications.Objects.AppsItem;
import com.itachi1706.cheesecakeutilities.R;

import java.util.Arrays;
import java.util.List;

/**
 * Created by itachi1706 on 2/20/2016.
 * For com.itachi1706.cheesecakeutilities.Modules.ListApplications.RecyclerAdapters in Cheesecake Utilities.
 */
public class AppsAdapter extends RecyclerView.Adapter<AppsAdapter.AppsViewHolder> {
    private List<AppsItem> appsList;

    public AppsAdapter(List<AppsItem> strings)
    {
        this.appsList = strings;
    }

    public AppsAdapter(AppsItem[] strings)
    {
        this.appsList = Arrays.asList(strings);
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
        appsViewHolder.appApiVersion.setText(s.getApiVersion());
        appsViewHolder.appPackageName.setText(s.getPackageName());
        appsViewHolder.appIcon.setImageDrawable(s.getIcon());
        appsViewHolder.appLocation = s.getAppPath();
        appsViewHolder.version = s.getVersion();
        appsViewHolder.appVersion.setText("Version: " + s.getVersion());
    }

    @Override
    public AppsViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.recyclerview_applist_apps, viewGroup, false);

        return new AppsViewHolder(itemView);
    }


    public class AppsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        protected TextView appName, appPackageName, appApiVersion, appVersion;
        protected ImageView appIcon;
        protected String appLocation, version;

        public AppsViewHolder(View v)
        {
            super(v);
            appName = (TextView) v.findViewById(R.id.tvAppName);
            appPackageName = (TextView) v.findViewById(R.id.tvPackageName);
            appApiVersion = (TextView) v.findViewById(R.id.tvAPI);
            appIcon = (ImageView) v.findViewById(R.id.iv_icon);
            appVersion = (TextView) v.findViewById(R.id.tvVersion);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            final View view = v;
            new AlertDialog.Builder(v.getContext()).setTitle(appName.getText().toString())
                    .setMessage("Package Name: " + appPackageName.getText().toString() +
                            "\n\nApp Version: " + version +
                            "\n\nAPI Version: " + appApiVersion.getText().toString() +
                            "\n\nApp Location: " + appLocation)
                    .setIcon(appIcon.getDrawable())
                    .setNeutralButton("App Settings", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", appPackageName.getText().toString(), null);
                            intent.setData(uri);
                            Log.v("AppsAdapter", "Attempting to launch for " + appName.getText());
                            view.getContext().startActivity(intent);
                        }
                    }).setPositiveButton("Backup", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Backup
                    Log.i("Backup", "Requesting backup of " + appPackageName.getText().toString());
                    sendCompleteIntent(view.getContext());
                }
            }).show();
        }

        private void sendCompleteIntent(Context context) {
            // Send a local broadcast to close any existing dialogs
            Intent completeIntent = new Intent(ListAppBroadcast.LISTAPP_BROADCAST_BACKUP);
            completeIntent.putExtra(ListAppBroadcast.LISTAPP_BROADCAST_APPNAME, appName.getText().toString());
            completeIntent.putExtra(ListAppBroadcast.LISTAPP_BROADCAST_APPPACKAGE, appPackageName.getText().toString());
            completeIntent.putExtra(ListAppBroadcast.LISTAPP_BROADCAST_APPPATH, appLocation);
            completeIntent.putExtra(ListAppBroadcast.LISTAPP_BROADCAST_APPVERSION, version);
            LocalBroadcastManager.getInstance(context).sendBroadcast(completeIntent);
        }

    }
}
