package com.itachi1706.cheesecakeutilities.modules.vehicleMileageTracker.objects

/**
 * Created by Kenneth on 28/7/2017.
 * for com.itachi1706.cheesecakeutilities.modules.VehicleMileageTracker.Objects in CheesecakeUtilities
 */

data class Record (var datetimeFrom: Long = 0, var dateTimeTo: Long = 0, var timezone: Long? = null, var mileageFrom: Double = 0.0, var mileageTo: Double = 0.0,
        var destination: String? = null, var purpose: String? = null, var vehicleNumber: String? = null, var vehicleId: String? = null, var vehicleClass: String? = null,
        var trainingMileage: Boolean = false) {
    // Calculated fields
    var totalMileage: Double = 0.0
        private set
    var totalTimeInMs: Long = 0
        private set

    // Version Number
    var version = -1

    fun updateMileage(): Boolean? {
        this.totalMileage = this.mileageTo - this.mileageFrom
        return true
    }

    fun updateTotalTime(): Boolean? {
        this.totalTimeInMs = this.dateTimeTo - this.datetimeFrom
        this.totalTimeInMs = this.dateTimeTo - this.datetimeFrom
        return true
    }
}
