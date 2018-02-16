package com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.Objects

/**
 * Created by Kenneth on 28/7/2017.
 * for com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.Objects in CheesecakeUtilities
 */

class Record {

    var datetimeFrom: Long? = null
    var dateTimeTo: Long? = null
    var timezone: Long? = null
    var mileageFrom: Double? = null
    var mileageTo: Double? = null
    var destination: String? = null
    var purpose: String? = null
    var vehicleNumber: String? = null
    var vehicleId: String? = null
    var vehicleClass: String? = null
    var trainingMileage: Boolean = false

    // Calculated fields
    var totalMileage: Double? = null
        private set
    var totalTimeInMs: Long? = null
        private set

    // Version Number
    var version = -1

    fun updateMileage(): Boolean? {
        this.totalMileage = this.mileageTo!! - this.mileageFrom!!
        return true
    }

    fun updateTotalTime(): Boolean? {
        this.totalTimeInMs = this.dateTimeTo!! - this.datetimeFrom!!
        this.totalTimeInMs = this.dateTimeTo!! - this.datetimeFrom!!
        return true
    }
}
