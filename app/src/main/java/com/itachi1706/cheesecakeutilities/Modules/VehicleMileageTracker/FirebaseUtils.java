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

    private static FirebaseDatabase firebaseDatabase;

    static final int RECORDS_VERSION = 1;

    public static FirebaseDatabase getFirebaseDatabase() {
        if (firebaseDatabase == null) {
            firebaseDatabase = FirebaseDatabase.getInstance();
            firebaseDatabase.setPersistenceEnabled(true);
        }
        return firebaseDatabase;
    }

    public static String formatTime(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy HH:mm", Locale.US);
        Date dt = new Date();
        dt.setTime(time);
        return sdf.format(dt);
    }

    public static String formatTimeDuration(long start, long end) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy HHmm", Locale.US);
        Date dt = new Date();
        dt.setTime(start);
        String timeString = sdf.format(dt);
        dt.setTime(end);
        timeString += " - " + sdf.format(dt);
        return timeString;
    }
}
