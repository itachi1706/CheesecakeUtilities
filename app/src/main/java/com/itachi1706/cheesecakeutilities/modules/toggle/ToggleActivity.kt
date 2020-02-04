package com.itachi1706.cheesecakeutilities.modules.toggle

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.itachi1706.cheesecakeutilities.BaseModuleActivity
import com.itachi1706.cheesecakeutilities.R
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
    }

    private fun hasStuffOnScreen(): Boolean {
        return toggle_private_dns.visibility != View.GONE
    }

    override fun onResume() {
        super.onResume()
        if (!hasStuffOnScreen()) return // Do not continue
        checkPrivateDns()
    }

    private fun checkPrivateDns() {
        // Check permission granted
        val isAllowed = checkWriteSecurePermission()
        toggle_status_private_dns.setTextColor(ContextCompat.getColor(this,
                if (isAllowed) if (PrefHelper.isNightModeEnabled(this)) R.color.green else R.color.dark_green else R.color.red))
        toggle_status_private_dns.text = if (isAllowed) "Granted" else "Not Granted"
        toggle_spinner_private_dns.isEnabled = isAllowed
        toggle_statusbar_private_dns.isEnabled = isAllowed
        toggle_switch_private_dns.isEnabled = isAllowed
        if (!isAllowed) return // End here as we should not continue further
    }

    private fun checkWriteSecurePermission(): Boolean {
        val requiredPermission = Manifest.permission.WRITE_SECURE_SETTINGS;
        return when (checkCallingOrSelfPermission(requiredPermission)) {
            PackageManager.PERMISSION_GRANTED -> true
            PackageManager.PERMISSION_DENIED -> false
            else -> false
        }
    }
}
