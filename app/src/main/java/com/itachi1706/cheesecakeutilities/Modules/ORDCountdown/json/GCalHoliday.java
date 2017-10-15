package com.itachi1706.cheesecakeutilities.Modules.ORDCountdown.json;

/**
 * Created by Kenneth on 15/10/2017.
 * for com.itachi1706.cheesecakeutilities.Modules.ORDCountdown.json in CheesecakeUtilities
 */

public class GCalHoliday {
    private String startYear, endYear, yearRange, timestamp, msg;
    private int size, error;
    private boolean cache;
    private GCalHolidayItem[] output;

    public String getStartYear() {
        return startYear;
    }

    public String getEndYear() {
        return endYear;
    }

    public String getYearRange() {
        return yearRange;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public long getTimestampLong() {
        return Long.parseLong(timestamp);
    }

    public String getMsg() {
        return msg;
    }

    public int getSize() {
        return size;
    }

    public int getError() {
        return error;
    }

    public boolean isCache() {
        return cache;
    }

    public GCalHolidayItem[] getOutput() {
        return output;
    }


}
