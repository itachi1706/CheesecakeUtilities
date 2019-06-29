package com.itachi1706.cheesecakeutilities.Modules.gpaCalculator

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.objects.GpaInstitution
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.objects.GpaModule
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.objects.GpaScoring
import com.itachi1706.cheesecakeutilities.R
import com.itachi1706.cheesecakeutilities.Util.LogHelper
import kotlinx.android.synthetic.main.activity_gpa_calculator_add_module.*

class AddModuleActivity : AddActivityBase() {

    private var selectedInstitution: GpaInstitution? = null
    private var scoringObject: GpaScoring? = null
    private lateinit var selectedSemesterKey: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gpa_calculator_add_module)

        val instituteString = intent?.extras!!.getString("institute", "-_-_-")
        selectedSemesterKey = intent?.extras!!.getString("key", "-")
        if (instituteString == "-_-_-" || selectedSemesterKey == "-") {
            Toast.makeText(this, "Invalid Institution or Semester", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Get the calculation modes
        getInstitution(instituteString)
        gpacalc_add.setOnClickListener { v ->
                when (val result = validate()) {
                    is String -> Snackbar.make(v, result, Snackbar.LENGTH_LONG).show()
                    is GpaModule -> {
                        addToDb(result)
                        Toast.makeText(v.context, "Module Added", Toast.LENGTH_LONG).show()
                        finish()
                    }
                    else -> {
                        LogHelper.e(TAG, "Invalid Validation")
                        Snackbar.make(v, "An error occurred adding module", Snackbar.LENGTH_LONG).show()
                    }
                }
        }

        cbPassFail.setOnCheckedChangeListener { _, _ -> updateGradeTierSpinner() }
    }

    override fun validate(): Any {
        val name = etName.text.toString()
        val courseCode = etCourseCode.text.toString()
        val creditsStr = etCredits.text.toString()
        val credits = if (creditsStr.isEmpty()) -1 else creditsStr.toInt()
        val passFail = cbPassFail.isChecked
        val grade = spinnerGpaGrade.selectedItemPosition - 1

        til_etName.error = "Required field"
        til_etCourseCode.error = "Required field"

        til_etName.isErrorEnabled = name.isEmpty()
        til_etCourseCode.isErrorEnabled = courseCode.isEmpty()

        if (til_etName.isErrorEnabled || til_etCourseCode.isErrorEnabled) return "Please resolve the errors before continuing"
        return GpaModule(name, courseCode, grade, credits, passFail)
    }

    private fun addToDb(newModule: GpaModule) {
        if (selectedInstitution == null) return // Don't try to add if you cannot add
        val db = GpaCalcFirebaseUtils.getGpaDatabaseUser(userId)
        db.child(selectedInstitution!!.shortName).child(GpaCalcFirebaseUtils.FB_REC_SEMESTER).child(selectedSemesterKey).child(GpaCalcFirebaseUtils.FB_REC_MODULE)
                .child(newModule.courseCode).setValue(newModule)
    }

    private fun updateScoringFeatures() {
        // Check if count or gpa and hide accordingly. Also check if there is a pass/fail tier
        if (scoringObject?.passtier == null) cbPassFail.isChecked = false
        cbPassFail.isEnabled = scoringObject?.passtier != null
        til_etCredits.visibility = if (scoringObject?.type == "count") View.GONE else View.VISIBLE
        updateGradeTierSpinner()
    }

    val gradeTierList: LinkedHashMap<String, GpaScoring.GpaTier?> = LinkedHashMap()

    private fun updateGradeTierSpinner() {
        gradeTierList.clear()
        gradeTierList["- (Not Graded)"] = null
        val tier = if (cbPassFail.isChecked && scoringObject?.passtier != null) scoringObject?.passtier!! else scoringObject?.gradetier!!
        tier.forEach {
            gradeTierList.put("${it.name} (${it.desc})".replace(" (No description)", ""), it)
        }
        //gradeTierList.putAll(tier.map { i -> "${i.name} (${i.desc})".replace(" (No description)", "") }, tier.toList())
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, gradeTierList.keys.toList())
        spinnerGpaGrade.adapter = adapter
    }

    private fun getScoringObject() {
        Log.i(TAG, "Retrieving Scoring Object for this institution")
        val db = GpaCalcFirebaseUtils.getGpaDatabase().child(GpaCalcFirebaseUtils.FB_REC_SCORING).child(selectedInstitution!!.type)
        db.keepSynced(true)
        db.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                scoringObject = dataSnapshot.getValue(GpaScoring::class.java)
                updateScoringFeatures()
            }

            override fun onCancelled(p0: DatabaseError) {
                LogHelper.w(TAG, "getScoringObject:cancelled", p0.toException())
            }
        })
    }

    private fun getInstitution(institute: String) {
        val db = GpaCalcFirebaseUtils.getGpaDatabaseUser(userId).child(institute)
        db.keepSynced(true)
        db.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                selectedInstitution = dataSnapshot.getValue(GpaInstitution::class.java)
                supportActionBar?.subtitle = if (selectedInstitution == null) "An error occurred" else "${selectedInstitution?.name} | ${selectedInstitution?.semester!![selectedSemesterKey]?.name}"
                getScoringObject()
            }

            override fun onCancelled(p0: DatabaseError) {
                LogHelper.w(TAG, "getInstitution:cancelled", p0.toException())
            }
        })
    }

    companion object {
        private const val TAG = "GpaCalcAddModule"
    }
}
