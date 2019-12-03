package com.itachi1706.cheesecakeutilities.Modules.gpaCalculator

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.StyleableRes
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.objects.GpaInstitution
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.objects.GpaModule
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.objects.GpaScoring
import com.itachi1706.cheesecakeutilities.R
import com.itachi1706.cheesecakeutilities.Util.FirebaseValueEventListener
import com.itachi1706.cheesecakeutilities.Util.LogHelper
import com.itachi1706.cheesecakeutilities.extlibs.com.thebluealliance.spectrum.SpectrumPalette
import kotlinx.android.synthetic.main.activity_gpa_calculator_add_module.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap

class AddModuleActivity : AddActivityBase() {

    private var selectedInstitution: GpaInstitution? = null
    private var scoringObject: GpaScoring? = null
    private lateinit var selectedSemesterKey: String
    private lateinit var instituteString: String

    private var colorSelected: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gpa_calculator_add_module)

        instituteString = intent?.extras!!.getString("institute", "-_-_-")
        selectedSemesterKey = intent?.extras!!.getString("key", "-")
        if (instituteString == "-_-_-" || selectedSemesterKey == "-") {
            Toast.makeText(this, "Invalid Institution or Semester", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        if (editKey == null) randomizeColor() // Randomly pick color if in add mode

        // Get the calculation modes
        getInstitution(instituteString)
        gpacalc_add.setOnClickListener { v ->
                when (val result = validate()) {
                    is String -> Snackbar.make(v, result, Snackbar.LENGTH_LONG).show()
                    is GpaModule -> {
                        addToDb(result)
                        Toast.makeText(v.context, "Module ${if (module == null) "Added" else "Updated"}", Toast.LENGTH_LONG).show()
                        finish()
                    }
                    else -> {
                        LogHelper.e(TAG, "Invalid Validation")
                        Snackbar.make(v, "An error occurred adding module", Snackbar.LENGTH_LONG).show()
                    }
                }
        }

        cbPassFail.setOnCheckedChangeListener { _, _ -> updateGradeTierSpinner() }
        module_color_selector.setOnColorSelectedListener(object: SpectrumPalette.OnColorSelectedListener {
            override fun onColorSelected(color: Int) {
                colorSelected = color
                Log.d(TAG, "Color Selected: #${Integer.toHexString(color).toUpperCase(Locale.US)}")
            }
        })
    }

    private fun randomizeColor() {
        val typeArrayTmp = resources.obtainTypedArray(R.array.module_colors)
        val colorArray: ArrayList<Int> = ArrayList()
        for (@StyleableRes i in 0 until typeArrayTmp.length()) {
            colorArray.add(typeArrayTmp.getColor(i, 0))
        }
        colorSelected = colorArray.shuffled().take(1)[0]
        module_color_selector.setSelectedColor(colorSelected)
        typeArrayTmp.recycle()
    }

    private var module: GpaModule? = null

    override fun editModeEnabled(editKey: String) {
        GpaCalcFirebaseUtils.getGpaDatabaseUser(userId).child(instituteString).child(GpaCalcFirebaseUtils.FB_REC_SEMESTER).child(selectedSemesterKey)
                .child(GpaCalcFirebaseUtils.FB_REC_MODULE).child(editKey).addListenerForSingleValueEvent(object: FirebaseValueEventListener(TAG, "editMode"){
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Make user update scoring mode again
                module = dataSnapshot.getValue(GpaModule::class.java)
                Snackbar.make(findViewById(android.R.id.content), "Please update grade agian", Snackbar.LENGTH_LONG).show()
                etName.setText(module?.name)
                etCourseCode.setText(module?.courseCode)
                etCredits.setText(module?.credits.toString())
                cbPassFail.isChecked = module?.passFail ?: false
                gpacalc_add.text = "Edit Module"
                if (module?.color != 0) module_color_selector.setSelectedColor(module?.color!!)
                else randomizeColor()
                supportActionBar?.title = "Edit a Module"
                supportActionBar?.subtitle = "${etName.text.toString()} [${etCourseCode.text.toString()}]"
            }

        })
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

        // Make sure course code is unique
        if (selectedInstitution == null) return "Invalid institution error"
        if (selectedInstitution!!.semester[selectedSemesterKey]?.modules?.contains(courseCode)!! && module == null) {
            til_etCourseCode.error = "Module Already Exists"
            til_etCourseCode.isErrorEnabled = true
        }
        
        if (til_etName.isErrorEnabled || til_etCourseCode.isErrorEnabled) return "Please resolve the errors before continuing"
        if (colorSelected == 0) return "Please select module color"

        return GpaModule(name, courseCode, grade, credits, passFail, colorSelected)
    }

    private fun addToDb(newModule: GpaModule) {
        if (selectedInstitution == null) return // Don't try to add if you cannot add
        val db = GpaCalcFirebaseUtils.getGpaDatabaseUser(userId).child(selectedInstitution!!.shortName).child(GpaCalcFirebaseUtils.FB_REC_SEMESTER)
                .child(selectedSemesterKey).child(GpaCalcFirebaseUtils.FB_REC_MODULE)
        if (module == null) db.child(newModule.courseCode).setValue(newModule)
        else {
            val edited = module!!.copy(name = newModule.name, courseCode = newModule.courseCode, gradeTier = newModule.gradeTier, credits = newModule.credits, passFail = newModule.passFail,
                    color = colorSelected)
            if (edited.courseCode != module!!.courseCode) {
                // Change of shortname as well, delete old key
                db.child(module!!.courseCode).removeValue()
            }
            db.child(edited.courseCode).setValue(edited)
        }
    }

    private fun updateScoringFeatures() {
        // Check if count or gpa and hide accordingly. Also check if there is a pass/fail tier
        if (scoringObject?.passtier == null) cbPassFail.isChecked = false
        cbPassFail.isEnabled = scoringObject?.passtier != null
        til_etCredits.visibility = if (scoringObject?.type == "count") View.GONE else View.VISIBLE
        updateGradeTierSpinner()
    }

    private val gradeTierList: LinkedHashMap<String, GpaScoring.GpaTier?> = LinkedHashMap()

    private fun updateGradeTierSpinner() {
        gradeTierList.clear()
        if (scoringObject == null) return // Haven't init scoring object yet
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
        db.addListenerForSingleValueEvent(object: FirebaseValueEventListener(TAG, "getScoringObject") {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                scoringObject = dataSnapshot.getValue(GpaScoring::class.java)
                updateScoringFeatures()
            }
        })
    }

    private fun getInstitution(institute: String) {
        val db = GpaCalcFirebaseUtils.getGpaDatabaseUser(userId).child(institute)
        db.keepSynced(true)
        db.addListenerForSingleValueEvent(object: FirebaseValueEventListener(TAG, "getInstitution") {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                selectedInstitution = dataSnapshot.getValue(GpaInstitution::class.java)
                supportActionBar?.subtitle = if (selectedInstitution == null) "An error occurred" else "${selectedInstitution?.name} | ${selectedInstitution?.semester!![selectedSemesterKey]?.name}"
                getScoringObject()
            }
        })
    }

    companion object {
        private const val TAG = "GpaCalcAddModule"
    }
}
