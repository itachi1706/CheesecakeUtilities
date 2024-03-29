package com.itachi1706.cheesecakeutilities.util

import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.itachi1706.helperlib.helpers.LogHelper

/**
 * Created by Kenneth on 16/7/2019.
 * for com.itachi1706.cheesecakeutilities.modules.gpaCalculator in CheesecakeUtilities
 */
abstract class FirebaseValueEventListener(private val tag: String, private val methodName: String) :
    ValueEventListener {
    override fun onCancelled(p0: DatabaseError) {
        LogHelper.e(tag, "$methodName:cancelled", p0.toException())
    }
}