package com.itachi1706.cheesecakeutilities.modules.ordCountdown;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.itachi1706.appupdater.Util.PrefHelper;
import com.itachi1706.cheesecakeutilities.R;
import com.itachi1706.cheesecakeutilities.util.CommonMethods;
import com.itachi1706.cheesecakeutilities.util.LogHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ORDSettingsActivity extends AppCompatActivity {

    private long enlistMS, ordMS, popMS, ptpMS, milestoneMS, pdoption;
    private String pesStatusString;
    private TextView enlistEt, ordEt, popEt, ptpEt, milestoneEt, leaveEt, offEt;
    private Spinner pesStatusSpinner, payDaySpinner;
    private DatePickerDialog.OnDateSetListener pop, ord, enlist, ptp, milestone;

    private SharedPreferences sp;

    private static final int UPDATE_ORD = 1, UPDATE_POP = 2, UPDATE_ENLIST = 3, UPDATE_PTP = 4, UPDATE_NONE = 5, UPDATE_STATUS = 6, UPDATE_MILESTONE = 7;

    public static final int PES_A = 1, PES_PTP = 2, PES_BP = 3, PES_C = 4, PES_E = 5; // PES Status
    public static final int ENHANCED_BMT = 9, PTP_BMT = 17, BP_BMT = 19, E_BMT = 4; // BMT Weeks
    public static final int ENHANCED_PES = 21, NORMAL_PES = 24; // NS Weeks
    public static final String SP_ORD = "ordcalc_ord", SP_PTP = "ordcalc_ptp",
            SP_POP = "ordcalc_pop", SP_ENLIST = "ordcalc_enlist", SP_STATUS = "ordcalc_status",
            SP_MILESTONE = "ordcalc_milestone", SP_PAYDAY = "ordcalc_payday", SP_LEAVE = "ordcalc_leave", SP_OFF = "ordcalc_off";
    public static final int PAYDAY_10 = 0, PAYDAY_12 = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ordsettings);

        this.enlistMS = 0;
        this.ordMS = 0;
        this.popMS = 0;
        this.ptpMS = 0;
        this.pop = new dateListener(UPDATE_POP);
        this.ord = new dateListener(UPDATE_ORD);
        this.enlist = new dateListener(UPDATE_ENLIST);
        this.milestone = new dateListener(UPDATE_MILESTONE);
        this.ptp = new dateListener(UPDATE_PTP);

        this.sp = PrefHelper.getDefaultSharedPreferences(this);

        enlistEt = findViewById(R.id.etEnlist);
        ordEt = findViewById(R.id.etORD);
        popEt = findViewById(R.id.etPOP);
        ptpEt = findViewById(R.id.etPTP);
        leaveEt = findViewById(R.id.etLeave);
        offEt = findViewById(R.id.etOff);
        milestoneEt = (EditText) findViewById(R.id.etMilestone);
        milestoneEt.setOnClickListener(this::milestoneDialog);
        popEt.setOnClickListener(this::popDialog);
        ptpEt.setOnClickListener(this::ptpDialog);
        ordEt.setOnClickListener(this::ordDialog);
        enlistEt.setOnClickListener(this::enlistDialog);
        CommonMethods.disableAutofill(getWindow().getDecorView());
        pesStatusSpinner = findViewById(R.id.spinnerPES);
        pesStatusString = pesStatusSpinner.getSelectedItem().toString();
        payDaySpinner = findViewById(R.id.spinnerPD);
        leaveEt.setText(String.valueOf(sp.getInt(SP_LEAVE, 0)));
        offEt.setText(String.valueOf(sp.getInt(SP_OFF, 0)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.popMS = sp.getLong(SP_POP, 0);
        this.ptpMS = sp.getLong(SP_PTP, 0);
        this.enlistMS = sp.getLong(SP_ENLIST, 0);
        this.ordMS = sp.getLong(SP_ORD, 0);
        this.milestoneMS = sp.getLong(SP_MILESTONE, 0);
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
                LogHelper.d("PES UPDATE", pesStatusSpinner.getSelectedItem().toString());
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
        edit.putLong(SP_MILESTONE, this.milestoneMS);
        int offs = 0, leaves = 0;
        if (!this.leaveEt.getText().toString().isEmpty()) leaves = Integer.parseInt(this.leaveEt.getText().toString());
        if (!this.offEt.getText().toString().isEmpty()) offs = Integer.parseInt(this.offEt.getText().toString());
        edit.putInt(SP_LEAVE, leaves);
        edit.putInt(SP_OFF, offs);
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
        edit.remove(SP_MILESTONE);
        edit.remove(SP_OFF);
        edit.remove(SP_LEAVE);
        edit.remove("ordcalc_status_pos");
        edit.apply();

        this.pesStatusSpinner.setSelection(0);
        this.offEt.setText("0");
        this.leaveEt.setText("0");

        this.enlistMS = 0;
        this.ordMS = 0;
        this.popMS = 0;
        this.ptpMS = 0;
        this.milestoneMS = 0;
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

    public void milestoneDialog(View v) {
        Calendar calendar = Calendar.getInstance();
        if (this.milestoneMS > 0) calendar.setTimeInMillis(this.milestoneMS);
        DatePickerDialog mDialog = new DatePickerDialog(this, this.milestone, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        mDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "Clear", (dialog, which) -> {
            milestoneMS = 0;
            ORDSettingsActivity.this.updateText(UPDATE_MILESTONE);
        });
        mDialog.show();
    }

    private class dateListener implements DatePickerDialog.OnDateSetListener {
        private int type;

        dateListener(int type) {
            this.type = type;
        }

        @Override
        public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
            Calendar calendar = getCal(i, i1, i2);
            switch (type) {
                case UPDATE_MILESTONE: ORDSettingsActivity.this.milestoneMS = calendar.getTimeInMillis(); break;
                case UPDATE_POP: ORDSettingsActivity.this.popMS = calendar.getTimeInMillis(); break;
                case UPDATE_ENLIST: ORDSettingsActivity.this.enlistMS = calendar.getTimeInMillis(); break;
                case UPDATE_PTP: ORDSettingsActivity.this.ptpMS = calendar.getTimeInMillis(); break;
                case UPDATE_ORD: ORDSettingsActivity.this.ordMS = calendar.getTimeInMillis(); break;
            }
            ORDSettingsActivity.this.updateText(type);
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
        if (this.milestoneMS != 0) {
            cal.setTimeInMillis(this.milestoneMS);
            milestoneEt.setText(dateFormat.format(cal.getTime()));
        } else milestoneEt.setText("");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.modules_ord_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:
                saveAndExit();
                return true;
            case R.id.clear:
                new AlertDialog.Builder(this).setTitle("Clearing options")
                        .setMessage("This will clear all of your settings. Are you sure you want to continue?")
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> clearSettings()).setNegativeButton(android.R.string.no, null).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
