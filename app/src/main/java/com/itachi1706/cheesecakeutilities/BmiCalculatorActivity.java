package com.itachi1706.cheesecakeutilities;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class BmiCalculatorActivity extends BaseActivity {

    EditText height, weight;
    Button table, calculate;
    TextView result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bmi_calculator);

        height = (EditText) findViewById(R.id.etHeight);
        weight = (EditText) findViewById(R.id.etWeight);
        table = (Button) findViewById(R.id.btnTable);
        calculate = (Button) findViewById(R.id.btnCalculate);
        result = (TextView) findViewById(R.id.tvResults);

        calculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                calculate();
            }
        });
        table.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTable();
            }
        });
    }

    private void calculate() {
        if (!validate()) return;
        double heightVal = Double.parseDouble(height.getText().toString()) / 100.0;
        double weightVal = Double.parseDouble(weight.getText().toString());
        double bmi = Math.round((weightVal / Math.pow(heightVal, 2)) * 100.0) / 100.0;

        // Check range
        String range;
        if (bmi < SU) range = "Severely Underweight";
        else if (bmi < MU) range = "Moderately Underweight";
        else if (bmi < U) range = "Underweight";
        else if (bmi < N) range = "Normal";
        else if (bmi < OW) range = "Overweight";
        else if (bmi < CIO) range = "Class I Obese";
        else if (bmi < CIIO) range = "Class II Obese";
        else range = "Class III Obese";

        result.setText("BMI Value: " + bmi + "\nStatus: " + range);
    }

    private static final double SU = 16, MU = 17, U = 18.5, N = 25, OW = 30, CIO = 35, CIIO = 40;

    private void showTable() {
        new AlertDialog.Builder(this).setTitle("BMI Ranges")
                .setMessage("Severely Underweight = <16\n" +
                        "Moderately Underweight = 16 - 17\n" +
                        "Underweight = 17 - 18.5\n" +
                        "Normal = 18.5 - 25\n" +
                        "Overweight = 25 - 30\n" +
                        "Class I Obese = 30 - 35\n" +
                        "Class II Obese = 35 - 40\n" +
                        "Class III Obese = > 40").setPositiveButton(R.string.dialog_action_positive_close, null).show();
    }

    private boolean validate() {
        if (height.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please fill in height value", Toast.LENGTH_LONG).show();
            return false;
        }
        if (weight.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please fill in weight value", Toast.LENGTH_LONG).show();
            return false;
        }
        try {
            Integer.parseInt(height.getText().toString());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid Height", Toast.LENGTH_LONG).show();
            return false;
        }

        try {
            Double.parseDouble(weight.getText().toString());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid Weight", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    @Override
    public String getHelpDescription() {
        return "A BMI Calculator that gives you your BMI value after taking in your height and weight";
    }
}
