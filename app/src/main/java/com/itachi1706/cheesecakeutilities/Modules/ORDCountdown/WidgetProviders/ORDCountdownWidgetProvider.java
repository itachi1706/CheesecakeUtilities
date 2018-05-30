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
@Deprecated
public class ORDCountdownWidgetProvider extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.ordcountdown_widget_provider);

        // Calculate ORD
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        long ord = sp.getLong(ORDSettingsActivity.SP_ORD, 0);
        long currentTime = System.currentTimeMillis();

        if (ord != 0) {
            if (currentTime > ord) {
                views.setTextViewText(R.id.wid_ord_counter, "ORD");
                views.setTextViewText(R.id.wid_ord_days_counter, "LOH");
            } else {
                long duration = ord - currentTime;
                long ordDays = TimeUnit.MILLISECONDS.toDays(duration) + 1;
                views.setTextViewText(R.id.wid_ord_counter, ordDays + "");
                views.setTextViewText(R.id.wid_ord_days_counter, context.getResources().getQuantityString(R.plurals.ord_days, (int) ordDays));
            }
        } else {
            views.setTextViewText(R.id.wid_ord_counter, "???");
            views.setTextViewText(R.id.wid_ord_days_counter, context.getResources().getQuantityString(R.plurals.ord_days, 999));
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

