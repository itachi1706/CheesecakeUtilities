package com.itachi1706.cheesecakeutilities.Modules.IPPTCalculator;

import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.itachi1706.cheesecakeutilities.Modules.IPPTCalculator.Helpers.JsonHelper;
import com.itachi1706.cheesecakeutilities.R;

public class IpptCalculatorActivity extends AppCompatActivity {

    private Spinner genderSpinner, ageSpinner;
    private EditText runMin, runSec, pushup, situp;
    private TextInputLayout pushupLayout;
    private Button calculate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ippt_calculator);

        genderSpinner = (Spinner) findViewById(R.id.spinnerGender);
        ageSpinner = (Spinner) findViewById(R.id.spinnerAge);
        runMin = (EditText) findViewById(R.id.etRun);
        runSec = (EditText) findViewById(R.id.etRunSec);
        pushup = (EditText) findViewById(R.id.etPushUps);
        situp = (EditText) findViewById(R.id.etSitUps);
        calculate = (Button) findViewById(R.id.btnCalculate);
        pushupLayout = (TextInputLayout) findViewById(R.id.til_etPushUps);
        genderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String g = genderSpinner.getSelectedItem().toString();
                Log.d("IPPT UPDATE", g);
                int gender = JsonHelper.getGender(g);
                if (gender == JsonHelper.FEMALE) pushupLayout.setHint("Bent Knee Push-Ups");
                else pushupLayout.setHint("Push-Ups");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        ageSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, JsonHelper.getAgeRangeText(this)));
        calculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculate();
            }
        });
    }

    private void calculate() {
        if (!validate()) {
            Toast.makeText(this, "Unable to calculate. Please make sure all fields are filled", Toast.LENGTH_LONG).show();
            return;
        }

        int rm = Integer.parseInt(runMin.getText().toString());
        int rs = Integer.parseInt(runSec.getText().toString());
        int su = Integer.parseInt(situp.getText().toString());
        int pu = Integer.parseInt(pushup.getText().toString());
        int ageGroup = JsonHelper.getAgeGroup(ageSpinner.getSelectedItem().toString(), this);
        int gender = JsonHelper.getGender(genderSpinner.getSelectedItem().toString());
        int score = JsonHelper.calculateScore(pu, su, rm, rs, ageGroup, gender, this);
        new AlertDialog.Builder(this).setMessage("Score: " + score + "\nResults: " + JsonHelper.getScoreResults(score)).show();

    }

    private boolean validate() {
        return !runSec.getText().toString().isEmpty() && !runMin.getText().toString().isEmpty()
                && !situp.getText().toString().isEmpty() && !pushup.getText().toString().isEmpty();
    }


    // TODO: Move to the score table activity
        /*genderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String g = genderSpinner.getSelectedItem().toString();
                Log.d("IPPT UPDATE", g);
                int gender = JsonHelper.getGender(g);
                if (gender == JsonHelper.FEMALE)
                    exerciseSpinner.setAdapter(new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item,
                            Arrays.asList(getResources().getStringArray(R.array.exercisefemale))));
                else
                    exerciseSpinner.setAdapter(new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item,
                            Arrays.asList(getResources().getStringArray(R.array.exercisemale))));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });*/
}
