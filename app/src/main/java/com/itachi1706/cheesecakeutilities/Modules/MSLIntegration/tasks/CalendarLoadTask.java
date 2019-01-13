package com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.tasks;

import android.util.Log;

import com.google.api.services.calendar.model.CalendarList;
import com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.CalendarAsyncTask;
import com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.MSLActivity;
import com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.model.CalendarInfo;

import java.io.IOException;

/**
 * Created by Kenneth on 14/1/2019.
 * for com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.tasks in CheesecakeUtilities
 */
public class CalendarLoadTask extends CalendarAsyncTask {

    private String action;

    CalendarLoadTask(MSLActivity calendar, String action) {
        super(calendar);
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

    public static void run(MSLActivity calendar, String action) {
        new CalendarLoadTask(calendar, action).execute();
    }

}
