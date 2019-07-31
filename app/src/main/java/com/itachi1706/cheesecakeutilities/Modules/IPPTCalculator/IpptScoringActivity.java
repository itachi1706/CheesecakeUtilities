package com.itachi1706.cheesecakeutilities.Modules.IPPTCalculator;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.itachi1706.cheesecakeutilities.Modules.IPPTCalculator.Helpers.JsonHelper;
import com.itachi1706.cheesecakeutilities.R;
import com.itachi1706.cheesecakeutilities.RecyclerAdapters.StringRecyclerAdapter;
import com.itachi1706.cheesecakeutilities.Util.LogHelper;

import java.util.Arrays;

public class IpptScoringActivity extends AppCompatActivity {

    private Spinner genderSpinner, ageSpinner, exerciseSpinner;
    private RecyclerView recyclerView;
    private ProgressBar bar;
    private TextView label;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ippt_scoring);

        genderSpinner = findViewById(R.id.spinnerGender);
        ageSpinner = findViewById(R.id.spinnerAge);
        exerciseSpinner = findViewById(R.id.spinnerExercise);

        recyclerView = findViewById(R.id.ippt_recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        bar = findViewById(R.id.ippt_pb);
        label = findViewById(R.id.ippt_pb_label);

        ageSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, JsonHelper.getAgeRangeText(this)));
        genderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String g = genderSpinner.getSelectedItem().toString();
                LogHelper.d("IPPT UPDATE", g);
                int gender = JsonHelper.getGender(g);
                if (gender == JsonHelper.FEMALE)
                    exerciseSpinner.setAdapter(new ArrayAdapter<>(getBaseContext(), android.R.layout.simple_spinner_dropdown_item,
                            Arrays.asList(getResources().getStringArray(R.array.ippt_exercisefemale))));
                else
                    exerciseSpinner.setAdapter(new ArrayAdapter<>(getBaseContext(), android.R.layout.simple_spinner_dropdown_item,
                            Arrays.asList(getResources().getStringArray(R.array.ippt_exercisemale))));
                updateRecyclerView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        ageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateRecyclerView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        exerciseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateRecyclerView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void updateRecyclerView() {
        int gender = JsonHelper.getGender(genderSpinner.getSelectedItem().toString());
        int ageGroup = JsonHelper.getAgeGroup(ageSpinner.getSelectedItem().toString(), this);
        int exercise = JsonHelper.getExercise(exerciseSpinner.getSelectedItem().toString());
        LogHelper.i("IpptScore", "Gender: " + gender);
        LogHelper.i("IpptScore", "Age Group: " + ageGroup);
        LogHelper.i("IpptScore", "Exercise: " + exercise);
        StringRecyclerAdapter adapter = new StringRecyclerAdapter(new String[0]);
        recyclerView.setAdapter(adapter);
        bar.setVisibility(View.VISIBLE);
        label.setVisibility(View.VISIBLE);
        new ScoringUpdateTask(this, results -> {
            StringRecyclerAdapter adapter1 = new StringRecyclerAdapter(results);
            recyclerView.setAdapter(adapter1);
            bar.setVisibility(View.GONE);
            label.setVisibility(View.GONE);
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ageGroup, gender, exercise);
    }
}
