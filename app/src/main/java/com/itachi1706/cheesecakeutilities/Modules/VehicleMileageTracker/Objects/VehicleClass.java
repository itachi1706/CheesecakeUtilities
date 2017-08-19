package com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.Objects;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kenneth on 28/7/2017.
 * for com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.Objects in CheesecakeUtilities
 */

public class VehicleClass {

    private static List<VehClass> vehClassList;

    public static VehClass getClassType(String id) {
        if (vehClassList == null) init();
        for (VehClass v : vehClassList) {
            if (v.getId().equalsIgnoreCase(id)) return v;
        }
        return null;
    }

    public static VehClass getClassTypeWithName(String className) {
        if (vehClassList == null) init();
        for (VehClass v : vehClassList) {
            if (v.getFullname().equalsIgnoreCase(className)) return v;
        }
        return null;
    }

    public static List<VehClass> getVehClassList() {
        if (vehClassList == null) init();
        return vehClassList;
    }

    private static void init() {
        vehClassList = new ArrayList<>();
        vehClassList.add(new VehClass("class2", "Class 2A/2B/2"));
        vehClassList.add(new VehClass("class3", "Class 3/3A"));
        vehClassList.add(new VehClass("class4", "Class 4"));
        vehClassList.add(new VehClass("class4s", "Class 4S (Cargo Trailer)"));
        vehClassList.add(new VehClass("class5", "Class 5"));
        vehClassList.add(new VehClass("class4a", "Class 4A (Public Buses)"));
        vehClassList.add(new VehClass("class1", "Class 1 (Disabled)"));
        vehClassList.add(new VehClass("class3c", "Class 3C/3CA"));
    }

    public static class VehClass {
        private String id, fullname;

        VehClass(String id, String fullname) {
            this.id = id;
            this.fullname = fullname;
        }

        @NonNull
        public String getId() {
            return id;
        }

        public String getFullname() {
            return fullname;
        }
    }

}
