package com.itachi1706.cheesecakeutilities.modules.gpaCalculator

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
    var editKey: String? = null

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

    override fun onStart() {
        super.onStart()
        editKey = intent?.extras?.getString("editmode")
        if (editKey != null) {
            editModeEnabled(editKey!!)
        }
    }

    abstract fun editModeEnabled(editKey: String)

    abstract fun validate(): Any

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return if (item?.itemId == android.R.id.home) {
            finish()
            true
        } else super.onOptionsItemSelected(item)
    }
}