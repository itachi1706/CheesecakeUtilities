package com.itachi1706.cheesecakeutilities.Modules.FanfictionCompactor.Objects

/**
 * Created by Kenneth on 5/6/2016.
 * for com.itachi1706.cheesecakeutilities.Modules.FanfictionCompactor.Objects in CheesecakeUtilities
 */
class FanficNotificationObject {
    var message: String
    var title: String
    var altmessage: String? = null
    var isCancellable: Boolean = false
    var isIndeterminate: Boolean = false
    var progress: Int = 0
    var max: Int = 0

    val notificationMessage: String
        get() = if (altmessage == null) message else altmessage!!

    constructor(message: String, title: String, cancellable: Boolean, progress: Int, max: Int, indeterminate: Boolean) {
        this.message = message
        this.title = title
        this.isCancellable = cancellable
        this.progress = progress
        this.max = max
        this.isIndeterminate = indeterminate
        this.altmessage = null
    }

    constructor(message: String, title: String, cancellable: Boolean, progress: Int, max: Int, indeterminate: Boolean, altmessage: String) {
        this.message = message
        this.title = title
        this.isCancellable = cancellable
        this.progress = progress
        this.max = max
        this.isIndeterminate = indeterminate
        this.altmessage = altmessage
    }
}
