package com.itachi1706.cheesecakeutilities.modules.toggle

import android.Manifest
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.itachi1706.cheesecakeutilities.BaseModuleActivity
import com.itachi1706.cheesecakeutilities.R
import com.itachi1706.cheesecakeutilities.modules.toggle.ToggleHelper.FORCE_90HZ_SETTING
import com.itachi1706.cheesecakeutilities.modules.toggle.ToggleHelper.PRIVATE_DNS_SETTING
import com.itachi1706.cheesecakeutilities.modules.toggle.services.QSForce90HzTileService
import com.itachi1706.cheesecakeutilities.modules.toggle.services.QSPrivateDNSTileService
import com.itachi1706.cheesecakeutilities.redirectapp.InstallAppTask
import com.itachi1706.helperlib.helpers.LogHelper
import com.itachi1706.helperlib.helpers.PrefHelper
import com.itachi1706.helperlib.utils.NotifyUserUtil
import kotlinx.android.synthetic.main.activity_toggle.*

class ToggleActivity : BaseModuleActivity() {

    override val helpDescription: String
        get() = "Basic System Toggles that is also available on the tiles menu as well"

    private lateinit var permissionStr: String
    private var privateDnsUpdater: Boolean = false
    private var force90HzUpdater: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_toggle)

        permissionStr = "adb shell pm grant $packageName ${Manifest.permission.WRITE_SECURE_SETTINGS}"
        toggle_how_private_dns.setOnClickListener {
            AlertDialog.Builder(this).setTitle("How to grant permission")
                    .setMessage("To toggle Private DNS, we require the WRITE_SECURE_SETTINGS permission. Hence we need to execute the following command to be ran on ADB\n\n$permissionStr")
                    .setPositiveButton(android.R.string.ok, null).setNegativeButton(R.string.share) { _,_ ->
                        val shareIntent = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, permissionStr) }
                        startActivity(Intent.createChooser(shareIntent, "Share Command"))
                    }.show()
        }

        // Check if device has private dns (Android 9 and above)
        toggle_private_dns.visibility = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) View.GONE else View.VISIBLE

        // If there is nothing in the page, throw error that its not available for user
        if (!hasStuffOnScreen()) {
            AlertDialog.Builder(this).setTitle("Incompatible Android Version")
                    .setMessage("System Toggles are only available for devices on Android 9.0 Pie and above and is currently incompatible compatible with your device")
                    .setCancelable(false).setPositiveButton(R.string.dialog_action_positive_close) { _, _ -> finish() }.show()
            return // Do not process anything else
        }

        val sp = PrefHelper.getDefaultSharedPreferences(this)

        toggle_spinner_private_dns.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) { }
            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                if (privateDnsUpdater) { privateDnsUpdater = false; return }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) checkPrivateDns()
                sp.edit().putInt(PRIVATE_DNS_SETTING, position).apply()
            }
        }
        privateDnsUpdater = true
        toggle_spinner_private_dns.setSelection(sp.getInt(PRIVATE_DNS_SETTING, 0))
        toggle_switch_private_dns.setOnCheckedChangeListener { buttonView, isChecked ->
            if (privateDnsUpdater) { privateDnsUpdater = false; return@setOnCheckedChangeListener }
            showEnableToast("Private DNS", isChecked, buttonView.context)
            val onState = resources.getStringArray(R.array.private_dns_entries_option)[toggle_spinner_private_dns.selectedItemPosition]
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) Settings.Global.putString(contentResolver, PRIVATE_DNS_SETTING, if (isChecked) onState else "off")
            LogHelper.i(TAG, "Toggled Private DNS to ${if (isChecked) onState else "off"}")
        }
        toggle_statusbar_private_dns.isChecked = checkPrivDNSTileEnabled()
        toggle_statusbar_private_dns.setOnCheckedChangeListener { _, isChecked -> packageManager.setComponentEnabledSetting(ComponentName(this, QSPrivateDNSTileService::class.java),
                if (isChecked) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP) }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) checkPixelForce90Hz() else disablePixelMode("Android Version must be Marshmallow and above")
    }

    override fun onResume() {
        super.onResume()
        if (!hasStuffOnScreen()) return // Do not continue
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) checkPrivateDns()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val filter = IntentFilter(ToggleHelper.ACTION_REPLY)
            if (pixelReceiver == null) {
                pixelReceiver = PixelReceiver(object: PixelReceiver.Action {
                    override fun doAction(message: String, success: Boolean) {
                        Log.i(TAG, "Received Success State $success with message: $message")
                        if (message.contains("Ping Check")) {
                            // Check
                            val version = message.split("|")[1].trim()
                            isAllowedPixel(success, version)
                            return
                        }
                    }
                })
                registerReceiver(pixelReceiver, filter)
            }
            refreshPixelForce90Hz()
        }
    }

    private fun checkPrivDNSTileEnabled(): Boolean { return packageManager.getComponentEnabledSetting(ComponentName(this, QSPrivateDNSTileService::class.java)) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED }
    private fun checkForce90HzTileEnabled(): Boolean { return packageManager.getComponentEnabledSetting(ComponentName(this, QSForce90HzTileService::class.java)) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED }
    private fun hasStuffOnScreen(): Boolean { return toggle_private_dns.visibility != View.GONE }
    private fun showEnableToast(type: String, isChecked: Boolean, context: Context) { Toast.makeText(context, "${if (isChecked) "Enabling" else "Disabling"} $type", Toast.LENGTH_LONG).show() }
    private fun setTextColor(view: TextView, isAllowed: Boolean) { view.setTextColor(ContextCompat.getColor(this, if (isAllowed) if (PrefHelper.isNightModeEnabled(this)) R.color.green else R.color.dark_green else R.color.red)) }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun checkPrivateDns() {
        // Check permission granted
        val isAllowed = ToggleHelper.checkWriteSecurePermission(this)
        setTextColor(toggle_status_private_dns, isAllowed)
        toggle_status_private_dns.text = if (isAllowed) "Granted" else "Not Granted"
        toggle_spinner_private_dns.isEnabled = isAllowed
        toggle_statusbar_private_dns.isEnabled = isAllowed
        toggle_switch_private_dns.isEnabled = isAllowed

        // Check Setting
        val option = Settings.Global.getString(contentResolver, PRIVATE_DNS_SETTING)
        LogHelper.d(TAG, "Current Private DNS: $option")

        // Get selected option
        val pos = toggle_spinner_private_dns.selectedItemPosition
        val selection = resources.getStringArray(R.array.private_dns_entries_option)[pos]

        privateDnsUpdater = true
        toggle_switch_private_dns.isChecked = option == selection // check if match
    }

    /**
     * Only supported for Pixel 4 (Flame) and Pixel 4 XL (Flame)
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkPixelForce90Hz() {
        // Only for Pixel. If non Pixel series device do not continue
        LogHelper.i(TAG, "Checking device to see if it is a Google Pixel device")
        if (!ToggleHelper.checkGooglePhone()) { disablePixelMode("Google Phone Check failed. Disabling Pixel Exclusive Features"); return }
        if (!ToggleHelper.checkSupportedPixelPhone()) { disablePixelMode("Unsupported Pixel device. Disabling unsupported features. Current Device: ${Build.MODEL}"); return }
        LogHelper.i(TAG, "Found supported Pixel Device (${Build.MODEL}). Enabling feature")

        toggle_grant_force_90hz.setOnClickListener {
            NotifyUserUtil.createShortToast(this, "Installing Companion App to toggle Force 90Hz. Please open app at least once when its done")
            InstallAppTask(this, "com.itachi1706.cheesecakeutilitiessettingscompanion").execute()
        }

        toggle_switch_force_90hz.setOnCheckedChangeListener { buttonView, isChecked ->
            if (force90HzUpdater) { force90HzUpdater = false; return@setOnCheckedChangeListener }
            showEnableToast("Force 90Hz", isChecked, buttonView.context)
            val onState = 90.0f
            val toggleIntent = Intent().apply {
                action = ToggleHelper.ACTION_CHANGE
                putExtra(ToggleHelper.DATA_SETTING_TYPE, ToggleHelper.DATA_CONST_SYSTEM)
                putExtra(ToggleHelper.DATA_SETTING_NAME, FORCE_90HZ_SETTING)
                putExtra(ToggleHelper.DATA_SETTING_VAL, if (isChecked) "90.0" else "0.0")
                addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
                component = ToggleHelper.COMPONENT_NAME
            }
            sendBroadcast(toggleIntent)
            LogHelper.i(TAG, "Attempting to toggle Force 90Hz (min refresh rate) to ${if (isChecked) onState.toString() else "0"}")
        }
        toggle_statusbar_force_90hz.isChecked = checkForce90HzTileEnabled()
        toggle_statusbar_force_90hz.setOnCheckedChangeListener { _, isChecked -> packageManager.setComponentEnabledSetting(ComponentName(this, QSForce90HzTileService::class.java),
                if (isChecked) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP) }

        refreshPixelForce90Hz()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun refreshPixelForce90Hz() {
        if (toggle_force_90hz.visibility == View.GONE) return // Not doing anything
        // Check Settings
        val option = Settings.System.getFloat(contentResolver, FORCE_90HZ_SETTING)
        LogHelper.d(TAG, "Current Min Refresh Rate: $option")

        if (toggle_switch_force_90hz.isChecked != (option == 90.0f)) force90HzUpdater = true
        toggle_switch_force_90hz.isChecked = option == 90.0f // 90Hz display

        // Check if we can toggle (App Instaalled anot)
        val checkAppInstalled = Intent().apply {
            action = ToggleHelper.ACTION_CHECK
            addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
            component = ToggleHelper.COMPONENT_NAME
        }
        sendBroadcast(checkAppInstalled)
    }

    private fun isAllowedPixel(isAllowed: Boolean, version: String) {
        setTextColor(toggle_status_force_90hz, isAllowed)
        toggle_status_force_90hz.text = if (isAllowed) "Installed ($version)" else "Not Installed"
        toggle_switch_force_90hz.isEnabled = isAllowed
        toggle_statusbar_force_90hz.isEnabled = isAllowed
        toggle_grant_force_90hz.text = if (isAllowed) "Update Companion App" else "Install Companion App"
    }

    private var pixelReceiver: BroadcastReceiver? = null

    override fun onPause() {
        super.onPause()
        if (pixelReceiver != null) {
            unregisterReceiver(pixelReceiver)
            pixelReceiver = null
        }
    }

    private fun disablePixelMode(msg: String) {
        LogHelper.w(TAG, msg)
        toggle_force_90hz.visibility = View.GONE
    }

    companion object { private const val TAG = "ToggleActivity" }
}
