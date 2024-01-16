package com.itachi1706.cheesecakeutilities.modules.gpaCalculator

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.itachi1706.cheesecakeutilities.BaseModuleActivity
import com.itachi1706.cheesecakeutilities.R
import com.itachi1706.cheesecakeutilities.databinding.ActivityGpaCalculatorMainBinding
import com.itachi1706.cheesecakeutilities.modules.gpaCalculator.fragment.InstitutionListFragment
import com.itachi1706.cheesecakeutilities.modules.gpaCalculator.fragment.ModuleListFragment
import com.itachi1706.cheesecakeutilities.modules.gpaCalculator.fragment.SemesterListFragment
import com.itachi1706.cheesecakeutilities.modules.gpaCalculator.interfaces.GpaCalcCallback
import com.itachi1706.cheesecakeutilities.modules.gpaCalculator.interfaces.StateSwitchListener
import com.itachi1706.cheesecakeutilities.modules.gpaCalculator.objects.GpaInstitution
import com.itachi1706.cheesecakeutilities.modules.gpaCalculator.objects.GpaScoring
import com.itachi1706.cheesecakeutilities.modules.gpaCalculator.objects.GpaSemester
import com.itachi1706.cheesecakeutilities.modules.vehicleMileageTracker.VehMileageFirebaseUtils
import com.itachi1706.cheesecakeutilities.util.FirebaseValueEventListener
import com.itachi1706.helperlib.helpers.LogHelper

class MainViewActivity(override val helpDescription: String = "A utility for handling keeping track of scores such as Grade Point Averages (GPA)") : BaseModuleActivity(), StateSwitchListener {

    private var currentState: Int = STATE_INSTITUTION
    private lateinit var userData: DatabaseReference
    private lateinit var userId: String
    private var defaultActionBarText: String? = null

    private var updateScoreOnCreate = false

    private val scoring: HashMap<String, GpaScoring> = HashMap()

    private lateinit var binding: ActivityGpaCalculatorMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGpaCalculatorMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        defaultActionBarText = supportActionBar?.title.toString()

        userId = VehMileageFirebaseUtils.getFirebaseUIDFromSharedPref(PreferenceManager.getDefaultSharedPreferences(this)) ?: "nien"
        if (userId.equals("nien", ignoreCase = true)) {
            // Fail, return to login activity
            Toast.makeText(this, "Invalid Login Token", Toast.LENGTH_SHORT).show()
            val logoutIntent = Intent(this, InitActivity::class.java)
            logoutIntent.putExtra("logout", true)
            startActivity(logoutIntent)
            finish()
            return
        }

        binding.fab.drawable.setColorFilter(ResourcesCompat.getColor(resources, R.color.white, null), PorterDuff.Mode.SRC_ATOP)
        binding.fab.setOnClickListener { view ->
            addFabAction(view)
        }

        // Init Firebase
        userData = GpaCalcFirebaseUtils.getGpaDatabaseUser(userId)
        updateScoring()
        updateScoreOnCreate = true

        // Replace existing fragment
        val tmp = InstitutionListFragment().apply {
            val bundle = Bundle().apply { putBoolean("boot", true) }
            arguments = bundle
        }
        supportFragmentManager.beginTransaction().replace(R.id.fragment, tmp).commit() // Force replace fragment?
    }

    override fun onResume() {
        super.onResume()
        if (!updateScoreOnCreate) updateScoring() else updateScoreOnCreate = false
    }

    private fun updateScoring(callback: GpaCalcCallback? = null) {
        LogHelper.i(TAG, "Updating Scoring Objects Map")
        val db = GpaCalcFirebaseUtils.getGpaDatabase().child(GpaCalcFirebaseUtils.FB_REC_SCORING)
        db.keepSynced(true)
        db.addListenerForSingleValueEvent(object: FirebaseValueEventListener(TAG, "updateScoring") {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                scoring.clear()
                if (dataSnapshot.hasChildren()) {
                    dataSnapshot.children.forEach{
                        scoring[it.key!!] = it.getValue(GpaScoring::class.java)!!
                    }
                }
                callback?.onCallback()
            }
        })
    }

    override fun updateSelectedInstitution() {
        if (selInstitute == null) {
            LogHelper.e(TAG, "Nothing to update in institution")
            return
        }
        LogHelper.i(TAG, "Updating Selected Institution")
        val key = selInstitute!!.shortName
        val db = GpaCalcFirebaseUtils.getGpaDatabaseUser(userId).child(key)
        db.keepSynced(true)
        db.addListenerForSingleValueEvent(object: FirebaseValueEventListener(TAG, "updateSelectedInstitution") {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                selInstitute = dataSnapshot.getValue(GpaInstitution::class.java)
            }
        })
    }

    override fun getCurrentState(): Int {
        return currentState
    }

    private fun addFabAction(view: View) {
        when (currentState) {
            STATE_INSTITUTION -> startActivity(Intent(this, AddInstitutionActivity::class.java).apply { putExtra("userid", userId) })
            STATE_SEMESTER -> startActivity(Intent(this, AddSemesterActivity::class.java).apply {
                putExtra("userid", userId)
                putExtra("institute", selInstitute?.shortName)
            })
            STATE_MODULE -> startActivity(Intent(this, AddModuleActivity::class.java).apply {
                putExtra("userid", userId)
                putExtra("institute", selInstitute?.shortName)
                putExtra("key", selSemesterKey)
            })
            else -> Snackbar.make(view, "Unimplemetned", Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu_gpa_calculator_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.score_system -> startActivity(Intent(this, ScoringActivity::class.java))
            R.id.logout -> {
                startActivity(Intent(this, InitActivity::class.java).apply { putExtra("logout", true) })
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private var selInstitute: GpaInstitution? = null
    private var selSemester: GpaSemester? = null
    private var selSemesterKey: String? = null

    override fun onStateSwitch(newState: Int) {
        LogHelper.d(TAG, "State Switch: $newState")
        currentState = newState
        // Special state changes
        when (newState) {
            STATE_INSTITUTION -> selInstitute = null
            STATE_SEMESTER -> selSemester = null
        }
    }

    override fun getUserData(): DatabaseReference {
        return userData
    }

    override fun getUserId(): String {
        return userId
    }

    override fun selectInstitute(instituteSelected: GpaInstitution) {
        if (currentState != STATE_INSTITUTION) LogHelper.e(TAG, "Invalid State!!! Expected 0 but got $currentState")
        selInstitute = instituteSelected

        startFragment(SemesterListFragment(), Bundle().apply {
            putString("selection", instituteSelected.shortName)
            putString("type", instituteSelected.type)
        }, "semester-view")
    }

    override fun selectSemester(semester: GpaSemester, key: String) {
        if (currentState != STATE_SEMESTER) LogHelper.e(TAG, "Invalid State!!! Expected 1 but got $currentState")
        selSemester = semester
        selSemesterKey = key

        startFragment(ModuleListFragment(), Bundle().apply {
            putString("selection", selInstitute?.shortName)
            putString("type", selInstitute?.type)
            putString("semester", selSemesterKey)
        }, "module-view")
    }

    private fun startFragment(fragClass: Fragment, bundle: Bundle, tag: String) {
        val frag: Fragment = fragClass
        frag.arguments = bundle
        supportFragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
                .replace(R.id.fragment, frag).addToBackStack(tag)
                .commit()
    }

    override fun getInstitution(): GpaInstitution? {
        return selInstitute
    }

    override fun getSemester(): GpaSemester? {
        return selSemester
    }

    override fun getScoreMap(): HashMap<String, GpaScoring> {
        return scoring
    }

    override fun goBack() {
        onBackPressed()
    }

    override fun updateActionBar(title: String?, subtitle: String?) {
        if (defaultActionBarText == null) return // Not initialized yet
        if (title == null && defaultActionBarText!!.isNotEmpty()) supportActionBar?.title = defaultActionBarText
        else if (title != null) supportActionBar?.title = title
        supportActionBar?.subtitle = subtitle
    }

    override fun updateScoreMap(callback: GpaCalcCallback) {
        LogHelper.i(TAG, "Manually updating Scoring Objects")
        updateScoring(callback)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) supportFragmentManager.popBackStack()
        else super.onBackPressed()
    }

    companion object {
        const val STATE_INSTITUTION = 0 // Institution selection screen
        const val STATE_SEMESTER = 1 // Semester selection screen
        const val STATE_MODULE = 2 // Module selection screen
        private const val TAG = "GpaCalcMain"
    }

}
