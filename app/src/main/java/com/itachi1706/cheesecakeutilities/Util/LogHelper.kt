package com.itachi1706.cheesecakeutilities.Util

import android.util.Log
import com.crashlytics.android.Crashlytics

/**
 * Created by Kenneth on 15/5/2019.
 * for com.itachi1706.cheesecakeutilities.Util in CheesecakeUtilities
 */
object LogHelper {

    private fun log(logLevel: Int, tag: String, message: String) {
        Crashlytics.log(logLevel, tag, message)
        Log.println(logLevel, tag, message)
    }

    @JvmStatic
    fun d(tag: String, message: String) {
        log(Log.DEBUG, tag, message)
    }

    @JvmStatic
    fun i(tag: String, message: String) {
        log(Log.INFO, tag, message)
    }

    @JvmStatic
    fun v(tag: String, message: String) {
        log(Log.VERBOSE, tag, message)
    }

    @JvmStatic
    fun e(tag: String, message: String) {
        log(Log.ERROR, tag, message)
    }

    @JvmStatic
    fun w(tag: String, message: String) {
        log(Log.WARN, tag, message)
    }

    @JvmStatic
    fun wtf(tag: String, message: String) {
        Log.wtf(tag, message)
    }
}