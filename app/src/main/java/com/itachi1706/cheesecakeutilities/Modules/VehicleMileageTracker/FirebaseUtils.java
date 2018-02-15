package com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker;

import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Kenneth on 28/7/2017.
 * for com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker in CheesecakeUtilities
 */

public class FirebaseUtils {

    // Constants
    public static final String FB_REC_USER = "users", FB_REC_STATS = "statistics", FB_REC_RECORDS = "records";

    private static FirebaseDatabase firebaseDatabase;

    static final int RECORDS_VERSION = 3;

    public static FirebaseDatabase getFirebaseDatabase() {
        if (firebaseDatabase == null) {
            firebaseDatabase = FirebaseDatabase.getInstance();
            firebaseDatabase.setPersistenceEnabled(true);
        }
        return firebaseDatabase;
    }

    public static String formatTime(long time) {
        return formatTime(time, "dd MMMM yyyy HH:mm");
    }

    public static String formatTime(long time, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
        Date dt = new Date();
        dt.setTime(time);
        return sdf.format(dt);
    }

    public static String formatTimeDuration(long start, long end) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy HHmm", Locale.US);
        Date dt = new Date();
        dt.setTime(start);
        String timeString = sdf.format(dt);
        sdf.applyPattern("dd/MM/yy HHmm zzz");
        dt.setTime(end);
        timeString += " - " + sdf.format(dt);
        return timeString;
    }
}
