package com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.model

/**
 * Created by Kenneth on 23/1/2019.
 * for com.itachi1706.cheesecakeutilities.Modules.MSLIntegration.model in CheesecakeUtilities
 */
class ExportFile {
    /*
    Unimportable includes
    - Google OAuth Token
    - Toggle State for Syncing Task and Exams
     */
    var cache: String? = null // /cache/msl...
    var history: String? = null // msl-metric-history
    var notificationDismiss: Boolean = false // msl_notification_dismiss
    var calendarId: String? = null // msl-cal-task-id
    var accessToken: String? = null // msl_access_token
}