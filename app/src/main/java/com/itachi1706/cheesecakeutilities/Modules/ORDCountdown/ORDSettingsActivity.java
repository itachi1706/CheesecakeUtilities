package com.itachi1706.cheesecakeutilities.Modules.ORDCountdown;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.itachi1706.cheesecakeutilities.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ORDSettingsActivity extends AppCompatActivity {

    private long enlistMS, ordMS, popMS, ptpMS, pdoption;
    private String pesStatusString;
    private TextView enlistEt, ordEt, popEt, ptpEt;
    private Spinner pesStatusSpinner, payDaySpinner;
    private DatePickerDialog.OnDateSetListener pop,ord,enlist,ptp;

    private SharedPreferences sp;

    private static final int UPDATE_ORD = 1, UPDATE_POP = 2, UPDATE_ENLIST = 3, UPDATE_PTP = 4, UPDATE_NONE = 5, UPDATE_STATUS=6;

    public static final int PES_A = 1, PES_PTP = 2, PES_BP = 3, PES_C = 4, PES_E = 5; // PES Status
    public static final int ENHANCED_BMT = 9, PTP_BMT = 17, BP_BMT = 19, E_BMT = 4; // BMT Weeks
    public static final int ENHANCED_PES = 21, NORMAL_PES = 24; // NS Weeks
    public static final String SP_ORD = "ordcalc_ord", SP_PTP = "ordcalc_ptp",
            SP_POP = "ordcalc_pop", SP_ENLIST = "ordcalc_enlist", SP_STATUS = "ordcalc_status", SP_PAYDAY = "ordcalc_payday";
    public static final int PAYDAY_10 = 0, PAYDAY_12 = 1;

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

        this.sp = PreferenceManager.getDefaultSharedPreferences(this);

        enlistEt = (EditText) findViewById(R.id.etEnlist);
        ordEt = (EditText) findViewById(R.id.etORD);
        popEt = (EditText) findViewById(R.id.etPOP);
        ptpEt = (EditText) findViewById(R.id.etPTP);
        popEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popDialog(v);
            }
        });
        ptpEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ptpDialog(v);
            }
        });
        ordEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ordDialog(v);
            }
        });
        enlistEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enlistDialog(v);
            }
        });

        pesStatusSpinner = (Spinner) findViewById(R.id.spinnerPES);
        pesStatusString = pesStatusSpinner.getSelectedItem().toString();
        payDaySpinner = (Spinner) findViewById(R.id.spinnerPD);
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.popMS = sp.getLong(SP_POP, 0);
        this.ptpMS = sp.getLong(SP_PTP, 0);
        this.enlistMS = sp.getLong(SP_ENLIST, 0);
        this.ordMS = sp.getLong(SP_ORD, 0);
        int statusTmp = sp.getInt("ordcalc_status_pos", -2);
        if (statusTmp != 0) {
            this.pesStatusSpinner.setSelection(statusTmp, true);
        }
        this.pdoption = sp.getLong(SP_PAYDAY, -1);
        if (this.pdoption != -1) {
            this.payDaySpinner.setSelection((int) this.pdoption);
        }
        this.pesStatusString = this.pesStatusSpinner.getSelectedItem().toString();
        this.updateText(UPDATE_NONE);
        pesStatusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d("PES UPDATE", pesStatusSpinner.getSelectedItem().toString());
                ORDSettingsActivity.this.pesStatusString = pesStatusSpinner.getSelectedItem().toString();
                ORDSettingsActivity.this.updateText(UPDATE_STATUS);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        payDaySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                pdoption = payDaySpinner.getSelectedItemId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void saveSettings() {
        SharedPreferences.Editor edit = sp.edit();
        edit.putLong(SP_POP, this.popMS);
        edit.putLong(SP_PTP, this.ptpMS);
        edit.putLong(SP_ENLIST, this.enlistMS);
        edit.putLong(SP_ORD, this.ordMS);
        edit.putString(SP_STATUS, this.pesStatusString);
        edit.putLong(SP_PAYDAY, this.pdoption);
        edit.putInt("ordcalc_status_pos", this.pesStatusSpinner.getSelectedItemPosition());
        edit.apply();
        Toast.makeText(this, "Settings Saved", Toast.LENGTH_LONG).show();
    }

    private void clearSettings() {
        SharedPreferences.Editor edit = sp.edit();
        edit.remove(SP_POP);
        edit.remove(SP_PTP);
        edit.remove(SP_ENLIST);
        edit.remove(SP_ORD);
        edit.remove(SP_STATUS);
        edit.remove("ordcalc_status_pos");
        edit.apply();

        this.pesStatusSpinner.setSelection(0);

        this.enlistMS = 0;
        this.ordMS = 0;
        this.popMS = 0;
        this.ptpMS = 0;
        this.pesStatusString = this.pesStatusSpinner.getSelectedItem().toString();

        this.updateText(UPDATE_NONE);
        Toast.makeText(this, "Cleared Settings", Toast.LENGTH_LONG).show();
    }

    private void saveAndExit() {
        saveSettings();
        this.finish();
    }

    public void ptpDialog(View v) {
        Calendar calendar = Calendar.getInstance();
        if (this.ptpMS > 0) calendar.setTimeInMillis(this.ptpMS);
        new DatePickerDialog(this, this.ptp, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    public void ordDialog(View v) {
        Calendar calendar = Calendar.getInstance();
        if (this.ordMS > 0) calendar.setTimeInMillis(this.ordMS);
        new DatePickerDialog(this, this.ord, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    public void enlistDialog(View v) {
        Calendar calendar = Calendar.getInstance();
        if (this.enlistMS > 0) calendar.setTimeInMillis(this.enlistMS);
        new DatePickerDialog(this, this.enlist, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    public void popDialog(View v) {
        Calendar calendar = Calendar.getInstance();
        if (this.popMS > 0) calendar.setTimeInMillis(this.popMS);
        new DatePickerDialog(this, this.pop, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    class popListener implements DatePickerDialog.OnDateSetListener {

        @Override
        public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
            Calendar calendar = getCal(i, i1, i2);
            ORDSettingsActivity.this.popMS = calendar.getTimeInMillis();
            ORDSettingsActivity.this.updateText(UPDATE_POP);
        }
    }

    class enlistListener implements DatePickerDialog.OnDateSetListener {

        @Override
        public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
            Calendar calendar = getCal(i, i1, i2);
            ORDSettingsActivity.this.enlistMS = calendar.getTimeInMillis();
            ORDSettingsActivity.this.updateText(UPDATE_ENLIST);
        }
    }

    class ptpListener implements DatePickerDialog.OnDateSetListener {

        @Override
        public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
            Calendar calendar = getCal(i, i1, i2);
            ORDSettingsActivity.this.ptpMS = calendar.getTimeInMillis();
            ORDSettingsActivity.this.updateText(UPDATE_PTP);
        }
    }

    class ordListener implements DatePickerDialog.OnDateSetListener {

        @Override
        public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
            Calendar calendar = getCal(i, i1, i2);
            ORDSettingsActivity.this.ordMS = calendar.getTimeInMillis();
            ORDSettingsActivity.this.updateText(UPDATE_ORD);
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

    private int getPesStatus() {
        return getPesStatus(pesStatusString);
    }

    public static int getPesStatus(String pes) {
        switch (pes) {
            case "A/B": return PES_A;
            case "A/B PTP": return PES_PTP;
            case "BP": return PES_BP;
            case "C": return PES_C;
            case "E": return PES_E;
            default: return PES_A;
        }
    }

    private void updatePtpTime() {
        int pesStatus = getPesStatus();
        if (pesStatus == PES_A || pesStatus == PES_E || pesStatus == PES_C) {
            this.ptpMS = 0;
            return;
        }
        if (this.enlistMS < 0) return;
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(this.enlistMS);
        switch (pesStatus) {
            case PES_BP: cal.add(Calendar.WEEK_OF_YEAR, (BP_BMT - ENHANCED_BMT)); break;
            case PES_PTP: cal.add(Calendar.WEEK_OF_YEAR, (PTP_BMT - ENHANCED_BMT)); break;
            default: return;
        }
        this.ptpMS = cal.getTimeInMillis();
    }

    private void updateOrdTime() {
        if (this.enlistMS <= 0) return;
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(this.enlistMS);
        if (getPesStatus() == PES_A)
            cal.add(Calendar.MONTH, ENHANCED_PES);
        else
            cal.add(Calendar.MONTH, NORMAL_PES);
        cal.add(Calendar.DATE, -1);
        this.ordMS = cal.getTimeInMillis();
    }

    private void updatePopTime() {
        if (this.enlistMS <= 0) return;
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(this.enlistMS);
        switch (getPesStatus()) {
            case PES_A:
            case PES_C: cal.add(Calendar.WEEK_OF_YEAR, ENHANCED_BMT); break;
            case PES_PTP: cal.add(Calendar.WEEK_OF_YEAR, PTP_BMT); break;
            case PES_BP: cal.add(Calendar.WEEK_OF_YEAR, BP_BMT); break;
            case PES_E: cal.add(Calendar.WEEK_OF_YEAR, E_BMT); break;
            default: return;
        }
        this.popMS = cal.getTimeInMillis();
    }

    private void updateText(int updated) {
        // Do calculations
        if (updated != UPDATE_NONE) {
            switch (updated) {
                case UPDATE_ENLIST:
                case UPDATE_STATUS:
                    updatePtpTime();
                    updatePopTime();
                    updateOrdTime();
                    break;
            }
        }
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.US);

        if (this.enlistMS != 0) {
            cal.setTimeInMillis(this.enlistMS);
            enlistEt.setText(dateFormat.format(cal.getTime()));
        } else enlistEt.setText("");
        if (this.ptpMS != 0) {
            cal.setTimeInMillis(this.ptpMS);
            ptpEt.setText(dateFormat.format(cal.getTime()));
        } else ptpEt.setText("");
        if (this.popMS != 0) {
            cal.setTimeInMillis(this.popMS);
            popEt.setText(dateFormat.format(cal.getTime()));
        } else popEt.setText("");
        if (this.ordMS != 0) {
            cal.setTimeInMillis(this.ordMS);
            ordEt.setText(dateFormat.format(cal.getTime()));
        } else ordEt.setText("");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.modules_ord_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save: saveAndExit(); return true;
            case R.id.clear: new AlertDialog.Builder(this).setTitle("Clearing options")
                    .setMessage("This will clear all of your settings. Are you sure you want to continue?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            clearSettings();
                        }
                    }).setNegativeButton(android.R.string.no, null).show(); return true;
            default: return super.onOptionsItemSelected(item);
        }
    }
}
