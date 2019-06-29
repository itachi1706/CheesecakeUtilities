package com.itachi1706.cheesecakeutilities.Modules.gpaCalculator

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.itachi1706.cheesecakeutilities.BaseModuleActivity
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.fragment.InstitutionListFragment
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.fragment.ModuleListFragment
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.fragment.SemesterListFragment
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.interfaces.GpaCalcCallback
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.interfaces.StateSwitchListener
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.objects.GpaInstitution
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.objects.GpaScoring
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.objects.GpaSemester
import com.itachi1706.cheesecakeutilities.R
import com.itachi1706.cheesecakeutilities.Util.LogHelper
import kotlinx.android.synthetic.main.activity_gpa_calculator_main.*

class MainViewActivity(override val helpDescription: String = "A utility for handling keeping track of scores such as Grade Point Averages (GPA)") : BaseModuleActivity(), StateSwitchListener {

    private var currentState: Int = STATE_INSTITUTION
    private lateinit var userData: DatabaseReference
    private lateinit var userId: String
    private var defaultActionBarText: String? = null

    private var updateScoreOnCreate = false

    private val scoring: HashMap<String, GpaScoring> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gpa_calculator_main)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        defaultActionBarText = supportActionBar?.title.toString()

        userId = PreferenceManager.getDefaultSharedPreferences(this).getString("firebase_uid", "nien") ?: "nien"
        if (userId.equals("nien", ignoreCase = true)) {
            // Fail, return to login activity
            Toast.makeText(this, "Invalid Login Token", Toast.LENGTH_SHORT).show()
            val logoutIntent = Intent(this, InitActivity::class.java)
            logoutIntent.putExtra("logout", true)
            startActivity(logoutIntent)
            finish()
            return
        }

        fab.drawable.setColorFilter(ResourcesCompat.getColor(resources, R.color.white, null), PorterDuff.Mode.SRC_ATOP)
        fab.setOnClickListener { view ->
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
        Log.i(TAG, "Updating Scoring Objects Map")
        val db = GpaCalcFirebaseUtils.getGpaDatabase().child(GpaCalcFirebaseUtils.FB_REC_SCORING)
        db.keepSynced(true)
        db.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                LogHelper.w(TAG, "updateScoring:onCancelled", p0.toException())
            }

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

    private fun addFabAction(view: View) {
        when (currentState) {
            STATE_INSTITUTION -> startActivity(Intent(this, AddInstitutionActivity::class.java).apply { putExtra("userid", userId) })
            STATE_SEMESTER -> startActivity(Intent(this, AddSemesterActivity::class.java).apply {
                putExtra("userid", userId)
                putExtra("institute", selInstitute?.shortName)
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

        startFragment(ModuleListFragment(), Bundle().apply {
            putString("selection", selInstitute?.shortName)
            putString("type", selInstitute?.type)
            putString("semester", key)
        }, "module-view")
    }

    private fun startFragment(fragClass: Fragment, bundle: Bundle, tag: String) {
        val frag: Fragment = fragClass
        frag.arguments = bundle
        supportFragmentManager.beginTransaction().replace(R.id.fragment, frag)
                .addToBackStack(tag)
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
        Log.i(TAG, "Manually updating Scoring Objects")
        updateScoring(callback)
    }

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
