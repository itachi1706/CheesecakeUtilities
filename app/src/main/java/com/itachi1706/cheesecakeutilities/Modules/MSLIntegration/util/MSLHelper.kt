@file:Suppress("ConstantConditionIf")

package com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.util

import com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.model.MSLData

/**
 * Created by Kenneth on 17/1/2019.
 * for com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.util in CheesecakeUtilities
 */
@Deprecated("MSL Closing Down")
object MSLHelper {

    fun completeMatch(task: MSLData.Task, otherTask: MSLData.Task): Boolean {
        if (checkNull(task.guid, otherTask.guid) != 0) return false
        if (task.guid != null && task.guid != otherTask.guid) return false

        if (checkNull(task.type, otherTask.type) != 0) return false
        if (task.type != null && task.type != otherTask.type) return false

        if (checkNull(task.title, otherTask.title) != 0) return false
        if (task.title != null && task.title != otherTask.title) return false

        if (checkNull(task.detail, otherTask.detail) != 0) return false
        if (task.detail != null && task.detail != otherTask.detail) return false

        if (checkNull(task.due_date, otherTask.due_date) != 0) return false
        if (task.due_date != null && task.due_date != otherTask.due_date) return false

        if (checkNull(task.timestamp, otherTask.timestamp) != 0) return false
        if (task.timestamp != otherTask.timestamp) return false

        if (checkNull(task.completed_at, otherTask.completed_at) != 0) return false
        if (task.completed_at != null && task.completed_at != otherTask.completed_at) return false

        if (checkNull(task.subject_guid, otherTask.subject_guid) != 0) return false
        if (task.subject_guid != null && task.subject_guid != otherTask.subject_guid) return false

        return if (checkNull(task.progress, otherTask.progress) != 0) false else task.progress == otherTask.progress
    }

    fun completeMatch(task: MSLData.Exam, otherTask: MSLData.Exam): Boolean {
        if (checkNull(task.guid, otherTask.guid) != 0) return false
        if (task.guid != null && task.guid != otherTask.guid) return false

        if (checkNull(task.module, otherTask.module) != 0) return false
        if (task.module != null && task.module != otherTask.module) return false

        if (checkNull(task.date, otherTask.date) != 0) return false
        if (task.date != null && task.date != otherTask.date) return false

        if (checkNull(task.duration, otherTask.duration) != 0) return false
        if (task.duration != otherTask.duration) return false

        if (checkNull(task.isResit, otherTask.isResit) != 0) return false
        if (task.isResit != otherTask.isResit) return false

        if (checkNull(task.seat, otherTask.seat) != 0) return false
        if (task.seat != null && task.seat != otherTask.seat) return false

        if (checkNull(task.room, otherTask.room) != 0) return false
        if (task.room != null && task.room != otherTask.room) return false

        return if (checkNull(task.subject_guid, otherTask.subject_guid) != 0) false else task.subject_guid == null || task.subject_guid == otherTask.subject_guid
    }

    /**
     * Check if either object is null
     * @param o1 First Object
     * @param o2 Second Object
     * @return 0 if both null/not-null, -1 if o1 is null, 1 if o2 is null
     */
    private fun checkNull(o1: Any?, o2: Any?): Int {
        return if (o1 == null && o2 == null)
            0
        else if (o1 == null)
            -1
        else if (o2 == null)
            1
        else
            0
    }
}
