package com.itachi1706.cheesecakeutilities.modules.listapplications;

import static android.graphics.Color.rgb;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.collection.ArrayMap;

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
import com.google.android.material.snackbar.Snackbar;
import com.itachi1706.helperlib.helpers.PrefHelper;
import com.itachi1706.cheesecakeutilities.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ListApplicationsApiGraphActivity extends AppCompatActivity implements OnChartValueSelectedListener {

    private RelativeLayout layout;

    private static final int[] MATERIAL_COLORS = {rgb(244, 67, 54), rgb(233, 30, 99), rgb(156, 39, 176), rgb(103, 58, 183), rgb(63, 81, 181), rgb(33, 150, 243), rgb(3, 169, 244), rgb(0, 188, 212), rgb(0, 150, 136), rgb(76, 175, 80), rgb(139, 195, 74), rgb(205, 220, 57), rgb(255, 235, 59), rgb(255, 193, 7), rgb(255, 152, 0), rgb(255, 87, 34), rgb(121, 85, 72), rgb(158, 158, 158), rgb(96, 125, 139)};

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
        dataSet.setColors(MATERIAL_COLORS);
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
        legend.setTextColor(PrefHelper.isNightModeEnabled(this) ? Color.WHITE : Color.BLACK);
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
