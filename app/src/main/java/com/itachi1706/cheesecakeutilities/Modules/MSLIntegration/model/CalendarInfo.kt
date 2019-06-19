package com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.model

import com.google.android.gms.common.internal.Objects
import com.google.api.services.calendar.model.Calendar
import com.google.api.services.calendar.model.CalendarListEntry

/**
 * Created by Kenneth on 13/1/2019.
 * for com.itachi1706.cheesecakeutilities.Modules.MSLIntegration in CheesecakeUtilities
 */
@Deprecated("MSL Closing Down")
class CalendarInfo : Comparable<CalendarInfo>, Cloneable {

    private var id: String? = null
    private var summary: String? = null

    internal constructor(id: String, summary: String) {
        this.id = id
        this.summary = summary
    }

    internal constructor(calendar: Calendar) {
        update(calendar)
    }

    internal constructor(calendar: CalendarListEntry) {
        update(calendar)
    }

    override fun toString(): String {
        return Objects.toStringHelper(CalendarInfo::class.java).add("id", id).add("summary", summary)
                .toString()
    }

    override fun compareTo(other: CalendarInfo): Int {
        return summary!!.compareTo(other.summary!!)
    }

    public override fun clone(): CalendarInfo {
        try {
            return super.clone() as CalendarInfo
        } catch (exception: CloneNotSupportedException) {
            // should not happen
            throw RuntimeException(exception)
        }

    }

    fun update(calendar: Calendar) {
        id = calendar.id
        summary = calendar.summary
    }

    fun update(calendar: CalendarListEntry) {
        id = calendar.id
        summary = calendar.summary
    }

    companion object {

        const val FIELDS = "id,summary"
        const val FEED_FIELDS = "items($FIELDS)"
    }
}
