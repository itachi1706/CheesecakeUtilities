package com.itachi1706.cheesecakeutilities.Modules.SGPsi;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.itachi1706.cheesecakeutilities.GeneralSettingsActivity;
import com.itachi1706.cheesecakeutilities.R;

import java.lang.ref.WeakReference;
import java.util.Locale;

public class PsiActivity extends AppCompatActivity {
    
    private TextView psiRange, psiNorth, psiSouth, psiEast, psiWest, psiCentral;
    private TextView pmRange, pmNorth, pmSouth, pmEast, pmWest, pmCentral;
    private TextView lastUpdate;
    private SwipeRefreshLayout refreshLayout;

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
        psiRange.setText(data.getPsirange());
        psiNorth.setText(String.format(Locale.getDefault(), "%d", data.getNorth()));
        psiSouth.setText(String.format(Locale.getDefault(), "%d", data.getSouth()));
        psiEast.setText(String.format(Locale.getDefault(), "%d", data.getEast()));
        psiWest.setText(String.format(Locale.getDefault(), "%d", data.getWest()));
        psiCentral.setText(String.format(Locale.getDefault(), "%d", data.getCentral()));
        pmRange.setText(data.getParticlerange());
        pmNorth.setText(String.format(Locale.getDefault(), "%d", data.getParticlenorth()));
        pmSouth.setText(String.format(Locale.getDefault(), "%d", data.getParticlesouth()));
        pmEast.setText(String.format(Locale.getDefault(), "%d", data.getParticleeast()));
        pmWest.setText(String.format(Locale.getDefault(), "%d", data.getParticlewest()));
        pmCentral.setText(String.format(Locale.getDefault(), "%d", data.getParticlecentral()));
        lastUpdate.setText(data.getTime());
        refreshLayout.setRefreshing(false);
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
