package com.itachi1706.cheesecakeutilities.Modules.gpaCalculator

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * Created by Kenneth on 29/6/2019.
 * for com.itachi1706.cheesecakeutilities.Modules.gpaCalculator in CheesecakeUtilities
 */
abstract class AddActivityBase : AppCompatActivity() {

    lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userId = intent?.extras!!.getString("userid", "nien")
        if (userId == "nien") {
            Toast.makeText(this, "Invalid User", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }

    abstract fun validate(): Any

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return if (item?.itemId == android.R.id.home) {
            finish()
            true
        } else super.onOptionsItemSelected(item)
    }
}