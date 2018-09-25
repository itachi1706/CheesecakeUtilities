package com.itachi1706.cheesecakeutilities.Modules.ListApplications;

import android.graphics.Color;
import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import androidx.collection.ArrayMap;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.itachi1706.cheesecakeutilities.R;
import com.itachi1706.cheesecakeutilities.Util.CommonVariables;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ListApplicationsApiGraphActivity extends AppCompatActivity implements OnChartValueSelectedListener {

    private RelativeLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_applications_api_graph);
        if (getSupportActionBar() != null && getSupportActionBar().isShowing()) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (!getIntent().hasExtra("appCount")) {
            Toast.makeText(this, "Cannot plot graph. Please wait for the app list to finish loading first!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        String appCount = getIntent().getStringExtra("appCount");
        ArrayMap<Integer, Integer> appCountMap = new ArrayMap<>();
        // Parse shit
        String[] tmp1 = appCount.split("-");
        for (String t : tmp1) {
            String[] tmp2 = t.split(":");
            if (tmp2.length != 2) {
                // Corrupt, ignore
                continue;
            }

            appCountMap.put(Integer.parseInt(tmp2[0]), Integer.parseInt(tmp2[1]));
        }

        PieChart chart = findViewById(R.id.chart);
        layout = findViewById(R.id.activity_list_applications_api_graph);
        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<Integer, Integer> object : appCountMap.entrySet()) {
            entries.add(new PieEntry((float) object.getValue(), "API " + object.getKey().toString()));
        }
        PieDataSet dataSet = new PieDataSet(entries, "API Version");
        dataSet.setColors(CommonVariables.MATERIAL_COLORS);
        chart.setNoDataText("No Applications on Device");
        chart.setHoleRadius(20f);
        Description desc = new Description();
        desc.setText("");
        chart.setDescription(desc);
        chart.setUsePercentValues(true);
        dataSet.setValueTextSize(15f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueFormatter(new PercentFormatter());
        chart.setTransparentCircleRadius(20f);
        Legend legend = chart.getLegend();
        legend.setWordWrapEnabled(true);
        legend.setTextSize(15f);
        PieData data = new PieData(dataSet);
        chart.setData(data);
        chart.setOnChartValueSelectedListener(this);
        chart.invalidate();
    }

    Snackbar snackbar = null;

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        if (!(e instanceof PieEntry)) return;
        if (snackbar != null && snackbar.isShownOrQueued()) {
            snackbar.dismiss();
            snackbar = null;
        }

        PieEntry pieEntry = (PieEntry) e;
        String api = pieEntry.getLabel();
        int count = (int) pieEntry.getValue();

        snackbar = Snackbar.make(layout, api + "\nApp Count: " + count, Snackbar.LENGTH_INDEFINITE);
        snackbar.show();
    }

    @Override
    public void onNothingSelected() {
        if (snackbar != null && snackbar.isShownOrQueued())
            snackbar.dismiss();

        snackbar = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
