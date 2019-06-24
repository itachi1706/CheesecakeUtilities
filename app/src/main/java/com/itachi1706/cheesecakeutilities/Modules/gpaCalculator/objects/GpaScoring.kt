package com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.objects

/**
 * Created by Kenneth on 24/6/2019.
 * for com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.objects in CheesecakeUtilities
 */
data class GpaScoring(val name: String = "Some name", val description: String = "No description available", val passtier: Array<GpaTier>? = null, val gradetier: Array<GpaTier>) {

    data class GpaTier(val name: String = "Grade Tier", val desc: String = "No description", val value: Double)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GpaScoring

        if (name != other.name) return false
        if (description != other.description) return false
        if (passtier != null) {
            if (other.passtier == null) return false
            if (!passtier.contentEquals(other.passtier)) return false
        } else if (other.passtier != null) return false
        if (!gradetier.contentEquals(other.gradetier)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + (passtier?.contentHashCode() ?: 0)
        result = 31 * result + gradetier.contentHashCode()
        return result
    }
}