package com.itachi1706.cheesecakeutilities.modules.ordCountdown.WidgetProviders;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import com.itachi1706.appupdater.Util.PrefHelper;
import com.itachi1706.cheesecakeutilities.MainMenuActivity;
import com.itachi1706.cheesecakeutilities.modules.ordCountdown.ORDActivity;
import com.itachi1706.cheesecakeutilities.modules.ordCountdown.ORDSettingsActivity;
import com.itachi1706.cheesecakeutilities.R;

import java.util.concurrent.TimeUnit;

import androidx.core.app.TaskStackBuilder;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID;

/**
 * Implementation of App Widget functionality.
 */
public class EventCountdownWidgetProvider extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.eventcountdown_widget_provider);

        // Calculate ORD
        SharedPreferences sp = PrefHelper.getDefaultSharedPreferences(context);
        String type = sp.getString("ord_widgetid_" + appWidgetId, "-");

        long time = 0;
        String completeMain = "???", completeSub = "LOH";
        int pluralToUse = R.plurals.ord_event_days;

        switch (type.toUpperCase()) {
            case "POP":
                time = sp.getLong(ORDSettingsActivity.SP_POP, 0);
                completeMain = "POP";
                break;
            case "ORD":
                time = sp.getLong(ORDSettingsActivity.SP_ORD, 0);
                completeMain = "ORD";
                break;
            case "MILESTONE":
                time = sp.getLong(ORDSettingsActivity.SP_MILESTONE, 0);
                completeMain = "M. Parade";
                completeSub = "Completed";
                break;
            default: pluralToUse = -1; break;
        }

        long currentTime = System.currentTimeMillis();
        if (pluralToUse == -1) {
            // Unknown
            views.setTextViewText(R.id.wid_ord_counter, "Unknown");
            views.setTextViewText(R.id.wid_ord_days_counter, "TYPE");
        } else if (time != 0) {
            if (currentTime > time) {
                views.setTextViewText(R.id.wid_ord_counter, completeMain);
                views.setTextViewText(R.id.wid_ord_days_counter, completeSub);
            } else {
                long duration = time - currentTime;
                long daysTaken = TimeUnit.MILLISECONDS.toDays(duration) + 1;
                views.setTextViewText(R.id.wid_ord_counter, Long.toString(daysTaken));
                views.setTextViewText(R.id.wid_ord_days_counter, context.getResources().getQuantityString(R.plurals.ord_event_days, (int) daysTaken, completeMain));
            }
        } else {
            views.setTextViewText(R.id.wid_ord_counter, "???");
            views.setTextViewText(R.id.wid_ord_days_counter, context.getResources().getQuantityString(R.plurals.ord_event_days, 999, completeMain));
        }

        Intent ordIntent = new Intent(context, ORDActivity.class);
        ordIntent.putExtra("menuitem", "ORD Countdown");
        ordIntent.putExtra("globalcheck", true);
        TaskStackBuilder backStack = TaskStackBuilder.create(context);
        backStack.addParentStack(MainMenuActivity.class);
        backStack.addNextIntent(new Intent(context, MainMenuActivity.class));
        backStack.addNextIntent(ordIntent);
        PendingIntent intent = backStack.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.wid_ord_main, intent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        if (appWidgetIds != null) {
            for (int mAppWidgetId : appWidgetIds) {
                Intent intent = new Intent(context, UpdateWidgetService.class);
                intent.putExtra(EXTRA_APPWIDGET_ID, mAppWidgetId);
                intent.setAction("FROM WIDGET PROVIDER");
                context.startService(intent);
            }

        }
    }

    public static class UpdateWidgetService extends IntentService {
        public UpdateWidgetService() {
            // only for debug purpose
            super("UpdateWidgetService");

        }

        @Override
        protected void onHandleIntent(Intent intent) {
            AppWidgetManager appWidgetManager = AppWidgetManager
                    .getInstance(UpdateWidgetService.this);

            int incomingAppWidgetId = intent.getIntExtra(EXTRA_APPWIDGET_ID,
                    INVALID_APPWIDGET_ID);

            if (incomingAppWidgetId != INVALID_APPWIDGET_ID) {
                try {
                    updateAppWidget(getApplicationContext(), appWidgetManager, incomingAppWidgetId);
                } catch (NullPointerException ignored) {
                    // Ignore exception
                }
            }
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        SharedPreferences sp = PrefHelper.getDefaultSharedPreferences(context);

        for (int appWidget : appWidgetIds) {
            sp.edit().remove("ord_widgetid_" + appWidget).apply();
        }
    }
}

