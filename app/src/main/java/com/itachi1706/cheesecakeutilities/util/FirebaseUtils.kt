package com.itachi1706.cheesecakeutilities.util

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToLong

/**
 * Created by Kenneth on 3/6/2019.
 * for com.itachi1706.cheesecakeutilities.Util in CheesecakeUtilities
 */
abstract class FirebaseUtils {

    companion object {

        private var firebaseDatabase: FirebaseDatabase? = null

        fun removeListener(listener: ValueEventListener) {
            getFirebaseDatabase().reference.removeEventListener(listener)
        }

        fun getFirebaseDatabase(): FirebaseDatabase {
            if (firebaseDatabase == null) {
                firebaseDatabase = FirebaseDatabase.getInstance()
                firebaseDatabase!!.setPersistenceEnabled(true)
            }
            return firebaseDatabase!!
        }

        fun getDatabaseReference(tag: String): DatabaseReference {
            val database = getFirebaseDatabase()
            return getDatabaseReference(tag, database)
        }

        fun getDatabaseReference(tag: String, database: FirebaseDatabase): DatabaseReference {
            return database.reference.child(tag)
        }

        fun formatTime(time: Long): String {
            return formatTime(time, "dd MMMM yyyy HH:mm")
        }

        fun formatTime(time: Long, format: String): String {
            val sdf = SimpleDateFormat(format, Locale.US)
            val dt = Date()
            dt.time = time
            return sdf.format(dt)
        }

        fun formatTimeDuration(start: Long, end: Long): String {
            val sdf = SimpleDateFormat("dd/MM/yy HHmm", Locale.US)
            val dt = Date()
            dt.time = start
            var timeString = sdf.format(dt)
            sdf.applyPattern("dd/MM/yy HHmm zzz")
            dt.time = end
            timeString += " - " + sdf.format(dt)
            return timeString
        }

        fun parseData(d: Double, decimal: Boolean): String {
            return if (decimal) String.format(Locale.getDefault(), "%.1f", d) else
                String.format(Locale.getDefault(), "%d", d.roundToLong())
        }
    }
}
