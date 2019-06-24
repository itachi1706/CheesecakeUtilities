package com.itachi1706.cheesecakeutilities.Modules.gpaCalculator

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.snackbar.Snackbar
import com.itachi1706.cheesecakeutilities.BaseModuleActivity
import com.itachi1706.cheesecakeutilities.R
import kotlinx.android.synthetic.main.activity_gpa_calculator_main.*

class GpaCalculatorMainActivity(override val helpDescription: String = "A utility for handling keeping track of scores such as Grade Point Averages (GPA)") : BaseModuleActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gpa_calculator_main)
        setSupportActionBar(toolbar)

        fab.drawable.setColorFilter(ResourcesCompat.getColor(resources, R.color.white, null), PorterDuff.Mode.SRC_ATOP)
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Unimplemetned", Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu_gpa_calculator_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.score_system -> startActivity(Intent(this, GpaCalculatorScoringActivity::class.java)) //Snackbar.make(fab, "Unimplemented", Snackbar.LENGTH_SHORT).show()
            R.id.logout -> {
                startActivity(Intent(this, GpaCalculatorInitActivity::class.java).apply { putExtra("logout", true) })
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}
