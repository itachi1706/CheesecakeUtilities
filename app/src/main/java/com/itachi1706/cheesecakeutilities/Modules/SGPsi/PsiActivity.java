package com.itachi1706.cheesecakeutilities.Modules.SGPsi;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.itachi1706.cheesecakeutilities.BaseActivity;
import com.itachi1706.cheesecakeutilities.R;
import com.itachi1706.cheesecakeutilities.Util.ColorUtils;

import java.lang.ref.WeakReference;
import java.util.Locale;

public class PsiActivity extends BaseActivity {
    
    private TextView psiRange, psiNorth, psiSouth, psiEast, psiWest, psiCentral;
    private TextView pmRange, pmNorth, pmSouth, pmEast, pmWest, pmCentral;
    private TextView lastUpdate;
    private SwipeRefreshLayout refreshLayout;

    @Override
    public String getHelpDescription() {
        return "Gives you information about the current PM2.5 and PSI data of Singapore. " +
                "Also provides a graph to show the history of all of the said data\n\n" +
                "Other Weather Data coming soon\n" +
                "\nData Credits: National Environment Agency, Singapore";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_psi);

        psiRange = findViewById(R.id.psi_psi_range);
        psiNorth = findViewById(R.id.psi_psi_north);
        psiSouth = findViewById(R.id.psi_psi_south);
        psiEast = findViewById(R.id.psi_psi_east);
        psiWest = findViewById(R.id.psi_psi_west);
        psiCentral = findViewById(R.id.psi_psi_cen);
        pmRange = findViewById(R.id.psi_pm_range);
        pmNorth = findViewById(R.id.psi_pm_north);
        pmSouth = findViewById(R.id.psi_pm_south);
        pmEast = findViewById(R.id.psi_pm_east);
        pmWest = findViewById(R.id.psi_pm_west);
        pmCentral = findViewById(R.id.psi_pm_cen);
        lastUpdate = findViewById(R.id.psi_last_update);
        refreshLayout = findViewById(R.id.pull_to_refresh);

        refreshLayout.setOnRefreshListener(this::retrieveData);
        refreshLayout.setColorSchemeResources(
                R.color.refresh_progress_1,
                R.color.refresh_progress_2);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Retrieve data
        retrieveData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.modules_psi, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                Toast.makeText(this, "Refreshing", Toast.LENGTH_SHORT).show();
                retrieveData();
                return true;
            case R.id.view_graph:
                startActivity(new Intent(this, PsiGraphActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void retrieveData() {
        new RetrievePsiData(new PsiDataHandler(this)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        refreshLayout.setRefreshing(true);
    }

    private void processData(PsiGeneral data) {
        tmp = data; // For updateField() method
        psiRange.setText(data.getPsirange());
        updateField(psiNorth, data.getNorth());
        updateField(psiSouth, data.getSouth());
        updateField(psiEast, data.getEast());
        updateField(psiWest, data.getWest());
        updateField(psiCentral, data.getCentral());
        pmRange.setText(data.getParticlerange());
        updateField(pmNorth, data.getParticlenorth());
        updateField(pmSouth, data.getParticlesouth());
        updateField(pmEast, data.getParticleeast());
        updateField(pmWest, data.getParticlewest());
        updateField(pmCentral, data.getParticlecentral());
        tmp = null; // Clear away the data to prevent memory leak
        lastUpdate.setText(data.getTime());
        refreshLayout.setRefreshing(false);
    }

    private PsiGeneral tmp;

    private void updateField(TextView view, int data) {
        view.setText(String.format(Locale.getDefault(), "%d", data));
        view.setTextColor(ColorUtils.Companion.getColorFromVariable(this, tmp.getColor(data)));
    }

    static class PsiDataHandler extends Handler {
        WeakReference<PsiActivity> mActivity;

        PsiDataHandler(PsiActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            PsiActivity activity = mActivity.get();
            super.handleMessage(msg);

            switch (msg.what) {
                case RetrievePsiData.DATA_RESULT:
                    Bundle data = msg.getData();
                    String psiData = data.getString("data", "NULL");
                    if (psiData.equalsIgnoreCase("null")) {
                        Toast.makeText(activity.getApplicationContext(), "An error occurred retrieving data", Toast.LENGTH_LONG).show();
                        return;
                    }
                    Log.d("PsiActivity", "JSON Data: " + psiData);

                    Gson gson = new Gson();
                    PsiGeneral psi = gson.fromJson(psiData, PsiGeneral.class);
                    activity.processData(psi);
                    break;
                default: super.handleMessage(msg); break;
            }
        }
    }
}
