package com.itachi1706.cheesecakeutilities

import android.app.Activity
import android.app.TaskStackBuilder
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.itachi1706.cheesecakeutilities.features.biometricAuth.AuthenticationActivity
import com.itachi1706.cheesecakeutilities.util.CommonMethods
import com.itachi1706.cheesecakeutilities.util.LogInit.initLogger
import com.itachi1706.helperlib.helpers.LogHelper
import com.itachi1706.helperlib.helpers.PrefHelper

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
        val sp = PrefHelper.getDefaultSharedPreferences(this)
        PrefHelper.handleDefaultThemeSwitch(sp.getString("app_theme", "batterydefault")!!)
        super.onCreate(savedInstanceState)

        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
        initLogger()
        val menuitem = if (this.intent.hasExtra("menuitem")) this.intent.extras!!.getString("menuitem", "") else ""
        val checkGlobal = this.intent.hasExtra("globalcheck") && this.intent.extras!!.getBoolean("globalcheck")
        val authagain = !this.intent.hasExtra("authagain") || this.intent.extras!!.getBoolean("authagain")
        if (!authagain) return
        if (!(menuitem == null || menuitem.isEmpty() || menuitem == "")) {
            if (!CommonMethods.isGlobalLocked(sp) && CommonMethods.isUtilityLocked(sp, menuitem)) {
                LogHelper.i("Authentication", "Requesting Utility Authentication for $menuitem")
                startActivityForResult(Intent(this, AuthenticationActivity::class.java), REQUEST_AUTH)
            }
        }
        if (checkGlobal) {
            if (CommonMethods.isGlobalLocked(sp)) {
                LogHelper.i("Authentication", "Requesting Authentication as app is locked globally")
                startActivityForResult(Intent(this, AuthenticationActivity::class.java), REQUEST_AUTH_GLOBAL)
            }
        }
    }

    @Deprecated("Deprecated in Java")
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
