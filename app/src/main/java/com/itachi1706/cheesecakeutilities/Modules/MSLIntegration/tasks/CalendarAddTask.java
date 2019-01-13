package com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.tasks;

import com.google.api.services.calendar.model.Calendar;
import com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.CalendarAsyncTask;
import com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.model.CalendarInfo;
import com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.MSLActivity;

import java.io.IOException;

/**
 * Created by Kenneth on 13/1/2019.
 * for com.itachi1706.cheesecakeutilities.Modules.MSLIntegration in CheesecakeUtilities
 */
public class CalendarAddTask extends CalendarAsyncTask {

    private final Calendar cal;

    public CalendarAddTask(MSLActivity activity, Calendar mCal) {
        super(activity);
        this.cal = mCal;
    }

    @Override
    public String getTaskAction() {
        return "ADD";
    }

    @Override
    protected void doInBackground() throws IOException {
        Calendar calendar = client.calendars().insert(cal).setFields(CalendarInfo.FIELDS).execute();
        model.add(calendar);
    }
}
