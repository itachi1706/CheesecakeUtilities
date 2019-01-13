package com.itachi1706.cheesecakeutilities.Modules.MSLIntegration;

import com.google.android.gms.common.internal.Objects;
import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.CalendarListEntry;

/**
 * Created by Kenneth on 13/1/2019.
 * for com.itachi1706.cheesecakeutilities.Modules.MSLIntegration in CheesecakeUtilities
 */
class CalendarInfo implements Comparable<CalendarInfo>, Cloneable {

    static final String FIELDS = "id,summary";
    static final String FEED_FIELDS = "items(" + FIELDS + ")";

    String id;
    String summary;

    CalendarInfo(String id, String summary) {
        this.id = id;
        this.summary = summary;
    }

    CalendarInfo(Calendar calendar) {
        update(calendar);
    }

    CalendarInfo(CalendarListEntry calendar) {
        update(calendar);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(CalendarInfo.class).add("id", id).add("summary", summary)
                .toString();
    }

    public int compareTo(CalendarInfo other) {
        return summary.compareTo(other.summary);
    }

    @Override
    public CalendarInfo clone() {
        try {
            return (CalendarInfo) super.clone();
        } catch (CloneNotSupportedException exception) {
            // should not happen
            throw new RuntimeException(exception);
        }
    }

    void update(Calendar calendar) {
        id = calendar.getId();
        summary = calendar.getSummary();
    }

    void update(CalendarListEntry calendar) {
        id = calendar.getId();
        summary = calendar.getSummary();
    }
}
