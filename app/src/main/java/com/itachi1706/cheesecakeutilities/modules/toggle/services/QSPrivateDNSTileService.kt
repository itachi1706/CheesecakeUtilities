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
import com.itachi1706.cheesecakeutilities.modules.toggle.ToggleHelper.PRIVATE_DNS_SETTING
import com.itachi1706.helperlib.helpers.LogHelper
import com.itachi1706.helperlib.helpers.PrefHelper

@RequiresApi(Build.VERSION_CODES.N)
class QSPrivateDNSTileService : TileService() {

    private lateinit var appContext: Context

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        appContext = baseContext
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onStartListening() {
        super.onStartListening()

        if (!ToggleHelper.checkWriteSecurePermission(this)) { noGo(); return }
        updateTileState(if (checkSettingEnabled()) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE)
    }

    override fun onClick() {
        super.onClick()
        if (!ToggleHelper.checkWriteSecurePermission(this)) { noGo(); return }
        if (isLocked) { LogHelper.w(TAG, "Not executing. Secure Lockscreen"); return }
        if (checkSettingEnabled()) {
            // Turn off
            Settings.Global.putString(contentResolver, PRIVATE_DNS_SETTING, "off")
            LogHelper.i(TAG, "Disabled Private DNS")
            updateTileState(Tile.STATE_INACTIVE)
        } else {
            // Turn on
            Settings.Global.putString(contentResolver, PRIVATE_DNS_SETTING, getActivateString())
            LogHelper.i(TAG, "Enabled Private DNS")
            updateTileState(Tile.STATE_ACTIVE)
        }
    }

    private fun checkSettingEnabled(): Boolean { return Settings.Global.getString(contentResolver, PRIVATE_DNS_SETTING) == getActivateString() }

    private fun getActivateString(): String {
        val sp = PrefHelper.getDefaultSharedPreferences(this)
        val pos = sp.getInt(PRIVATE_DNS_SETTING, 0)
        return resources.getStringArray(R.array.private_dns_entries_option)[pos]
    }

    private fun updateTileState(newState: Int) { qsTile.apply { state = newState; updateTile() } }

    private fun noGo() {
        updateTileState(Tile.STATE_UNAVAILABLE)
        LogHelper.w(TAG, "Unavailable. WRITE_SECURE_SETTINGS permission has not been granted")
    }

    companion object {
        private const val TAG = "QSPrivDNSTile"
    }
}
