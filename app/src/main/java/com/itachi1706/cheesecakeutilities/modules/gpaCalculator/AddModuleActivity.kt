package com.itachi1706.cheesecakeutilities.modules.gpaCalculator

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.itachi1706.cheesecakeutilities.R
import com.itachi1706.cheesecakeutilities.databinding.ActivityGpaCalculatorAddModuleBinding
import com.itachi1706.cheesecakeutilities.extlibs.com.thebluealliance.spectrum.SpectrumPalette
import com.itachi1706.cheesecakeutilities.modules.gpaCalculator.objects.GpaInstitution
import com.itachi1706.cheesecakeutilities.modules.gpaCalculator.objects.GpaModule
import com.itachi1706.cheesecakeutilities.modules.gpaCalculator.objects.GpaScoring
import com.itachi1706.cheesecakeutilities.util.FirebaseValueEventListener
import com.itachi1706.helperlib.helpers.LogHelper
import java.util.Locale

class AddModuleActivity : AddActivityBase() {

    private var selectedInstitution: GpaInstitution? = null
    private var scoringObject: GpaScoring? = null
    private lateinit var selectedSemesterKey: String
    private lateinit var instituteString: String

    private var colorSelected: Int = 0

    private lateinit var binding: ActivityGpaCalculatorAddModuleBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGpaCalculatorAddModuleBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

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
        binding.gpacalcAdd.setOnClickListener { v ->
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

        binding.cbPassFail.setOnCheckedChangeListener { _, _ -> updateGradeTierSpinner() }
        binding.moduleColorSelector.setOnColorSelectedListener(object: SpectrumPalette.OnColorSelectedListener {
            override fun onColorSelected(color: Int) {
                colorSelected = color
                LogHelper.d(TAG, "Color Selected: #${Integer.toHexString(color).toUpperCase(Locale.US)}")
            }
        })
    }

    private fun randomizeColor() {
        val typeArrayTmp = resources.obtainTypedArray(R.array.module_colors)
        val colorArray: ArrayList<Int> = ArrayList()
        for (i in 0 until typeArrayTmp.length()) {
            colorArray.add(typeArrayTmp.getColor(i, 0))
        }
        colorSelected = colorArray.shuffled().take(1)[0]
        binding.moduleColorSelector.setSelectedColor(colorSelected)
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
                binding.etName.setText(module?.name)
                binding.etCourseCode.setText(module?.courseCode)
                binding.etCredits.setText(module?.credits.toString())
                binding.cbPassFail.isChecked = module?.passFail ?: false
                binding.gpacalcAdd.text = "Edit Module"
                if (module?.color != 0) binding.moduleColorSelector.setSelectedColor(module?.color!!)
                else randomizeColor()
                supportActionBar?.title = "Edit a Module"
                supportActionBar?.subtitle = "${binding.etName.text.toString()} [${binding.etCourseCode.text.toString()}]"
            }

        })
    }

    override fun validate(): Any {
        val name = binding.etName.text.toString()
        val courseCode = binding.etCourseCode.text.toString()
        val creditsStr = binding.etCredits.text.toString()
        val credits = if (creditsStr.isEmpty()) -1 else creditsStr.toInt()
        val passFail = binding.cbPassFail.isChecked
        val grade = binding.spinnerGpaGrade.selectedItemPosition - 1

        binding.tilEtName.error = "Required field"
        binding.tilEtCourseCode.error = "Required field"

        binding.tilEtName.isErrorEnabled = name.isEmpty()
        binding.tilEtCourseCode.isErrorEnabled = courseCode.isEmpty()

        // Make sure course code is unique
        if (selectedInstitution == null) return "Invalid institution error"
        if (selectedInstitution!!.semester[selectedSemesterKey]?.modules?.contains(courseCode)!! && module == null) {
            binding.tilEtCourseCode.error = "Module Already Exists"
            binding.tilEtCourseCode.isErrorEnabled = true
        }
        
        if (binding.tilEtName.isErrorEnabled || binding.tilEtCourseCode.isErrorEnabled) return "Please resolve the errors before continuing"
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
        if (scoringObject?.passtier == null) binding.cbPassFail.isChecked = false
        binding.cbPassFail.isEnabled = scoringObject?.passtier != null
        binding.tilEtCredits.visibility = if (scoringObject?.type == "count") View.GONE else View.VISIBLE
        updateGradeTierSpinner()
    }

    private val gradeTierList: LinkedHashMap<String, GpaScoring.GpaTier?> = LinkedHashMap()

    private fun updateGradeTierSpinner() {
        gradeTierList.clear()
        if (scoringObject == null) return // Haven't init scoring object yet
        gradeTierList["- (Not Graded)"] = null
        val tier = if (binding.cbPassFail.isChecked && scoringObject?.passtier != null) scoringObject?.passtier!! else scoringObject?.gradetier!!
        tier.forEach {
            gradeTierList.put("${it.name} (${it.desc})".replace(" (No description)", ""), it)
        }
        //gradeTierList.putAll(tier.map { i -> "${i.name} (${i.desc})".replace(" (No description)", "") }, tier.toList())
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, gradeTierList.keys.toList())
        binding.spinnerGpaGrade.adapter = adapter
    }

    private fun getScoringObject() {
        LogHelper.i(TAG, "Retrieving Scoring Object for this institution")
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
