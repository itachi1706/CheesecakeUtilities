package com.itachi1706.cheesecakeutilities.modules.gpacalculator

import android.app.DatePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.DatePicker
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.itachi1706.cheesecakeutilities.databinding.ActivityGpaCalculatorAddSemesterBinding
import com.itachi1706.cheesecakeutilities.modules.gpacalculator.objects.GpaInstitution
import com.itachi1706.cheesecakeutilities.modules.gpacalculator.objects.GpaSemester
import com.itachi1706.cheesecakeutilities.util.FirebaseValueEventListener
import com.itachi1706.helperlib.helpers.LogHelper
import java.util.Calendar

class AddSemesterActivity : AddActivityBase() {

    private var selectedInstitution: GpaInstitution? = null
    private lateinit var instituteString: String

    private var startTime: Long = -1
    private var endTime: Long = -1

    private lateinit var startDateListener: DatePickerDialog.OnDateSetListener
    private lateinit var endDateListener: DatePickerDialog.OnDateSetListener

    private lateinit var binding: ActivityGpaCalculatorAddSemesterBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGpaCalculatorAddSemesterBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        instituteString = intent?.extras!!.getString("institute", "-_-_-")
        if (instituteString == "-_-_-") {
            Toast.makeText(this, "Invalid Institution", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        startDateListener = SemesterDateCallback(START_SEMESTER)
        endDateListener = SemesterDateCallback(END_SEMESTER)
        binding.fromDate.setOnClickListener{
            val calendar = Calendar.getInstance()
            if (startTime > 0) calendar.timeInMillis = startTime
            val dt = DatePickerDialog(this, startDateListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH))
            selectedInstitution?.let {
                if (it.startTimestamp > 0) dt.datePicker.minDate = it.startTimestamp
                if (it.endTimestamp > 0) dt.datePicker.maxDate = it.endTimestamp
            }
            dt.show()
        }

        binding.toDate.setOnClickListener{
            val calendar = Calendar.getInstance()
            if (endTime > 0) calendar.timeInMillis = endTime
            val dt = DatePickerDialog(this, endDateListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
            dt.setButton(DialogInterface.BUTTON_NEUTRAL, "Present Day") { dialog: DialogInterface, _: Int ->
                endTime = -1
                if (startTime == (-1).toLong()) startTime = selectedInstitution?.startTimestamp ?: System.currentTimeMillis()
                GpaCalcFirebaseUtils.updateDateTimeViews(binding.fromDate, binding.toDate, startTime, endTime)
                dialog.dismiss()
            }
            dt.datePicker.minDate = startTime
            selectedInstitution?.let {
                if (it.startTimestamp > 0 && it.startTimestamp > startTime) dt.datePicker.minDate = it.startTimestamp
                if (it.endTimestamp > 0) dt.datePicker.maxDate = it.endTimestamp
            }
            dt.show()
        }


        // Get the calculation modes
        getInstitution(instituteString)
        binding.gpacalcAdd.setOnClickListener { v ->
                when (val result = validate()) {
                    is String -> Snackbar.make(v, result, Snackbar.LENGTH_LONG).show()
                    is GpaSemester -> {
                        addToDb(result)
                        Toast.makeText(v.context, "Semester ${if (semester == null) "Added" else "Updated"}", Toast.LENGTH_LONG).show()
                        finish()
                    }
                    else -> {
                        LogHelper.e(TAG, "Invalid Validation")
                        Snackbar.make(v, "An error occurred adding semester", Snackbar.LENGTH_LONG).show()
                    }
                }
        }
    }

    private var semester: GpaSemester? = null

    override fun editModeEnabled(editKey: String) {
        GpaCalcFirebaseUtils.getGpaDatabaseUser(userId).child(instituteString).child(GpaCalcFirebaseUtils.FB_REC_SEMESTER)
                .child(editKey).addListenerForSingleValueEvent(object: FirebaseValueEventListener(TAG, "editMode") {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Make user update scoring mode again
                semester = dataSnapshot.getValue(GpaSemester::class.java)
                binding.etName.setText(semester?.name)
                binding.gpacalcAdd.text = "Edit Semester"
                supportActionBar?.title = "Edit aa Semester"
                supportActionBar?.subtitle = binding.etName.text.toString()

                // Handle start and end times
                if (semester?.startTimestamp != null) startTime = semester!!.startTimestamp
                if (semester?.endTimestamp != null && semester?.endTimestamp != (-1).toLong()) endTime = semester!!.endTimestamp
                GpaCalcFirebaseUtils.updateDateTimeViews(binding.fromDate, binding.toDate, startTime, endTime)
            }

        })
    }

    override fun validate(): Any {
        val name = binding.etName.text.toString()

        // Ready error texts
        binding.tilEtName.error = "Required field"
        binding.tilEtName.isErrorEnabled = name.isEmpty()
        binding.tilToDate.isErrorEnabled = false
        if (startTime == (-1).toLong()) startTime = System.currentTimeMillis()

        if (binding.tilEtName.isErrorEnabled) return "Please resolve the errors before continuing"

        if (endTime < startTime && endTime != (-1).toLong()) {
            binding.tilToDate.error = "Semester End Date cannot be before Start Date"
            binding.tilToDate.isErrorEnabled = true
            return "Please resolve date errors before continuing"
        }

        return GpaSemester(name, startTimestamp = startTime, endTimestamp = endTime)
    }

    private fun addToDb(newSemester: GpaSemester) {
        if (selectedInstitution == null) return // Don't try to add if you cannot add
        val db = GpaCalcFirebaseUtils.getGpaDatabaseUser(userId).child(selectedInstitution!!.shortName).child(GpaCalcFirebaseUtils.FB_REC_SEMESTER)
        if (semester == null) {
            val newRec = db.push()
            newRec.setValue(newSemester)
        } else {
            val edited = semester!!.copy(name = newSemester.name, startTimestamp = newSemester.startTimestamp, endTimestamp = newSemester.endTimestamp)
            db.child(editKey!!).setValue(edited)
        }
    }

    private fun getInstitution(institute: String) {
        val db = GpaCalcFirebaseUtils.getGpaDatabaseUser(userId).child(institute)
        db.keepSynced(true)
        db.addListenerForSingleValueEvent(object: FirebaseValueEventListener(TAG, "getInstitution") {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                selectedInstitution = dataSnapshot.getValue(GpaInstitution::class.java)
                supportActionBar?.subtitle = if (selectedInstitution == null) "An error occurred" else selectedInstitution?.name
            }
        })
    }

    private inner class SemesterDateCallback(val type: Int): DatePickerDialog.OnDateSetListener {
        override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
            val cal = GpaCalcFirebaseUtils.getCalendarWithNoTime(year, month, dayOfMonth)
            if (type == START_SEMESTER) startTime = cal.timeInMillis
            else endTime = cal.timeInMillis
            GpaCalcFirebaseUtils.updateDateTimeViews(binding.fromDate, binding.toDate, startTime, endTime)
        }
    }

    companion object {
        private const val TAG = "GpaCalcAddSemester"
        private const val START_SEMESTER = 0
        private const val END_SEMESTER = 1
    }
}
