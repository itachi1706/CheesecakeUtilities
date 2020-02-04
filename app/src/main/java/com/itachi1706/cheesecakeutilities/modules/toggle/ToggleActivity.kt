package com.itachi1706.cheesecakeutilities.modules.toggle

import android.Manifest
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.itachi1706.cheesecakeutilities.BaseModuleActivity
import com.itachi1706.cheesecakeutilities.R
import com.itachi1706.cheesecakeutilities.modules.toggle.ToggleHelper.PRIVATE_DNS_SETTING
import com.itachi1706.cheesecakeutilities.modules.toggle.services.QSPrivateDNSTileService
import com.itachi1706.helperlib.helpers.LogHelper
import com.itachi1706.helperlib.helpers.PrefHelper
import kotlinx.android.synthetic.main.activity_toggle.*

class ToggleActivity : BaseModuleActivity() {

    override val helpDescription: String
        get() = "Basic System Toggles that is also available on the tiles menu as well"

    private lateinit var permissionStr: String

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
            Toast.makeText(buttonView.context, "${if (isChecked) "Enabling" else "Disabling"} Private DNS", Toast.LENGTH_LONG).show()
            val onState = resources.getStringArray(R.array.private_dns_entries_option)[toggle_spinner_private_dns.selectedItemPosition]
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) Settings.Global.putString(contentResolver, PRIVATE_DNS_SETTING, if (isChecked) onState else "off")
            LogHelper.i(TAG, "Toggled Private DNS to ${if (isChecked) onState else "off"}")
        }
        toggle_statusbar_private_dns.isChecked = checkComponentEnabled()
        toggle_statusbar_private_dns.setOnCheckedChangeListener { _, isChecked -> packageManager.setComponentEnabledSetting(ComponentName(this, QSPrivateDNSTileService::class.java),
                if (isChecked) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP) }
    }

    private fun checkComponentEnabled(): Boolean { return packageManager.getComponentEnabledSetting(ComponentName(this, QSPrivateDNSTileService::class.java)) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED }

    private fun hasStuffOnScreen(): Boolean { return toggle_private_dns.visibility != View.GONE }

    private var privateDnsUpdater: Boolean = false

    override fun onResume() {
        super.onResume()
        if (!hasStuffOnScreen()) return // Do not continue
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) checkPrivateDns()
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun checkPrivateDns() {
        // Check permission granted
        val isAllowed = ToggleHelper.checkWriteSecurePermission(this)
        toggle_status_private_dns.setTextColor(ContextCompat.getColor(this, if (isAllowed)
            if (PrefHelper.isNightModeEnabled(this)) R.color.green else R.color.dark_green else R.color.red))
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

    companion object {
        private const val TAG = "ToggleActivity"

    }
}
