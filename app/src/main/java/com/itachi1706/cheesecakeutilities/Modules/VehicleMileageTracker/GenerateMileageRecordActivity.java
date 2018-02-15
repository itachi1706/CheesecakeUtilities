package com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.LongSparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GenerateMileageRecordActivity extends AppCompatActivity {

    TableLayout layout;
    Spinner monthSel;
    SharedPreferences sp;
    String user_id;
    LongSparseArray<String> monthData = null;

    private static final String TAG = "GenerateMileageRec";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_mileage_record);

        layout = findViewById(R.id.veh_mileage_table);

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        user_id = sp.getString("firebase_uid", "nien");
        if (user_id.equalsIgnoreCase("nien")) {
            // Fail, return to login activity
            Toast.makeText(this, "Invalid Login Token, please re-login", Toast.LENGTH_SHORT).show();
        }
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
        int lastDate = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        cal.set(Calendar.DAY_OF_MONTH, lastDate);
        long endDate = cal.getTimeInMillis();

        Query specificMonth = FirebaseUtils.getFirebaseDatabase().getReference().child("users").child(user_id).child("records")
                .orderByChild("datetimeFrom").startAt(date).endAt(endDate);
        specificMonth.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final List<Record> records = new ArrayList<>();
                final List<String> tags = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Record recList = ds.getValue(Record.class);
                    // Update records
                    assert recList != null;
                    records.add(recList);
                    tags.add(ds.getKey());
                }
                Log.i(TAG, "Records: " + records.size());
                Collections.reverse(records);
                Collections.reverse(tags);
                processRecords(records, tags);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void processRecords(List<Record> records, List<String> keys) {
        // TODO: Generate the report in the table view
        layout.removeAllViews();
        generateHeaders();
    }

    private void generateHeaders() {
        TableRow tr = new TableRow(this);
        tr.addView(getTextView(0, "Date"));
        tr.addView(getTextView(0, "Vehicle Number"));
        tr.addView(getTextView(0, "Start"));
        tr.addView(getTextView(0, "End"));
        tr.addView(getTextView(0, "Class 3"));
        tr.addView(getTextView(0, "Class 4"));
        layout.addView(tr, getTblLayoutParams());
    }

    private TextView getTextView(int id, String title) {
        TextView tv = new TextView(this);
        tv.setId(id);
        tv.setText(title.toUpperCase());
        tv.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
        tv.setPadding(40, 40, 40, 40);
        tv.setLayoutParams(getLayoutParams());
        return tv;
    }

    @NonNull
    private TableRow.LayoutParams getLayoutParams() {
        TableRow.LayoutParams params = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT);
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
