package com.itachi1706.cheesecakeutilities.Modules.CEPASReader.xml;

import org.simpleframework.xml.transform.Transform;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Transforms a java.util.Calendar into seconds since UNIX epoch (like EpochDateTransform)
 */

public class EpochCalendarTransform implements Transform<Calendar> {

    @Override
    public Calendar read(String value) {
        long s = Long.valueOf(value);
        Calendar c = GregorianCalendar.getInstance();
        c.setTimeInMillis(s);
        return c;
    }

    @Override
    public String write(Calendar value) {
        return String.valueOf(value.getTimeInMillis());
    }
}
