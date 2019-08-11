package com.itachi1706.cheesecakeutilities.Modules.FanfictionCompactor.Objects

/**
 * Created by Kenneth on 5/6/2016.
 * for com.itachi1706.cheesecakeutilities.Modules.FanfictionCompactor.Objects in CheesecakeUtilities
 */
@Deprecated("Migrated")
data class FanficNotificationObject @JvmOverloads constructor (
        var message: String, var title: String, var isCancellable: Boolean, var progress: Int, var max: Int, var isIndeterminate: Boolean, var altmessage: String? = null
) {
    val notificationMessage: String
        get() = if (altmessage == null) message else altmessage!!
}
