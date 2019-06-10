package com.itachi1706.cheesecakeutilities

import android.app.Activity
import android.app.TaskStackBuilder
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.crashlytics.android.Crashlytics
import com.itachi1706.appupdater.Util.PrefHelper
import com.itachi1706.cheesecakeutilities.Features.FingerprintAuth.AuthenticationActivity
import com.itachi1706.cheesecakeutilities.Util.CommonMethods
import io.fabric.sdk.android.Fabric

/**
 * Created by Kenneth on 12/5/2019.
 * for com.itachi1706.cheesecakeutilities in CheesecakeUtilities
 */
abstract class BaseActivity : AppCompatActivity() {

    abstract val helpDescription: String

    companion object {
        private const val REQUEST_AUTH = 3
        private const val REQUEST_AUTH_GLOBAL = 4
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fabric = Fabric.Builder(this).kits(Crashlytics()).debuggable(BuildConfig.DEBUG).build()
        if (!BuildConfig.DEBUG) Fabric.with(fabric)
        val menuitem = if (this.intent.hasExtra("menuitem")) this.intent.extras!!.getString("menuitem", "") else ""
        val checkGlobal = this.intent.hasExtra("globalcheck") && this.intent.extras!!.getBoolean("globalcheck")
        val authagain = !this.intent.hasExtra("authagain") || this.intent.extras!!.getBoolean("authagain")
        if (!authagain) return
        val sp = PrefHelper.getDefaultSharedPreferences(this)
        GeneralSettingsActivity.GeneralPreferenceFragment.updateDarkModeSetting(sp.getString("app_theme", "batterydefault"))
        if (!(menuitem == null || menuitem.isEmpty() || menuitem == "")) {
            if (!CommonMethods.isGlobalLocked(sp) && CommonMethods.isUtilityLocked(sp, menuitem)) {
                Log.i("Authentication", "Requesting Utility Authentication for $menuitem")
                startActivityForResult(Intent(this, AuthenticationActivity::class.java), REQUEST_AUTH)
            }
        }
        if (checkGlobal) {
            if (CommonMethods.isGlobalLocked(sp)) {
                Log.i("Authentication", "Requesting Authentication as app is locked globally")
                startActivityForResult(Intent(this, AuthenticationActivity::class.java), REQUEST_AUTH_GLOBAL)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_AUTH) {
            if (resultCode == Activity.RESULT_CANCELED) {
                finish()
            }
        }
        if (requestCode == REQUEST_AUTH_GLOBAL) {
            if (resultCode == Activity.RESULT_CANCELED) {
                finishAffinity()
            } else if (resultCode == Activity.RESULT_OK) {
                TaskStackBuilder.create(this)
                        .addParentStack(MainMenuActivity::class.java)
                        .addNextIntent(Intent(this, MainMenuActivity::class.java).putExtra("authagain", false))
                        .addNextIntent(Intent(this, this.javaClass).putExtra("authagain", false))
                        .startActivities()
            }
        }
    }
}
