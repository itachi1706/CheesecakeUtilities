package com.itachi1706.cheesecakeutilities.Modules.MSLIntegration;

import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.CalendarListEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Kenneth on 13/1/2019.
 * for com.itachi1706.cheesecakeutilities.Modules.MSLIntegration in CheesecakeUtilities
 */
class CalendarModel {

    private final Map<String, CalendarInfo> calendars = new HashMap<String, CalendarInfo>();

    int size() {
        synchronized (calendars) {
            return calendars.size();
        }
    }

    void remove(String id) {
        synchronized (calendars) {
            calendars.remove(id);
        }
    }

    CalendarInfo get(String id) {
        synchronized (calendars) {
            return calendars.get(id);
        }
    }

    void add(Calendar calendarToAdd) {
        synchronized (calendars) {
            CalendarInfo found = get(calendarToAdd.getId());
            if (found == null) {
                calendars.put(calendarToAdd.getId(), new CalendarInfo(calendarToAdd));
            } else {
                found.update(calendarToAdd);
            }
        }
    }

    void add(CalendarListEntry calendarToAdd) {
        synchronized (calendars) {
            CalendarInfo found = get(calendarToAdd.getId());
            if (found == null) {
                calendars.put(calendarToAdd.getId(), new CalendarInfo(calendarToAdd));
            } else {
                found.update(calendarToAdd);
            }
        }
    }

    void reset(List<CalendarListEntry> calendarsToAdd) {
        synchronized (calendars) {
            calendars.clear();
            for (CalendarListEntry calendarToAdd : calendarsToAdd) {
                add(calendarToAdd);
            }
        }
    }

    public CalendarInfo[] toSortedArray() {
        synchronized (calendars) {
            List<CalendarInfo> result = new ArrayList<CalendarInfo>();
            for (CalendarInfo calendar : calendars.values()) {
                result.add(calendar.clone());
            }
            Collections.sort(result);
            return result.toArray(new CalendarInfo[0]);
        }
    }
}