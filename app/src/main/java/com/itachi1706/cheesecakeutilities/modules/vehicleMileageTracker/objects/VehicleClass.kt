package com.itachi1706.cheesecakeutilities.modules.vehicleMileageTracker.objects

import java.util.ArrayList

/**
 * Created by Kenneth on 28/7/2017.
 * for com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.Objects in CheesecakeUtilities
 */

object VehicleClass {

    private var vehClassList: MutableList<VehClass>? = null

    fun getClassType(id: String): VehClass? {
        if (vehClassList == null) init()
        for (v in vehClassList!!) {
            if (v.id.equals(id, ignoreCase = true)) return v
        }
        return null
    }

    fun getClassTypeWithName(className: String): VehClass? {
        if (vehClassList == null) init()
        for (v in vehClassList!!) {
            if (v.fullname.equals(className, ignoreCase = true)) return v
        }
        return null
    }

    fun getVehClassList(): List<VehClass>? {
        if (vehClassList == null) init()
        return vehClassList
    }

    private fun init() {
        vehClassList = ArrayList()
        vehClassList!!.add(VehClass("class2", "Class 2A/2B/2"))
        vehClassList!!.add(VehClass("class3", "Class 3/3A"))
        vehClassList!!.add(VehClass("class4", "Class 4"))
        vehClassList!!.add(VehClass("class4s", "Class 4S (Cargo Trailer)"))
        vehClassList!!.add(VehClass("class5", "Class 5"))
        vehClassList!!.add(VehClass("class4a", "Class 4A (Public Buses)"))
        vehClassList!!.add(VehClass("class1", "Class 1 (Disabled)"))
        vehClassList!!.add(VehClass("class3c", "Class 3C/3CA"))
    }

    class VehClass internal constructor(val id: String, val fullname: String)

}
