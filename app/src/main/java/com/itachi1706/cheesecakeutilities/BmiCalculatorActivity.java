package com.itachi1706.cheesecakeutilities;

import android.content.Context;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.itachi1706.cheesecakeutilities.util.CommonMethods;

public class BmiCalculatorActivity extends BaseModuleActivity {

    EditText height, weight;
    Button table, calculate;
    TextView result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bmi_calculator);

        height = findViewById(R.id.etHeight);
        weight = findViewById(R.id.etWeight);
        table = findViewById(R.id.btnTable);
        calculate = findViewById(R.id.btnCalculate);
        result = findViewById(R.id.tvResults);
        CommonMethods.disableAutofill(getWindow().getDecorView());
        calculate.setOnClickListener(v -> {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            calculate();
        });
        table.setOnClickListener(v -> showTable());
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
            //noinspection ResultOfMethodCallIgnored
            Integer.parseInt(height.getText().toString());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid Height", Toast.LENGTH_LONG).show();
            return false;
        }

        try {
            //noinspection ResultOfMethodCallIgnored
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
