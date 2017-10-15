package com.itachi1706.cheesecakeutilities.Modules.ORDCountdown.json;

import java.util.Calendar;

/**
 * Created by Kenneth on 15/10/2017.
 * for com.itachi1706.cheesecakeutilities.Modules.ORDCountdown.json in CheesecakeUtilities
 */

public class GCalHolidayItem {
    private String name, date;

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public long getDateInMillis() {
        String[] dates = date.split("-");
        Calendar cal = Calendar.getInstance();
        cal.set(Integer.parseInt(dates[0]), Integer.parseInt(dates[1]) - 1, Integer.parseInt(dates[2]), 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }
}
