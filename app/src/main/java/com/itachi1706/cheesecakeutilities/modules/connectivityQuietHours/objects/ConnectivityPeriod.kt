package com.itachi1706.cheesecakeutilities.modules.connectivityQuietHours.objects

/**
 * Created by Kenneth on 28/4/2017.
 * for com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.Objects in CheesecakeUtilities
 */

class ConnectivityPeriod {

    var startHr: Int = 0
    var startMin: Int = 0
    var endHr: Int = 0
    var endMin: Int = 0

    constructor(startHr: Int, startMin: Int, endHr: Int, endMin: Int) {
        this.startHr = startHr
        this.startMin = startMin
        this.endHr = endHr
        this.endMin = endMin
    }

    constructor(serializedObject: String) {
        val tmp = serializedObject.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val tmpSize = tmp.size
        this.endHr = 0
        this.endMin = 0
        this.startHr = 0
        this.startMin = 0
        if (tmpSize >= 4) this.endMin = Integer.parseInt(tmp[3])
        if (tmpSize >= 3) this.endHr = Integer.parseInt(tmp[2])
        if (tmpSize >= 2) this.startMin = Integer.parseInt(tmp[1])
        if (tmpSize >= 1) this.startHr = Integer.parseInt(tmp[0])
    }

    constructor(startHr: Int, startMin: Int) {
        this.startHr = startHr
        this.startMin = startMin
        this.endHr = 0
        this.endMin = 0
    }

    override fun toString(): String {
        return startHr.toString() + ":" + startMin + " - " + endHr + ":" + endMin
    }

    fun serialize(): String {
        return startHr.toString() + ":" + startMin + ":" + endHr + ":" + endMin
    }
}
