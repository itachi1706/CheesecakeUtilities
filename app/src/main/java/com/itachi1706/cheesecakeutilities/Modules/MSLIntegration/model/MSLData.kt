package com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.model

import androidx.annotation.Keep

/**
 * Created by Kenneth on 14/1/2019.
 * for com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.model in CheesecakeUtilities
 */
class MSLData {

    val subjects: Array<Subjects>? = null
    val tasks: Array<Task>? = null
    val exams: Array<Exam>? = null

    @Keep
    inner class Subjects {
        val guid: String? = null
        val name: String? = null
    }

    inner class Task {
        val guid: String? = null
        val type: String? = null
        val title: String? = null
        val detail: String? = null
        val due_date: String? = null
        val timestamp: Double = 0.toDouble()
        val completed_at: String? = null
        val subject_guid: String? = null
        val exam_guid: String? = null
        val progress: Int = 0

        // If revision
        var examString: String? = null
    }

    inner class Exam {
        val guid: String? = null
        val module: String? = null
        val date: String? = null
        val duration: Int = 0
        val isResit: Boolean = false
        val seat: String? = null
        val room: String? = null
        val subject_guid: String? = null
    }
}
