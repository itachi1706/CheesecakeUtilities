package com.itachi1706.cheesecakeutilities.Modules.gpaCalculator

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.objects.GpaInstitution
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.objects.GpaScoring
import com.itachi1706.cheesecakeutilities.R
import com.itachi1706.cheesecakeutilities.Util.LogHelper
import kotlinx.android.synthetic.main.activity_gpa_calculator_add_institution.*

class AddInstitutionActivity : AddActivityBase() {

    val modes: HashMap<String, Pair<String, GpaScoring>> = HashMap()
    val selectionList: ArrayList<String> = ArrayList()
    val existingInstitutions: ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gpa_calculator_add_institution)

        // Get the calculation modes
        populateModes()
        populateInstitutions()
        spinnerGpaCalcMode.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                Log.d(TAG, "spinnerGpaCalcMode:onItemSelected")
                if (modes.isEmpty() || selectionList.isEmpty()) return // Simply not do anything
                val mode = modes[selectionList[position]]
                Log.i(TAG, "Selected Mode: ${mode?.second?.name ?: "Unknown"}")
                til_etCreditsName.visibility = if (mode?.second?.type == "count") View.GONE else View.VISIBLE
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Unused
            }
        }

        gpacalc_add.setOnClickListener { v ->
                when (val result = validate()) {
                    is String -> Snackbar.make(v, result, Snackbar.LENGTH_LONG).show()
                    is GpaInstitution -> {
                        addToDb(result)
                        Toast.makeText(v.context, "Institution ${if (institute == null) "Added" else "Updated"}", Toast.LENGTH_LONG).show()
                        finish()
                    }
                    else -> {
                        LogHelper.e(TAG, "Invalid Validation")
                        Snackbar.make(v, "An error occurred adding institution", Snackbar.LENGTH_LONG).show()
                    }
                }
        }
    }

    private var institute: GpaInstitution? = null

    override fun editModeEnabled(editKey: String) {
        GpaCalcFirebaseUtils.getGpaDatabaseUser(userId).child(editKey).addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                LogHelper.e(TAG, "editMode:cancelled", p0.toException())
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Make user update scoring mode again
                institute = dataSnapshot.getValue(GpaInstitution::class.java)
                Snackbar.make(findViewById(android.R.id.content), "Please update scoring mode agian", Snackbar.LENGTH_LONG).show()
                etName.setText(institute?.name)
                etShortName.setText(institute?.shortName)
                etCreditsName.setText(institute?.creditName)
                gpacalc_add.text = "Edit Institution"
                supportActionBar?.title = "Edit an Institution"
                supportActionBar?.subtitle = etName.text.toString()
            }

        })
    }

    override fun validate(): Any {
        // Check that theres a mode to select
        if (modes.isEmpty() || selectionList.isEmpty()) return "No modes found! Unable to add institution"

        val name = etName.text.toString()
        val shortName = etShortName.text.toString()
        val mode = modes[spinnerGpaCalcMode.selectedItem]
        var credits = etCreditsName.text.toString()

        // Ready error texts
        til_etName.error = "Required field"
        til_etShortName.error = "Required field"

        til_etName.isErrorEnabled = name.isEmpty()
        til_etShortName.isErrorEnabled = shortName.isEmpty()

        if (mode == null) return "Unable to find mode, please attempt to readd!"

        if (til_etCreditsName.visibility == View.VISIBLE && credits.isEmpty()) credits = "Credits"

        if (existingInstitutions.contains(shortName) && institute == null) {
            til_etShortName.error = "Institution Already Exists"
            til_etShortName.isErrorEnabled = true
        }

        if (til_etName.isErrorEnabled || til_etShortName.isErrorEnabled) return "Please resolve the errors before continuing"

        return GpaInstitution(name, shortName, mode.first, credits)
    }

    private fun addToDb(newInstitution: GpaInstitution) {
        val db = GpaCalcFirebaseUtils.getGpaDatabaseUser(userId)
        if (institute == null) db.child(newInstitution.shortName).setValue(newInstitution)
        else {
            val edited = institute!!.copy(name = newInstitution.name, shortName = newInstitution.shortName, type = newInstitution.type, creditName = newInstitution.creditName)
            if (edited.shortName != institute!!.shortName) {
                // Change of shortname as well, delete old key
                db.child(institute!!.shortName).removeValue()
            }
            db.child(edited.shortName).setValue(edited)
        }
    }

    private fun populateModes() {
        val db = GpaCalcFirebaseUtils.getGpaDatabase().child(GpaCalcFirebaseUtils.FB_REC_SCORING)
        db.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.hasChildren()) return
                modes.clear()
                dataSnapshot.children.forEach {
                    val gpa = it.getValue(GpaScoring::class.java) ?: return@forEach
                    modes[gpa.shortname] = Pair(it.key!!, gpa)
                }

                updateAdapter()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                LogHelper.w(TAG, "populateModes:cancelled", databaseError.toException())
            }
        })
    }

    private fun populateInstitutions() {
        val db = GpaCalcFirebaseUtils.getGpaDatabaseUser(userId)
        db.keepSynced(true)
        db.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.hasChildren()) return // Don't need do anything as we have no existing institutions
                existingInstitutions.clear()
                dataSnapshot.children.forEach{ existingInstitutions.add(it.key!!) }
            }

            override fun onCancelled(p0: DatabaseError) {
                LogHelper.w(TAG, "populateInstitutions:cancelled", p0.toException())
            }
        })
    }

    private fun updateAdapter() {
        selectionList.clear()
        modes.keys.toCollection(selectionList)
        val adapter: ArrayAdapter<String> = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, selectionList)
        spinnerGpaCalcMode.adapter = adapter
    }

    companion object {
        private const val TAG = "GpaCalcAddInstitution"
    }
}
