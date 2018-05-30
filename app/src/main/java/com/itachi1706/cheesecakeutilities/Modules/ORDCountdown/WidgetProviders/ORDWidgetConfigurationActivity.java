package com.itachi1706.cheesecakeutilities.Modules.ORDCountdown.WidgetProviders;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;

import com.itachi1706.cheesecakeutilities.R;
import com.itachi1706.cheesecakeutilities.RecyclerAdapters.StringRecyclerAdapter;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID;

/**
 * Created by Kenneth on 5/29/2018.
 * for com.itachi1706.cheesecakeutilities.Modules.ORDCountdown.WidgetProviders in CheesecakeUtilities
 */
public class ORDWidgetConfigurationActivity extends AppCompatActivity {

    String[] options = {"ORD", "POP", "Milestone"};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ord_widget_configure);
        setResult(RESULT_CANCELED); // In case user clicked back or cancel

        RecyclerView view = findViewById(R.id.config_recycler_view);
        view.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        view.setLayoutManager(linearLayoutManager);
        view.setItemAnimator(new DefaultItemAnimator());

        StringRecyclerAdapter adapter = new StringRecyclerAdapter(options, false);
        adapter.setOnClickListener(v -> {
                 TextView text = v.findViewById(R.id.text1);
                 showAppWidget(text.getText().toString());

        });
        view.setAdapter(adapter);
    }

    int mAppWidgetId;

    private void showAppWidget(String type) {

        mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(EXTRA_APPWIDGET_ID,
                    INVALID_APPWIDGET_ID);

            PreferenceManager.getDefaultSharedPreferences(this).edit().putString("ord_widgetid_" + mAppWidgetId, type).apply();

            Intent startService = new Intent(this,
                    EventCountdownWidgetProvider.UpdateWidgetService.class);
            startService.putExtra(EXTRA_APPWIDGET_ID, mAppWidgetId);
            startService.setAction("FROM CONFIGURATION ACTIVITY");
            setResult(RESULT_OK, startService);
            startService(startService);

            finish();
        }
        if (mAppWidgetId == INVALID_APPWIDGET_ID) {
            Log.i("I am invalid", "I am invalid");
            finish();
        }
    }
}
