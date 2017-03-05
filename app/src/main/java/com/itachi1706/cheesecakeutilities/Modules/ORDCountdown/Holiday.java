package com.itachi1706.cheesecakeutilities.Modules.ORDCountdown;

import java.util.Calendar;
import java.util.Comparator;

/**
 * Created by Kenneth on 5/3/2017.
 * for com.itachi1706.cheesecakeutilities.Modules.ORDCountdown in CheesecakeUtilities
 */

public class Holiday {

    private long time;
    private String holidayName, timeString;

    public Holiday(String holidayString) {
        String[] tmp = holidayString.split(":");
        String[] dates = tmp[1].split("-");
        Calendar cal = Calendar.getInstance();
        cal.set(Integer.parseInt(dates[2]), Integer.parseInt(dates[1]) - 1, Integer.parseInt(dates[0]), 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        this.time = cal.getTimeInMillis();
        this.holidayName = tmp[0];
        this.timeString = tmp[1];
    }

    public long getTime() {
        return time;
    }

    public String getHolidayName() {
        return holidayName;
    }

    public String getTimeString() {
        return timeString;
    }
}
