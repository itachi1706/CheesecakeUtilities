package com.itachi1706.cheesecakeutilities.modules.IPPTCalculator;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.textfield.TextInputLayout;
import com.itachi1706.cheesecakeutilities.BaseModuleActivity;
import com.itachi1706.cheesecakeutilities.modules.IPPTCalculator.Helpers.JsonHelper;
import com.itachi1706.cheesecakeutilities.modules.IPPTCalculator.JsonObjects.Gender;
import com.itachi1706.cheesecakeutilities.modules.IPPTCalculator.JsonObjects.Main;
import com.itachi1706.cheesecakeutilities.R;
import com.itachi1706.cheesecakeutilities.util.CommonMethods;
import com.itachi1706.cheesecakeutilities.util.LogHelper;

import static com.itachi1706.cheesecakeutilities.modules.IPPTCalculator.Helpers.JsonHelper.FEMALE;
import static com.itachi1706.cheesecakeutilities.modules.IPPTCalculator.Helpers.JsonHelper.PUSHUP;
import static com.itachi1706.cheesecakeutilities.modules.IPPTCalculator.Helpers.JsonHelper.RUN;
import static com.itachi1706.cheesecakeutilities.modules.IPPTCalculator.Helpers.JsonHelper.SITUP;
import static com.itachi1706.cheesecakeutilities.modules.IPPTCalculator.Helpers.JsonHelper.UNKNOWN;

public class IpptCalculatorActivity extends BaseModuleActivity {

    private Spinner genderSpinner, ageSpinner;
    private EditText runMin, runSec, pushup, situp;
    private TextView results;
    private TextInputLayout pushupLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ippt_calculator);

        genderSpinner = findViewById(R.id.spinnerGender);
        ageSpinner = findViewById(R.id.spinnerAge);
        runMin = findViewById(R.id.etRun);
        runSec = findViewById(R.id.etRunSec);
        pushup = findViewById(R.id.etPushUps);
        situp = findViewById(R.id.etSitUps);
        Button calculate = findViewById(R.id.btnCalculate);
        Button scores = findViewById(R.id.btnViewScoring);
        results = findViewById(R.id.tvResults);
        pushupLayout = findViewById(R.id.til_etPushUps);
        CommonMethods.disableAutofill(getWindow().getDecorView());
        genderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String g = genderSpinner.getSelectedItem().toString();
                LogHelper.d("IPPT UPDATE", g);
                int gender = JsonHelper.getGender(g);
                if (gender == FEMALE) pushupLayout.setHint("Bent Knee Push-Ups");
                else pushupLayout.setHint("Push-Ups");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        ageSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, JsonHelper.getAgeRangeText(this)));
        calculate.setOnClickListener(v -> calculate());
        scores.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), IpptScoringActivity.class)));
    }

    private void calculate() {
        int rm = (runMin.getText().toString().isEmpty()) ? -1 : Integer.parseInt(runMin.getText().toString());
        int rs = (runSec.getText().toString().isEmpty()) ? 0 : Integer.parseInt(runSec.getText().toString());
        int su = (situp.getText().toString().isEmpty()) ? -1 : Integer.parseInt(situp.getText().toString());
        int pu = (pushup.getText().toString().isEmpty()) ? -1 : Integer.parseInt(pushup.getText().toString());

        int ageGroup = JsonHelper.getAgeGroup(ageSpinner.getSelectedItem().toString(), this);
        int gender = JsonHelper.getGender(genderSpinner.getSelectedItem().toString());
        int score;
        Main object = JsonHelper.readFromJsonRaw(this);
        Gender exercisesScore = (gender == FEMALE) ? object.getDataFemale() : object.getDataMale();

        StringBuilder message = new StringBuilder();
        if (!incomplete(rm, rs, su, pu)) {
            score = JsonHelper.calculateScore(pu, su, rm, rs, ageGroup, gender, this);
            message.append("Score: ").append(score).append("\nResults: ").append(JsonHelper.getScoreResults(score));
            results.setText(message.toString());


            message.append("\n\nDetails:\nPush-Ups: ").append(pu).append(" (")
                    .append(JsonHelper.getPushUpScore(pu, ageGroup, exercisesScore)).append(" pts)");
            message.append("\nSit Ups: ").append(su).append(" (").append(JsonHelper.getSitUpScore(su, ageGroup, exercisesScore)).append(" pts)");
            message.append("\n2.4 Run: ").append(rm).append(":").append(rs).append(" (")
                    .append(JsonHelper.getRunScore(rm, rs, ageGroup, exercisesScore)).append(" pts)");
        } else {
            score = JsonHelper.calculateIncompleteScore(pu, su, rm, rs, ageGroup, gender, this);
            message.append("Score: ").append(score).append("\nResults: Incomplete Score");
            results.setText(message.toString());

            int countOfErrors = 0;
            int error = UNKNOWN;
            if (su == -1) {
                countOfErrors++;
                error = SITUP;
            }
            if (pu == -1) {
                countOfErrors++;
                error = PUSHUP;
            }
            if (rm == -1) {
                countOfErrors++;
                error = RUN;
            }

            if (countOfErrors == 1) {
                // Calculate how many more reps to go
                int ptsmoreactive = 61 - score;
                switch (error) {
                    case SITUP:
                        message.append("\n\nDetails:\nPush-Ups: ").append(pu).append(" (")
                                .append(JsonHelper.getPushUpScore(pu, ageGroup, exercisesScore)).append(" pts)");
                        message.append("\n2.4 Run: ").append(rm).append(":").append(rs).append(" (")
                                .append(JsonHelper.getRunScore(rm, rs, ageGroup, exercisesScore)).append(" pts)");

                        message.append("\n\nYou need ").append(JsonHelper.countSitupMore(ptsmoreactive, ageGroup, exercisesScore))
                                .append(" (Active)/").append(JsonHelper.countSitupMore(ptsmoreactive - 10, ageGroup, exercisesScore))
                                .append(" (NSMen) sit ups to get a pass");
                        break;
                    case PUSHUP:
                        message.append("\n\nDetails:\nSit Ups: ").append(su).append(" (")
                                .append(JsonHelper.getSitUpScore(su, ageGroup, exercisesScore)).append(" pts)");
                        message.append("\n2.4 Run: ").append(rm).append(":").append(rs).append(" (")
                                .append(JsonHelper.getRunScore(rm, rs, ageGroup, exercisesScore)).append(" pts)");

                        message.append("\n\nYou need ").append(JsonHelper.countPushupMore(ptsmoreactive, ageGroup, exercisesScore))
                                .append(" (Active)/").append(JsonHelper.countPushupMore(ptsmoreactive - 10, ageGroup, exercisesScore))
                                .append(" (NSMen) push ups to get a pass");
                        break;
                    case RUN:
                        message.append("\n\nDetails:\nPush-Ups: ").append(pu).append(" (")
                                .append(JsonHelper.getPushUpScore(pu, ageGroup, exercisesScore)).append(" pts)");
                        message.append("\nSit Ups: ").append(su).append(" (").append(JsonHelper.getSitUpScore(su, ageGroup, exercisesScore)).append(" pts)");

                        message.append("\n\nYou need to get ").append(JsonHelper.countRunMore(ptsmoreactive, ageGroup, exercisesScore))
                                .append(" (Active)/").append(JsonHelper.countRunMore(ptsmoreactive - 10, ageGroup, exercisesScore))
                                .append(" (NSMen) for running to get a pass");
                }
            }
        }
        new AlertDialog.Builder(this).setTitle("IPPT Score")
                .setMessage(message.toString())
                .setPositiveButton(R.string.dialog_action_positive_close, null).show();

    }

    private boolean incomplete(int... values) {
        return values.length != 4 || values[0] == -1 || values[1] == -1 || values[2] == -1 || values[3] == -1;
    }

    @Override
    public String getHelpDescription() {
        return "Calculates your IPPT Score based on MINDEF Score Tables\n" +
                "https://www.mindef.gov.sg/imindef/mindef_websites/atozlistings/army/microsites/afc/IPPT_mgt_system/ippt.html";
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.modules_ipptcalc, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.view_ippt: startActivity(new Intent(this, IpptScoringActivity.class)); return true;
            default: return super.onOptionsItemSelected(item);
        }
    }
}
