package com.itachi1706.cheesecakeutilities.Modules.gpaCalculator

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.itachi1706.cheesecakeutilities.FirebaseLoginActivity

/**
 * Created by Kenneth on 24/6/2019.
 * for com.itachi1706.cheesecakeutilities.Modules.gpaCalculator in CheesecakeUtilities
 */

class GpaCalculatorInitActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val i = Intent(this, FirebaseLoginActivity::class.java).apply {
            if (intent.extras != null) putExtras(intent.extras!!)

            putExtra(FirebaseLoginActivity.CONTINUE_INTENT, Intent(applicationContext, GpaCalculatorMainActivity::class.java))
            putExtra(FirebaseLoginActivity.HELP_EXTRA, "An utility to help keep track of scores\n\nNote: This is the login screen where you have " +
                    "to login with a Google Account to continue as your scores will be saved based on your Google Account")
        }
        Log.i("GpaCalculatorInit", "Redirecting login activity to VehicleMileageMainActivity")
        startActivity(i)
        finish()
    }
}