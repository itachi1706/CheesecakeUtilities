package com.itachi1706.cheesecakeutilities.modules.toggle.services

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.itachi1706.cheesecakeutilities.R
import com.itachi1706.cheesecakeutilities.modules.toggle.ToggleHelper
import com.itachi1706.cheesecakeutilities.modules.toggle.ToggleHelper.FORCE_90HZ_SETTING
import com.itachi1706.cheesecakeutilities.modules.toggle.ToggleHelper.PRIVATE_DNS_SETTING
import com.itachi1706.helperlib.helpers.LogHelper
import com.itachi1706.helperlib.helpers.PrefHelper

@RequiresApi(Build.VERSION_CODES.N)
class QSForce90HzTileService : TileService() {

    private lateinit var appContext: Context

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        appContext = baseContext
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onStartListening() {
        super.onStartListening()

        if (!ToggleHelper.checkForce90HzSupport()) { noGo(); return }
        updateTileState(if (checkSettingEnabled()) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE)
    }

    override fun onClick() {
        super.onClick()
        if (!ToggleHelper.checkForce90HzSupport()) { noGo(); return }
        if (isLocked) { LogHelper.w(TAG, "Not executing. Secure Lockscreen"); return }
        if (checkSettingEnabled()) {
            // Turn off
            sendBroadcast("0.0")
            LogHelper.i(TAG, "Disabling Force 90Hz")
            updateTileState(Tile.STATE_INACTIVE)
        } else {
            // Turn on
            sendBroadcast("90.0")
            LogHelper.i(TAG, "Enabling Force 90Hz")
            updateTileState(Tile.STATE_ACTIVE)
        }
    }

    private fun sendBroadcast(newValue: String) {
        val toggleIntent = Intent().apply {
            action = ToggleHelper.ACTION_CHANGE
            putExtra(ToggleHelper.DATA_SETTING_TYPE, ToggleHelper.DATA_CONST_SYSTEM)
            putExtra(ToggleHelper.DATA_SETTING_NAME, FORCE_90HZ_SETTING)
            putExtra(ToggleHelper.DATA_SETTING_VAL, newValue)
            addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
            component = ToggleHelper.COMPONENT_NAME
        }
        sendBroadcast(toggleIntent)
    }

    private fun checkSettingEnabled(): Boolean { return Settings.System.getFloat(contentResolver, FORCE_90HZ_SETTING) == 90.0f }

    private fun updateTileState(newState: Int) { qsTile.apply { state = newState; updateTile() } }

    private fun noGo() {
        updateTileState(Tile.STATE_UNAVAILABLE)
        LogHelper.w(TAG, "Unavailable. This tile is only available for Google Pixel devices with 90Hz screens (flame, coral)")
    }

    companion object { private const val TAG = "QSForce90HzTile" }
}
