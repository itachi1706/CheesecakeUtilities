package com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.model

import com.google.api.services.calendar.model.Calendar
import com.google.api.services.calendar.model.CalendarListEntry
import java.util.*

/**
 * Created by Kenneth on 13/1/2019.
 * for com.itachi1706.cheesecakeutilities.Modules.MSLIntegration in CheesecakeUtilities
 */
class CalendarModel {

    private val calendars = HashMap<String, CalendarInfo>()

    fun size(): Int {
        synchronized(calendars) {
            return calendars.size
        }
    }

    fun remove(id: String) {
        synchronized(calendars) {
            calendars.remove(id)
        }
    }

    operator fun get(id: String): CalendarInfo? {
        synchronized(calendars) {
            return calendars[id]
        }
    }

    fun add(calendarToAdd: Calendar) {
        synchronized(calendars) {
            val found = get(calendarToAdd.id)
            found?.update(calendarToAdd)
                    ?: calendars.put(calendarToAdd.id, CalendarInfo(calendarToAdd))
        }
    }

    fun add(calendarToAdd: CalendarListEntry) {
        synchronized(calendars) {
            val found = get(calendarToAdd.id)
            found?.update(calendarToAdd)
                    ?: calendars.put(calendarToAdd.id, CalendarInfo(calendarToAdd))
        }
    }

    fun reset(calendarsToAdd: List<CalendarListEntry>) {
        synchronized(calendars) {
            calendars.clear()
            for (calendarToAdd in calendarsToAdd) {
                add(calendarToAdd)
            }
        }
    }

    fun toSortedArray(): Array<CalendarInfo> {
        synchronized(calendars) {
            val result = ArrayList<CalendarInfo>()
            for (calendar in calendars.values) {
                result.add(calendar.clone())
            }
            result.sort()
            return result.toTypedArray()
        }
    }
}