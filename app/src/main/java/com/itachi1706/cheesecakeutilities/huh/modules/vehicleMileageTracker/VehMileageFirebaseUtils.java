package com.itachi1706.cheesecakeutilities.huh.modules.vehicleMileageTracker;

import android.content.SharedPreferences;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.itachi1706.cheesecakeutilities.huh.util.FirebaseUtils;

/**
 * Created by Kenneth on 28/7/2017.
 * for com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker in CheesecakeUtilities
 */

public class VehMileageFirebaseUtils extends FirebaseUtils {

    // Constants
    public static final String FB_REC_USER = "users", FB_REC_STATS = "statistics", FB_REC_RECORDS = "records",
            MILEAGE_DEC = "veh_mileage_decimal";

    public static final String FB_UID = "firebase_uid";

    static final int RECORDS_VERSION = 4;

    public static DatabaseReference getVehicleMileageDatabase(FirebaseDatabase database) {
        return Companion.getDatabaseReference("vehmileage", database);
    }

    public static DatabaseReference getVehicleMileageDatabase() {
        return Companion.getDatabaseReference("vehmileage");
    }

    public static String getFirebaseUIDFromSharedPref(SharedPreferences sp) {
        return sp.getString(VehMileageFirebaseUtils.FB_UID, "nien");
    }
}
