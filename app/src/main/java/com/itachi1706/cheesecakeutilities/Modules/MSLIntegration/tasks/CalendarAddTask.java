package com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.tasks;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.api.services.calendar.model.Calendar;
import com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.CalendarAsyncTask;
import com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.model.CalendarInfo;
import com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.model.CalendarModel;

import java.io.IOException;

/**
 * Created by Kenneth on 13/1/2019.
 * for com.itachi1706.cheesecakeutilities.Modules.MSLIntegration in CheesecakeUtilities
 */
public class CalendarAddTask extends CalendarAsyncTask {

    private final Calendar cal;
    private String pref;

    public CalendarAddTask(Context context, CalendarModel model, com.google.api.services.calendar.Calendar client, Calendar mCal, String prefStore) {
        super(context, model, client);
        this.cal = mCal;
        this.pref = prefStore;
    }

    @Override
    public String getTaskAction() {
        return "ADD";
    }

    @Override
    protected void doInBackground() throws IOException {
        Calendar calendar = client.calendars().insert(cal).setFields(CalendarInfo.FIELDS).execute();
        model.add(calendar);
        Log.i("MSL-ADD", "Created calendar with ID: " + calendar.getId());
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(pref, calendar.getId()).apply();
    }
}
