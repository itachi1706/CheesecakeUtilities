package com.itachi1706.cheesecakeutilities;

import android.app.DatePickerDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ORDSettingsActivity extends AppCompatActivity {

    private long enlistMS, ordMS, popMS, ptpMS;
    private TextView enlistEt, ordEt, popEt, ptpEt;
    private DatePickerDialog.OnDateSetListener pop,ord,enlist,ptp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ordsettings);

        this.enlistMS = 0;
        this.ordMS = 0;
        this.popMS = 0;
        this.ptpMS = 0;
        this.pop = new popListener();
        this.ord = new ordListener();
        this.enlist = new enlistListener();
        this.ptp = new ptpListener();

        enlistEt = (EditText) findViewById(R.id.etEnlist);
        ordEt = (EditText) findViewById(R.id.etORD);
        popEt = (EditText) findViewById(R.id.etPOP);
        ptpEt = (EditText) findViewById(R.id.etPTP);
    }

    public void ptpDialog(View v) {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, this.ptp, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    public void ordDialog(View v) {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, this.ord, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    public void enlistDialog(View v) {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, this.enlist, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    public void popDialog(View v) {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, this.pop, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    class popListener implements DatePickerDialog.OnDateSetListener {

        @Override
        public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
            Calendar calendar = getCal(i, i1, i2);
            ORDSettingsActivity.this.popMS = calendar.getTimeInMillis();
            ORDSettingsActivity.this.updateText();
        }
    }

    class enlistListener implements DatePickerDialog.OnDateSetListener {

        @Override
        public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
            Calendar calendar = getCal(i, i1, i2);
            ORDSettingsActivity.this.enlistMS = calendar.getTimeInMillis();
            ORDSettingsActivity.this.updateText();
        }
    }

    class ptpListener implements DatePickerDialog.OnDateSetListener {

        @Override
        public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
            Calendar calendar = getCal(i, i1, i2);
            ORDSettingsActivity.this.ptpMS = calendar.getTimeInMillis();
            ORDSettingsActivity.this.updateText();
        }
    }

    class ordListener implements DatePickerDialog.OnDateSetListener {

        @Override
        public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
            Calendar calendar = getCal(i, i1, i2);
            ORDSettingsActivity.this.ordMS = calendar.getTimeInMillis();
            ORDSettingsActivity.this.updateText();
        }
    }

    public static Calendar getCal(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        clearTime(cal);
        return cal;
    }

    public static void clearTime(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }

    private void updateText() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
        TextView txt = enlistEt;
        if (this.enlistMS != 0) {
            cal.setTimeInMillis(this.enlistMS);
            txt.setText(dateFormat.format(cal.getTime()));
        }
        txt = ptpEt;
        if (this.ptpMS != 0) {
            cal.setTimeInMillis(this.ptpMS);
            txt.setText(dateFormat.format(cal.getTime()));
        }
        txt = popEt;
        if (this.popMS != 0) {
            cal.setTimeInMillis(this.popMS);
            txt.setText(dateFormat.format(cal.getTime()));
        }
        txt = ordEt;
        if (this.ordMS != 0) {
            cal.setTimeInMillis(this.ordMS);
            txt.setText(dateFormat.format(cal.getTime()));
        }
    }
}
