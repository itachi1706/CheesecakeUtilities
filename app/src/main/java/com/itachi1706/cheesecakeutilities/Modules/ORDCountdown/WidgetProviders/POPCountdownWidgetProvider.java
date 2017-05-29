package com.itachi1706.cheesecakeutilities.Modules.ORDCountdown.WidgetProviders;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;

import com.itachi1706.cheesecakeutilities.MainMenuActivity;
import com.itachi1706.cheesecakeutilities.Modules.ORDCountdown.ORDActivity;
import com.itachi1706.cheesecakeutilities.Modules.ORDCountdown.ORDSettingsActivity;
import com.itachi1706.cheesecakeutilities.R;

import java.util.concurrent.TimeUnit;

/**
 * Implementation of App Widget functionality.
 */
public class POPCountdownWidgetProvider extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.popcountdown_widget_provider);

        // Calculate ORD
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        long pop = sp.getLong(ORDSettingsActivity.SP_POP, 0);
        long currentTime = System.currentTimeMillis();

        if (pop != 0) {
            if (currentTime > pop) {
                views.setTextViewText(R.id.wid_ord_counter, "POP");
                views.setTextViewText(R.id.wid_ord_days_counter, "LOH");
            } else {
                long duration = pop - currentTime;
                long popDays = TimeUnit.MILLISECONDS.toDays(duration) + 1;
                views.setTextViewText(R.id.wid_ord_counter, popDays + "");
                views.setTextViewText(R.id.wid_ord_days_counter, context.getResources().getQuantityString(R.plurals.ord_pop_days, (int) popDays));
            }
        } else {
            views.setTextViewText(R.id.wid_ord_counter, "???");
            views.setTextViewText(R.id.wid_ord_days_counter, context.getResources().getQuantityString(R.plurals.ord_pop_days, 999));
        }

        Intent popIntent = new Intent(context, ORDActivity.class);
        popIntent.putExtra("menuitem", "ORD Countdown");
        popIntent.putExtra("globalcheck", true);
        TaskStackBuilder backStack = TaskStackBuilder.create(context);
        backStack.addParentStack(MainMenuActivity.class);
        backStack.addNextIntent(new Intent(context, MainMenuActivity.class));
        backStack.addNextIntent(popIntent);
        PendingIntent intent = backStack.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.wid_ord_main, intent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
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
}

