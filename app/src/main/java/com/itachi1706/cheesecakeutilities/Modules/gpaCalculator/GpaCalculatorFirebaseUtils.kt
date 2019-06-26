package com.itachi1706.cheesecakeutilities.Modules.gpaCalculator

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.itachi1706.cheesecakeutilities.Util.FirebaseUtils

/**
 * Created by Kenneth on 24/6/2019.
 * for com.itachi1706.cheesecakeutilities.Modules.gpaCalculator in CheesecakeUtilities
 */
object GpaCalculatorFirebaseUtils: FirebaseUtils() {
    const val FB_REC_SCORING = "scoring"
    const val FB_REC_USER = "users"

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
}