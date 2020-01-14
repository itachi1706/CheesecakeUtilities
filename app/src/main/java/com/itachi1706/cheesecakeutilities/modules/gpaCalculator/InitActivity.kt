package com.itachi1706.cheesecakeutilities.modules.gpaCalculator

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.itachi1706.cheesecakeutilities.FirebaseLoginActivity
import com.itachi1706.helperlib.helpers.LogHelper

/**
 * Created by Kenneth on 24/6/2019.
 * for com.itachi1706.cheesecakeutilities.modules.gpaCalculator in CheesecakeUtilities
 */

class InitActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val i = Intent(this, FirebaseLoginActivity::class.java).apply {
            if (intent.extras != null) putExtras(intent.extras!!)

            putExtra(FirebaseLoginActivity.CONTINUE_INTENT, Intent(applicationContext, MainViewActivity::class.java))
            putExtra(FirebaseLoginActivity.HELP_EXTRA, "An utility to help keep track of scores\n\nNote: This is the login screen where you have " +
                    "to login with a Google Account to continue as your scores will be saved based on your Google Account")
        }
        LogHelper.i("GpaCalculatorInit", "Redirecting login activity to GpaCalculatorMainActivity")
        startActivity(i)
        finish()
    }
}