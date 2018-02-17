package com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.LongSparseArray;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.Objects.Record;
import com.itachi1706.cheesecakeutilities.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GenerateMileageRecordActivity extends AppCompatActivity {

    private TableLayout layout;
    private Spinner monthSel;
    private HorizontalScrollView hScroll;
    private ScrollView vScroll;

    private String user_id;
    private LongSparseArray<String> monthData = null;

    private static final String TAG = "GenerateMileageRec";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_mileage_record);

        layout = findViewById(R.id.veh_mileage_table);
        hScroll = findViewById(R.id.hscroll);
        vScroll = findViewById(R.id.vscroll);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        user_id = sp.getString("firebase_uid", "nien");
        if (user_id.equalsIgnoreCase("nien")) {
            // Fail, return to login activity
            Toast.makeText(this, "Invalid Login Token, please re-login", Toast.LENGTH_SHORT).show();
        }
    }

    private float mx, my;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float curX, curY;
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                mx = event.getX();
                my = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                curX = event.getX();
                curY = event.getY();
                vScroll.scrollBy((int) (mx - curX), (int) (my - curY));
                hScroll.scrollBy((int) (mx - curX), (int) (my - curY));
                mx = curX;
                my = curY;
                break;
            case MotionEvent.ACTION_UP:
                curX = event.getX();
                curY = event.getY();
                vScroll.scrollBy((int) (mx - curX), (int) (my - curY));
                hScroll.scrollBy((int) (mx - curX), (int) (my - curY));
                break;
        }

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.modules_veh_mileage_generate_report, menu);
        MenuItem menuItem = menu.findItem(R.id.menuMonth);
        monthSel = (Spinner) menuItem.getActionView();

        FirebaseUtils.getFirebaseDatabase().getReference().child("users").child(user_id).child("statistics")
                .child("timeRecords").child("perMonth").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                monthData = new LongSparseArray<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    long key = Long.parseLong(ds.getKey());
                    Date d = new Date(key);
                    SimpleDateFormat sd = new SimpleDateFormat("MMMM yyyy", Locale.US);
                    monthData.put(key, sd.format(d));
                }
                Log.d(TAG, "Month Data Size: " + monthData.size());
                List<String> tmp = new ArrayList<>();
                for (int i = 0; i < monthData.size(); i++) {
                    tmp.add(monthData.valueAt(i));
                }

                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.simple_spinner_item_white, tmp);
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                monthSel.setAdapter(spinnerAdapter);
                monthSel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view1, int position, long id) {
                        if (position >= monthData.size()) {
                            // Error
                            Log.e(TAG, "Position #" + position + " exceeds dataset size of " + monthData.size());
                            return;
                        }
                        long date = monthData.keyAt(position);
                        processDate(date);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return true;
    }

    private void processDate(long date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(date));
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
        cal.set(Calendar.HOUR_OF_DAY, cal.getActualMinimum(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, cal.getActualMinimum(Calendar.MINUTE));
        cal.set(Calendar.SECOND, cal.getActualMinimum(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, cal.getActualMinimum(Calendar.MILLISECOND));
        long startDate = cal.getTimeInMillis();
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        cal.set(Calendar.HOUR_OF_DAY, cal.getActualMaximum(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, cal.getActualMaximum(Calendar.MINUTE));
        cal.set(Calendar.SECOND, cal.getActualMaximum(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, cal.getActualMaximum(Calendar.MILLISECOND));
        long endDate = cal.getTimeInMillis();

        Query specificMonth = FirebaseUtils.getFirebaseDatabase().getReference().child("users").child(user_id).child("records")
                .orderByChild("datetimeFrom").startAt(startDate).endAt(endDate);
        specificMonth.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final List<Record> records = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Record recList = ds.getValue(Record.class);
                    // Update records
                    assert recList != null;
                    records.add(recList);
                }
                Log.i(TAG, "Records: " + records.size());
                processRecords(records);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private double totalC3 = 0, totalC4 = 0;

    private void processRecords(List<Record> records) {
        layout.removeAllViews();
        totalC4 = 0;
        totalC3 = 0;
        generateHeaders();
        int row = 1;
        for (Record r : records) {
            if (generateData(r, row)) row++;
        }
        generateFooter(row);
    }

    private void generateHeaders() {
        TableRow tr = new TableRow(this);
        tr.addView(getTextView(0, "S/N", true));
        tr.addView(getTextView(0, "DATE", true));
        tr.addView(getTextView(0, "VEHICLE NO", true));
        tr.addView(getTextView(0, "START", true));
        tr.addView(getTextView(0, "END", true));
        tr.addView(getTextView(0, "CLASS 3", true));
        tr.addView(getTextView(0, "CLASS 4", true));
        layout.addView(tr, getTblLayoutParams());
    }

    private boolean generateData(Record record, int col) {
        TableRow tr = new TableRow(this);
        tr.addView(getTextView(col, col + ""));
        // Process Date
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy", Locale.US);
        Date dt = new Date();
        dt.setTime(record.getDatetimeFrom());
        tr.addView(getTextView(col, sdf.format(dt)));
        tr.addView(getTextView(col, record.getVehicleNumber()));
        tr.addView(getTextView(col, record.getMileageFrom() + ""));
        tr.addView(getTextView(col, record.getMileageTo() + ""));
        if (record.getVehicleClass().equalsIgnoreCase("class3")) {
            tr.addView(getTextView(col, record.getTotalMileage() + ""));
            tr.addView(getTextView(col, "-"));
            totalC3 += record.getTotalMileage();
        } else if (record.getVehicleClass().equalsIgnoreCase("class4")) {
            tr.addView(getTextView(col, "-"));
            tr.addView(getTextView(col, record.getTotalMileage() + ""));
            totalC4 += record.getTotalMileage();
        } else return false;
        layout.addView(tr, getTblLayoutParams());
        return true;
    }

    private void generateFooter(int col) {
        TableRow tr = new TableRow(this);
        tr.addView(getTextView(col, ""));
        tr.addView(getTextView(col, ""));
        tr.addView(getTextView(col, ""));
        tr.addView(getTextView(col, ""));
        tr.addView(getTextView(col, "TOTAL", true));
        tr.addView(getTextView(col, totalC3 + ""));
        tr.addView(getTextView(col, totalC4 + ""));
        layout.addView(tr, getTblLayoutParams());
    }

    private TextView getTextView(int id, String title) {
        return getTextView(id, title, false);
    }

    private TextView getTextView(int id, String title, boolean bold) {
        TextView tv = new TextView(this);
        tv.setId(id);
        tv.setText(title);
        if (bold) tv.setTypeface(null, Typeface.BOLD);
        tv.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
        tv.setPadding(40, 5, 40, 5);
        tv.setLayoutParams(getLayoutParams());
        return tv;
    }

    @NonNull
    private TableRow.LayoutParams getLayoutParams() {
        TableRow.LayoutParams params = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_HORIZONTAL;
        params.setMargins(2, 0, 0, 2);
        return params;
    }

    @NonNull
    private TableLayout.LayoutParams getTblLayoutParams() {
        return new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT);
    }
}
