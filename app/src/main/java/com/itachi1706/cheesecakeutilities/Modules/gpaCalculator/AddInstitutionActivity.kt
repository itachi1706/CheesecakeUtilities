package com.itachi1706.cheesecakeutilities.Modules.gpaCalculator

import android.app.DatePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.objects.GpaInstitution
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.objects.GpaScoring
import com.itachi1706.cheesecakeutilities.R
import com.itachi1706.cheesecakeutilities.Util.FirebaseValueEventListener
import com.itachi1706.cheesecakeutilities.Util.LogHelper
import kotlinx.android.synthetic.main.activity_gpa_calculator_add_institution.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class AddInstitutionActivity : AddActivityBase() {

    val modes: HashMap<String, Pair<String, GpaScoring>> = HashMap()
    val selectionList: ArrayList<String> = ArrayList()
    val existingInstitutions: ArrayList<String> = ArrayList()

    private var startTime: Long = System.currentTimeMillis()
    private var endTime: Long = -1

    private lateinit var startListener: DatePickerDialog.OnDateSetListener
    private lateinit var endListener: DatePickerDialog.OnDateSetListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gpa_calculator_add_institution)

        startListener = InstituteDateCallback(START_INSTITUTE)
        endListener = InstituteDateCallback(END_INSTITUTE)
        fromDate.setOnClickListener{
            val calendar = Calendar.getInstance()
            if (startTime > 0) calendar.timeInMillis = startTime
            DatePickerDialog(this, startListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        toDate.setOnClickListener{
            val calendar = Calendar.getInstance()
            if (endTime > 0) calendar.timeInMillis = endTime
            val dt = DatePickerDialog(this, endListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
            dt.setButton(DialogInterface.BUTTON_NEUTRAL, "Present Day") { dialog: DialogInterface, _: Int ->
                endTime = -1
                GpaCalcFirebaseUtils.updateDateTimeViews(fromDate, toDate, startTime, endTime)
                dialog.dismiss()
            }
            dt.datePicker.minDate = startTime
            dt.show()
        }

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
        GpaCalcFirebaseUtils.getGpaDatabaseUser(userId).child(editKey).addListenerForSingleValueEvent(object: FirebaseValueEventListener(TAG, "editMode") {
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

                // Handle start and end times
                if (institute?.startTimestamp != null) startTime = institute!!.startTimestamp
                if (institute?.endTimestamp != null && institute?.endTimestamp != (-1).toLong()) endTime = institute!!.endTimestamp
                GpaCalcFirebaseUtils.updateDateTimeViews(fromDate, toDate, startTime, endTime)
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
        til_toDate.isErrorEnabled = false

        if (mode == null) return "Unable to find mode, please attempt to readd!"

        if (til_etCreditsName.visibility == View.VISIBLE && credits.isEmpty()) credits = "Credits"

        if (existingInstitutions.contains(shortName) && institute == null) {
            til_etShortName.error = "Institution Already Exists"
            til_etShortName.isErrorEnabled = true
        }

        if (til_etName.isErrorEnabled || til_etShortName.isErrorEnabled) return "Please resolve the errors before continuing"

        if (endTime < startTime && endTime != (-1).toLong()) {
            til_toDate.error = "Graduation Date cannot be before Start Date"
            til_toDate.isErrorEnabled = true
            return "Please resolve date errors before continuing"
        }

        return GpaInstitution(name, shortName, mode.first, credits, startTimestamp = startTime, endTimestamp = endTime)
    }

    private fun addToDb(newInstitution: GpaInstitution) {
        val db = GpaCalcFirebaseUtils.getGpaDatabaseUser(userId)
        if (institute == null) db.child(newInstitution.shortName).setValue(newInstitution)
        else {
            val edited = institute!!.copy(name = newInstitution.name, shortName = newInstitution.shortName, type = newInstitution.type, creditName = newInstitution.creditName,
                    startTimestamp = newInstitution.startTimestamp, endTimestamp = newInstitution.endTimestamp)
            if (edited.shortName != institute!!.shortName) {
                // Change of shortname as well, delete old key
                db.child(institute!!.shortName).removeValue()
            }
            db.child(edited.shortName).setValue(edited)
        }
    }

    private fun populateModes() {
        val db = GpaCalcFirebaseUtils.getGpaDatabase().child(GpaCalcFirebaseUtils.FB_REC_SCORING)
        db.addListenerForSingleValueEvent(object: FirebaseValueEventListener(TAG, "populateModes") {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.hasChildren()) return
                modes.clear()
                dataSnapshot.children.forEach {
                    val gpa = it.getValue(GpaScoring::class.java) ?: return@forEach
                    modes[gpa.shortname] = Pair(it.key!!, gpa)
                }

                updateAdapter()
            }
        })
    }

    private fun populateInstitutions() {
        val db = GpaCalcFirebaseUtils.getGpaDatabaseUser(userId)
        db.keepSynced(true)
        db.addListenerForSingleValueEvent(object: FirebaseValueEventListener(TAG, "populateInstitutions") {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.hasChildren()) return // Don't need do anything as we have no existing institutions
                existingInstitutions.clear()
                dataSnapshot.children.forEach{ existingInstitutions.add(it.key!!) }
            }
        })
    }

    private fun updateAdapter() {
        selectionList.clear()
        modes.keys.toCollection(selectionList)
        val adapter: ArrayAdapter<String> = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, selectionList)
        spinnerGpaCalcMode.adapter = adapter
    }

    private inner class InstituteDateCallback(val type: Int): DatePickerDialog.OnDateSetListener {
        override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
            val cal = GpaCalcFirebaseUtils.getCalendarWithNoTime(year, month, dayOfMonth)
            if (type == START_INSTITUTE) startTime = cal.timeInMillis
            else endTime = cal.timeInMillis
            GpaCalcFirebaseUtils.updateDateTimeViews(fromDate, toDate, startTime, endTime)
        }
    }

    companion object {
        private const val TAG = "GpaCalcAddInstitution"
        private const val START_INSTITUTE = 0
        private const val END_INSTITUTE = 1
    }
}
