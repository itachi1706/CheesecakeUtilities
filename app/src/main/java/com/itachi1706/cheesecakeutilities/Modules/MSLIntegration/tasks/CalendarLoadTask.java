package com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.tasks;

import android.content.Context;
import android.util.Log;

import com.google.api.services.calendar.model.CalendarList;
import com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.CalendarAsyncTask;
import com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.model.CalendarInfo;
import com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.model.CalendarModel;

import java.io.IOException;

/**
 * Created by Kenneth on 14/1/2019.
 * for com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.tasks in CheesecakeUtilities
 */
public class CalendarLoadTask extends CalendarAsyncTask {

    private String action;

    CalendarLoadTask(Context context, CalendarModel model, com.google.api.services.calendar.Calendar client, String action) {
        super(context, model, client);
        this.action = (action.isEmpty()) ? "" : action;
    }

    @Override
    public String getTaskAction() {
        if (!action.isEmpty()) return "LOAD-" + action;
        return "LOAD";
    }

    @Override
    protected void doInBackground() throws IOException {
        Log.d("MSL-LOAD", "Loading calendars for " + action + "...");
        CalendarList feed = client.calendarList().list().setFields(CalendarInfo.FEED_FIELDS).execute();
        model.reset(feed.getItems());
    }

    public static void run(Context context, String action, CalendarModel model, com.google.api.services.calendar.Calendar client) {
        new CalendarLoadTask(context, model, client, action).execute();
    }

}
