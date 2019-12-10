package com.itachi1706.cheesecakeutilities.huh.redirectapp

/**
 * Created by Kenneth on 11/8/2019.
 * for com.itachi1706.cheesecakeutilities.huh.redirectapp.fanfictionReader in CheesecakeUtilities
 */
data class RedirectAppUrlObject(val msg: Message = Message(), val error: Int = 0) {
    data class Message(val id: Int = 0, val url: String = "", val appName: String = "", val packageName: String = "",
                       val latestVersion: String = "", val latestVersionCode: Int = 0, val dateCreated: Long = System.currentTimeMillis())
}