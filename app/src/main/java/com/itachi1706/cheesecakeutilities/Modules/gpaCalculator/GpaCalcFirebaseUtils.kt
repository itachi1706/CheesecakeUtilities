package com.itachi1706.cheesecakeutilities.Modules.gpaCalculator

import android.content.Context
import android.widget.EditText
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.objects.GpaScoring
import com.itachi1706.cheesecakeutilities.Util.ColorUtils
import com.itachi1706.cheesecakeutilities.Util.FirebaseUtils
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Kenneth on 24/6/2019.
 * for com.itachi1706.cheesecakeutilities.Modules.gpaCalculator in CheesecakeUtilities
 */
object GpaCalcFirebaseUtils: FirebaseUtils() {
    const val FB_REC_SCORING = "scoring"
    const val FB_REC_USER = "users"
    const val FB_REC_SEMESTER = "semester"
    const val FB_REC_MODULE = "modules"

    const val RECORDS_VER = 0

    @JvmStatic
    @JvmOverloads
    fun getGpaDatabase(database: FirebaseDatabase? = null): DatabaseReference {
        return if (database == null) Companion.getDatabaseReference("gpacalc")
            else Companion.getDatabaseReference("gpacalc", database)
    }

    fun getGpaDatabaseUser(userId: String) : DatabaseReference {
        return getGpaDatabase().child(FB_REC_USER).child(userId)
    }

    fun getCalendarWithNoTime(year: Int, month: Int, dayOfMonth: Int): Calendar {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month)
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal
    }

    fun updateDateTimeViews(startView: EditText, endView: EditText, startTime: Long, endTime: Long) {
        val calender = Calendar.getInstance()
        val dateFormat = DATE_FORMAT
        calender.timeInMillis = startTime
        startView.setText(dateFormat.format(calender.time))
        if (endTime != (-1).toLong()) {
            calender.timeInMillis = endTime
            endView.setText(dateFormat.format(calender.time))
        } else endView.setText("")
    }

    fun getGpaColor(grade: String, scoreTier: GpaScoring?, context: Context?): Int {
        if (grade == "Unknown" || scoreTier == null || context == null || scoreTier.finalGradeColor == null) return -999
        val g = grade.toDoubleOrNull() ?: return -999
        for (col in scoreTier.finalGradeColor) {
            if (g > col.from && g <= col.to) return ColorUtils.getColorFromVariable(context, colorFromString(col.color))
        }
        return -999
    }

    private fun colorFromString(gradeString: String): Int {
        return when (gradeString.toLowerCase()) {
            "green" -> ColorUtils.DARK_GREEN
            "yellow" -> ColorUtils.DARK_YELLOW
            "orange" -> ColorUtils.ORANGE
            "red" -> ColorUtils.RED
            else -> -999
        }
    }

    val DATE_FORMAT = SimpleDateFormat("dd MMM yyyy", Locale.US)
}