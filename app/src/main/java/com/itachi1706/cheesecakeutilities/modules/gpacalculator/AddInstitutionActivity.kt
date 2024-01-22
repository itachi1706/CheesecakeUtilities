package com.itachi1706.cheesecakeutilities.modules.gpacalculator

import android.app.DatePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.itachi1706.cheesecakeutilities.databinding.ActivityGpaCalculatorAddInstitutionBinding
import com.itachi1706.cheesecakeutilities.modules.gpacalculator.objects.GpaInstitution
import com.itachi1706.cheesecakeutilities.modules.gpacalculator.objects.GpaScoring
import com.itachi1706.cheesecakeutilities.util.FirebaseValueEventListener
import com.itachi1706.helperlib.helpers.LogHelper
import java.util.Calendar

class AddInstitutionActivity : AddActivityBase() {

    val modes: HashMap<String, Pair<String, GpaScoring>> = HashMap()
    val selectionList: ArrayList<String> = ArrayList()
    val existingInstitutions: ArrayList<String> = ArrayList()

    private var startTime: Long = System.currentTimeMillis()
    private var endTime: Long = -1

    private lateinit var startListener: DatePickerDialog.OnDateSetListener
    private lateinit var endListener: DatePickerDialog.OnDateSetListener
    
    private lateinit var binding: ActivityGpaCalculatorAddInstitutionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGpaCalculatorAddInstitutionBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        startListener = InstituteDateCallback(START_INSTITUTE)
        endListener = InstituteDateCallback(END_INSTITUTE)
        binding.fromDate.setOnClickListener{
            val calendar = Calendar.getInstance()
            if (startTime > 0) calendar.timeInMillis = startTime
            DatePickerDialog(this, startListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.toDate.setOnClickListener{
            val calendar = Calendar.getInstance()
            if (endTime > 0) calendar.timeInMillis = endTime
            val dt = DatePickerDialog(this, endListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
            dt.setButton(DialogInterface.BUTTON_NEUTRAL, "Present Day") { dialog: DialogInterface, _: Int ->
                endTime = -1
                GpaCalcFirebaseUtils.updateDateTimeViews(binding.fromDate, binding.toDate, startTime, endTime)
                dialog.dismiss()
            }
            dt.datePicker.minDate = startTime
            dt.show()
        }

        // Get the calculation modes
        populateModes()
        populateInstitutions()
        binding.spinnerGpaCalcMode.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                LogHelper.d(TAG, "spinnerGpaCalcMode:onItemSelected")
                if (modes.isEmpty() || selectionList.isEmpty()) return // Simply not do anything
                val mode = modes[selectionList[position]]
                LogHelper.i(TAG, "Selected Mode: ${mode?.second?.name ?: "Unknown"}")
                binding.tilEtCreditsName.visibility = if (mode?.second?.type == "count") View.GONE else View.VISIBLE
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Unused
            }
        }

        binding.gpacalcAdd.setOnClickListener { v ->
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
                binding.etName.setText(institute?.name)
                binding.etShortName.setText(institute?.shortName)
                binding.etCreditsName.setText(institute?.creditName)
                binding.gpacalcAdd.text = "Edit Institution"
                supportActionBar?.title = "Edit an Institution"
                supportActionBar?.subtitle = binding.etName.text.toString()

                // Handle start and end times
                institute?.let {
                    startTime = it.startTimestamp
                    if (it.endTimestamp != (-1).toLong()) endTime = it.endTimestamp
                }
                GpaCalcFirebaseUtils.updateDateTimeViews(binding.fromDate, binding.toDate, startTime, endTime)
            }
        })
    }

    override fun validate(): Any {
        // Check that theres a mode to select
        if (modes.isEmpty() || selectionList.isEmpty()) return "No modes found! Unable to add institution"

        val name = binding.etName.text.toString()
        val shortName = binding.etShortName.text.toString()
        val mode = modes[binding.spinnerGpaCalcMode.selectedItem]
        var credits = binding.etCreditsName.text.toString()

        // Ready error texts
        binding.tilEtName.error = "Required field"
        binding.tilEtShortName.error = "Required field"

        binding.tilEtName.isErrorEnabled = name.isEmpty()
        binding.tilEtShortName.isErrorEnabled = shortName.isEmpty()
        binding.tilToDate.isErrorEnabled = false

        if (mode == null) return "Unable to find mode, please attempt to readd!"

        if (binding.tilEtCreditsName.visibility == View.VISIBLE && credits.isEmpty()) credits = "Credits"

        if (existingInstitutions.contains(shortName) && institute == null) {
            binding.tilEtShortName.error = "Institution Already Exists"
            binding.tilEtShortName.isErrorEnabled = true
        }

        if (binding.tilEtName.isErrorEnabled || binding.tilEtShortName.isErrorEnabled) return "Please resolve the errors before continuing"

        if (endTime < startTime && endTime != (-1).toLong()) {
            binding.tilToDate.error = "Graduation Date cannot be before Start Date"
            binding.tilToDate.isErrorEnabled = true
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
        binding.spinnerGpaCalcMode.adapter = adapter
    }

    private inner class InstituteDateCallback(val type: Int): DatePickerDialog.OnDateSetListener {
        override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
            val cal = GpaCalcFirebaseUtils.getCalendarWithNoTime(year, month, dayOfMonth)
            if (type == START_INSTITUTE) startTime = cal.timeInMillis
            else endTime = cal.timeInMillis
            GpaCalcFirebaseUtils.updateDateTimeViews(binding.fromDate, binding.toDate, startTime, endTime)
        }
    }

    companion object {
        private const val TAG = "GpaCalcAddInstitution"
        private const val START_INSTITUTE = 0
        private const val END_INSTITUTE = 1
    }
}
