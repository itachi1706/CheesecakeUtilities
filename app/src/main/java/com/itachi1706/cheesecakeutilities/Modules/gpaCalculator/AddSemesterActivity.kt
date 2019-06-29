package com.itachi1706.cheesecakeutilities.Modules.gpaCalculator

import android.os.Bundle
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.objects.GpaInstitution
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.objects.GpaSemester
import com.itachi1706.cheesecakeutilities.R
import com.itachi1706.cheesecakeutilities.Util.LogHelper
import kotlinx.android.synthetic.main.activity_gpa_calculator_add_semester.*

class AddSemesterActivity : AddActivityBase() {

    private var selectedInstitution: GpaInstitution? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gpa_calculator_add_semester)

        val instituteString = intent?.extras!!.getString("institute", "-_-_-")
        if (instituteString == "-_-_-") {
            Toast.makeText(this, "Invalid Institution", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Get the calculation modes
        getInstitution(instituteString)
        gpacalc_add.setOnClickListener { v ->
                when (val result = validate()) {
                    is String -> Snackbar.make(v, result, Snackbar.LENGTH_LONG).show()
                    is GpaSemester -> {
                        addToDb(result)
                        Toast.makeText(v.context, "Semester Added", Toast.LENGTH_LONG).show()
                        finish()
                    }
                    else -> {
                        LogHelper.e(TAG, "Invalid Validation")
                        Snackbar.make(v, "An error occurred adding semester", Snackbar.LENGTH_LONG).show()
                    }
                }
        }
    }

    override fun validate(): Any {
        val name = etName.text.toString()

        // Ready error texts
        til_etName.error = "Required field"
        til_etName.isErrorEnabled = name.isEmpty()

        if (til_etName.isErrorEnabled) return "Please resolve the errors before continuing"

        return GpaSemester(name)
    }

    private fun addToDb(newSemester: GpaSemester) {
        if (selectedInstitution == null) return // Don't try to add if you cannot add
        val db = GpaCalcFirebaseUtils.getGpaDatabaseUser(userId)
        val newRec = db.child(selectedInstitution!!.shortName).child(GpaCalcFirebaseUtils.FB_REC_SEMESTER).push()
        newRec.setValue(newSemester)
    }

    private fun getInstitution(institute: String) {
        val db = GpaCalcFirebaseUtils.getGpaDatabaseUser(userId).child(institute)
        db.keepSynced(true)
        db.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                selectedInstitution = dataSnapshot.getValue(GpaInstitution::class.java)
                supportActionBar?.subtitle = if (selectedInstitution == null) "An error occurred" else selectedInstitution?.name
            }

            override fun onCancelled(p0: DatabaseError) {
                LogHelper.w(TAG, "getInstitution:cancelled", p0.toException())
            }
        })
    }

    companion object {
        private const val TAG = "GpaCalcAddSemester"
    }
}
