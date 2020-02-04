package com.itachi1706.cheesecakeutilities.modules.toggle

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager

/**
 * Created by Kenneth on 4/2/2020.
 * for com.itachi1706.cheesecakeutilities.modules.toggle in CheesecakeUtilities
 */
object ToggleHelper {

    const val PRIVATE_DNS_SETTING = "private_dns_mode" // Settings.Global.PRIVATE_DNS_MODE

    @JvmStatic
    fun checkWriteSecurePermission(context: Context): Boolean {
        val requiredPermission = Manifest.permission.WRITE_SECURE_SETTINGS
        return when (context.checkCallingOrSelfPermission(requiredPermission)) {
            PackageManager.PERMISSION_GRANTED -> true
            PackageManager.PERMISSION_DENIED -> false
            else -> false
        }
    }
}