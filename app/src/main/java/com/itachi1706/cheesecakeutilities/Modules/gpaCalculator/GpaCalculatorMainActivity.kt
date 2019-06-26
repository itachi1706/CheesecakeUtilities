package com.itachi1706.cheesecakeutilities.Modules.gpaCalculator

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DatabaseReference
import com.itachi1706.cheesecakeutilities.BaseModuleActivity
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.`interface`.StateSwitchListener
import com.itachi1706.cheesecakeutilities.R
import kotlinx.android.synthetic.main.activity_gpa_calculator_main.*

class GpaCalculatorMainActivity(override val helpDescription: String = "A utility for handling keeping track of scores such as Grade Point Averages (GPA)") : BaseModuleActivity(), StateSwitchListener {

    var currentState: Int = STATE_INSTITUTION
    private lateinit var userData: DatabaseReference
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gpa_calculator_main)
        setSupportActionBar(toolbar)

        userId = PreferenceManager.getDefaultSharedPreferences(this).getString("firebase_uid", "nien") ?: "nien"
        if (userId.equals("nien", ignoreCase = true)) {
            // Fail, return to login activity
            Toast.makeText(this, "Invalid Login Token", Toast.LENGTH_SHORT).show()
            val logoutIntent = Intent(this, GpaCalculatorInitActivity::class.java)
            logoutIntent.putExtra("logout", true)
            startActivity(logoutIntent)
            finish()
            return
        }

        fab.drawable.setColorFilter(ResourcesCompat.getColor(resources, R.color.white, null), PorterDuff.Mode.SRC_ATOP)
        fab.setOnClickListener { view ->
            addFabAction(view)
        }

        // Init Firebase
        userData = GpaCalculatorFirebaseUtils.getGpaDatabaseUser(userId)
    }

    private fun addFabAction(view: View) {
        when (currentState) {
            STATE_INSTITUTION -> startActivity(Intent(this, GpaCalculatorAddInstitutionActivity::class.java).apply { putExtra("userid", userId) })
            else -> Snackbar.make(view, "Unimplemetned", Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu_gpa_calculator_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.score_system -> startActivity(Intent(this, GpaCalculatorScoringActivity::class.java))
            R.id.logout -> {
                startActivity(Intent(this, GpaCalculatorInitActivity::class.java).apply { putExtra("logout", true) })
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStateSwitch(newState: Int) {
        currentState = newState
    }

    override fun getState(): Int {
        return currentState
    }

    override fun getUserData(): DatabaseReference {
        return userData
    }

    companion object {
        const val STATE_INSTITUTION = 0 // Institution selection screen
        const val STATE_SEMESTER = 1 // Semester selection screen
        const val STATE_MODULE = 2 // Module selection screen
    }

}
