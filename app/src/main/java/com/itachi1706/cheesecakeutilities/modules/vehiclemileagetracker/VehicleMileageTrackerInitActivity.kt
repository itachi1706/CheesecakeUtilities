package com.itachi1706.cheesecakeutilities.modules.vehiclemileagetracker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.itachi1706.cheesecakeutilities.FirebaseLoginActivity
import com.itachi1706.helperlib.helpers.LogHelper

class VehicleMileageTrackerInitActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Bring to login and move on

        val i = Intent(this, FirebaseLoginActivity::class.java)
        if (intent.extras != null) i.putExtras(intent.extras!!)
        LogHelper.i("VehMileageInit", "Redirecting login activity to VehicleMileageMainActivity")

        val forwardIntent = Intent(this, VehicleMileageMainActivity::class.java)
        i.putExtra(FirebaseLoginActivity.CONTINUE_INTENT, forwardIntent)
        i.putExtra(FirebaseLoginActivity.HELP_EXTRA, "An utility to track vehicle mileage\n\nNote: This is the login screen where you have " +
                "to login with a Google Account to continue as your mileage records will be saved based on your Google Account")
        startActivity(i)
        finish()
    }
}
