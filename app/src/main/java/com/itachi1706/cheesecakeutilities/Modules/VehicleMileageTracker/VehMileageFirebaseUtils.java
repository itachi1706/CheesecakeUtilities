package com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.itachi1706.cheesecakeutilities.Util.FirebaseUtils;

/**
 * Created by Kenneth on 28/7/2017.
 * for com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker in CheesecakeUtilities
 */

public class VehMileageFirebaseUtils extends FirebaseUtils {

    // Constants
    public static final String FB_REC_USER = "users", FB_REC_STATS = "statistics", FB_REC_RECORDS = "records",
            MILEAGE_DEC = "veh_mileage_decimal";

    private static FirebaseDatabase firebaseDatabase;

    static final int RECORDS_VERSION = 4;

    public DatabaseReference getVehicleMileageDatabase(FirebaseDatabase database) {
        return Companion.getDatabaseReference("vehmileage");
    }
}
