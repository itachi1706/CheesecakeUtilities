package com.itachi1706.cheesecakeutilities.util

import com.crashlytics.android.Crashlytics
import com.itachi1706.cheesecakeutilities.BuildConfig
import com.itachi1706.helperlib.helpers.LogHelper
import com.itachi1706.helperlib.interfaces.LogHandler

/**
 * Created by Kenneth on 15/1/2020.
 * for com.itachi1706.cheesecakeutilities.util in CheesecakeUtilities
 */
object LogInit {
    @JvmStatic
    fun initLogger() {
        LogHelper.addExternalLog(object: LogHandler { override fun handleExtraLogging(logLevel: Int, tag: String, message: String) { if (!BuildConfig.DEBUG) Crashlytics.log(logLevel, tag, message) } })
    }
}