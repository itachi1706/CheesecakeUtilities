package com.itachi1706.cheesecakeutilities.huh.util

import android.util.Log
import com.crashlytics.android.Crashlytics
import com.itachi1706.cheesecakeutilities.BuildConfig

/**
 * Created by Kenneth on 15/5/2019.
 * for com.itachi1706.cheesecakeutilities.Util in CheesecakeUtilities
 */
object LogHelper {

    private fun crashlyticsLogging(logLevel: Int, tag:String, message: String) {
        if (!BuildConfig.DEBUG) Crashlytics.log(logLevel, tag, message)
    }

    private fun log(logLevel: Int, tag: String, message: String) {
        crashlyticsLogging(logLevel, tag, message)
        Log.println(logLevel, tag, message)
    }

    @JvmStatic
    fun d(tag: String, message: String) {
        log(Log.DEBUG, tag, message)
    }

    @JvmStatic
    fun d(tag: String, message: String, tr: Throwable) {
        crashlyticsLogging(Log.DEBUG, tag, message)
        crashlyticsLogging(Log.DEBUG, tag, "Exception thrown: " + tr.message)
        Log.d(tag, message, tr)
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
    fun e(tag: String, message: String, tr: Throwable) {
        crashlyticsLogging(Log.ERROR, tag, message)
        crashlyticsLogging(Log.ERROR, tag, "Exception thrown: " + tr.message)
        Log.e(tag, message, tr)
    }

    @JvmStatic
    fun w(tag: String, tr: Throwable) {
        crashlyticsLogging(Log.WARN, tag, "Exception thrown: " + tr.message)
        Log.w(tag, tr)
    }

    @JvmStatic
    fun w(tag: String, message: String, tr: Throwable) {
        crashlyticsLogging(Log.WARN, tag, message)
        crashlyticsLogging(Log.WARN, tag, "Exception thrown: " + tr.message)
        Log.w(tag, message, tr)
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