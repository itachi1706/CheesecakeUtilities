package com.itachi1706.cheesecakeutilities.Modules.ListApplications;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.itachi1706.cheesecakeutilities.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.github.mikephil.charting.utils.ColorTemplate.rgb;

public class ListApplicationsApiGraphActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_applications_api_graph);

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

        PieChart chart = (PieChart) findViewById(R.id.chart);
        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<Integer, Integer> object : appCountMap.entrySet()) {
            entries.add(new PieEntry((float) object.getValue(), "API " + object.getKey().toString()));
        }
        PieDataSet dataSet = new PieDataSet(entries, "API Version");
        dataSet.setColors(colors, this);
        //dataSet.setColors(android.R.color.holo_red_dark, android.R.color.holo_blue_bright);
        //dataSet.setValueTextColor(Color.BLACK);
        PieData data = new PieData(dataSet);
        chart.setData(data);
        chart.invalidate();
    }

    int[] colors = {rgb("#F44336")};
}
