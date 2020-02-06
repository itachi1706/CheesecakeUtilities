package com.itachi1706.cheesecakeutilities.modules.toggle

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.PorterDuffXfermode
import android.os.Build
import com.itachi1706.helperlib.helpers.LogHelper

/**
 * Created by Kenneth on 4/2/2020.
 * for com.itachi1706.cheesecakeutilities.modules.toggle in CheesecakeUtilities
 */
object ToggleHelper {

    const val PRIVATE_DNS_SETTING = "private_dns_mode" // Settings.Global.PRIVATE_DNS_MODE
    const val FORCE_90HZ_SETTING = "min_refresh_rate" // Settings.System.MIN_REFRESH_RATE (PIXEL)

    // Match companion app
    val COMPONENT_NAME = ComponentName("com.itachi1706.cheesecakeutilitiessettingscompanion", "com.itachi1706.cheesecakeutilitiessettingscompanion.SettingsChangeReceiver")
    const val ACTION_CHECK = "com.itachi1706.cheesecakeutilitiessettingscompanion.CHECK"
    const val ACTION_CHANGE = "com.itachi1706.cheesecakeutilitiessettingscompanion.CHANGE_SETTING"
    const val ACTION_REPLY = "com.itachi1706.cheesecakeutilitiessettingscompanion.REPLY" // Other app must listen to this
    const val DATA_RESULT = "result"
    const val DATA_EXTRA_DATA = "extradata"
    const val DATA_SETTING_NAME = "settingname"
    const val DATA_SETTING_TYPE = "settingtype" // 0 - Global, 1 - Secure, 2 - System
    const val DATA_SETTING_VAL = "settingval"
    const val DATA_CONST_GLOBAL = 0
    const val DATA_CONST_SECURE = 1
    const val DATA_CONST_SYSTEM = 2

    @JvmStatic
    fun checkWriteSecurePermission(context: Context): Boolean {
        val requiredPermission = Manifest.permission.WRITE_SECURE_SETTINGS
        return when (context.checkCallingOrSelfPermission(requiredPermission)) {
            PackageManager.PERMISSION_GRANTED -> true
            PackageManager.PERMISSION_DENIED -> false
            else -> false
        }
    }

    private const val PIXEL_TAG = "PixelChk"

    @JvmStatic
    fun checkGooglePhone(): Boolean {
        LogHelper.i(PIXEL_TAG, "Pixel Phone Check. Manufacturer: ${Build.MANUFACTURER}, Brand: ${Build.BRAND}")
        return Build.MANUFACTURER == "Google" && Build.BRAND == "google"
    }

    @JvmStatic
    fun checkSupportedPixelPhone(): Boolean {
        LogHelper.d(PIXEL_TAG, "Supported Devices: Pixel 4 (flame), Pixel 4XL (coral)")
        LogHelper.i(PIXEL_TAG, "Supported Pixel Phone Check. Board: ${Build.BOARD}, Device: ${Build.DEVICE}, Hardware: ${Build.HARDWARE}, Product: ${Build.PRODUCT}")
        return checkProdMatch("coral") || checkProdMatch("flame")
    }

    private fun checkProdMatch(name: String): Boolean {
        return Build.BOARD == Build.DEVICE && Build.DEVICE == Build.HARDWARE && Build.HARDWARE == Build.PRODUCT && Build.PRODUCT == name
    }

    @JvmStatic
    fun checkForce90HzSupport(): Boolean {
        return checkGooglePhone() && checkSupportedPixelPhone()
    }
}