package com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.AddModuleActivity
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.GpaCalcFirebaseUtils
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.GpaRecyclerAdapter
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.MainViewActivity
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.objects.GpaInstitution
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.objects.GpaModule
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.objects.GpaRecycler
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.objects.GpaScoring
import com.itachi1706.cheesecakeutilities.R
import com.itachi1706.cheesecakeutilities.util.FirebaseValueEventListener
import com.itachi1706.cheesecakeutilities.util.LogHelper

/**
 * Semester List View
 */
class ModuleListFragment : BaseGpaFragment() {

    private val state = MainViewActivity.STATE_MODULE

    private val modules: ArrayList<GpaModule> = ArrayList()

    private var selectedInstitutionString: String? = null
    private var selectedInstitutionType: String? = null
    private var selectedSemesterKey: String? = null

    private var selectedInstitution: GpaInstitution? = null
    private var scoreObject: GpaScoring? = null

    override fun getLogTag(): String { return TAG }

    override fun getState(): Int { return state }

    override fun evaluateToCont(v: View): Boolean {
        selectedInstitutionString = arguments?.getString("selection")
        selectedSemesterKey = arguments?.getString("semester")
        if (selectedInstitutionString == null || selectedSemesterKey == null) {
            LogHelper.e(TAG, "Institution/Semester not selected!")
            Snackbar.make(v, "An error has occurred. (Institution/Semester not found)", Snackbar.LENGTH_LONG).show()
            callback?.goBack()
            return false
        }
        selectedInstitutionType = arguments?.getString("type")
        return true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = super.onCreateView(inflater, container, savedInstanceState)

        adapter.setOnClickListener(View.OnClickListener { view ->
            val viewHolder = view.tag as GpaRecyclerAdapter.GpaViewHolder
            val pos = viewHolder.adapterPosition
            val moduleSelected = modules[pos]
            // FUTURE_TODO: Maybe swap edit to a screen with all the module information
            startActivity(Intent(context, AddModuleActivity::class.java).apply {
                putExtra("userid", callback?.getUserId())
                putExtra("institute", selectedInstitutionString)
                putExtra("key", selectedSemesterKey)
                putExtra("editmode", moduleSelected.courseCode)
            })
        })

        adapter.setOnCreateContextMenuListener(View.OnCreateContextMenuListener { menu, view, _ ->
            // Get selected institution
            val viewHolder = view.tag as GpaRecyclerAdapter.GpaViewHolder
            if (!initContextSelectMode(viewHolder.adapterPosition)) return@OnCreateContextMenuListener // Do nothing
            menu.setHeaderTitle("${moduleContextSel!!.name} [${moduleContextSel!!.courseCode}]")
            activity?.menuInflater?.inflate(R.menu.context_menu_editdelete, menu)
        })

        // Get institution name to update title
        selectedInstitution = callback?.getInstitution()
        updateActionBar()
        return v
    }

    private var moduleContextSel: GpaModule? = null

    override fun onContextItemSelected(item: MenuItem): Boolean {
        LogHelper.d(TAG, "Context Item Selected")
        if (moduleContextSel == null) return false
        return when (item.itemId) {
            R.id.menu_edit -> edit(null)
            R.id.menu_delete -> delete(null)
            else -> super.onContextItemSelected(item)
        }
    }

    override fun initContextSelectMode(position: Int): Boolean {
        moduleContextSel = modules[position]
        if (moduleContextSel == null) return false
        return true
    }

    override fun edit(position: Int?): Boolean {
        if (position != null) if (!initContextSelectMode(position)) return false

        startActivity(Intent(context, AddModuleActivity::class.java).apply {
            putExtra("userid", callback?.getUserId())
            putExtra("institute", selectedInstitutionString)
            putExtra("key", selectedSemesterKey)
            putExtra("editmode", moduleContextSel!!.courseCode)
        })
        return true
    }

    override fun delete(position: Int?): Boolean {
        if (position != null) if (!initContextSelectMode(position)) return false

        val moduleToDelete = moduleContextSel!!.copy()
        val data = getPath() ?: return false
        data.child(moduleToDelete.courseCode).removeValue()
        Snackbar.make(view!!, "Module Deleted", Snackbar.LENGTH_LONG).setAction("Undo") { v ->
            data.child(moduleToDelete.courseCode).setValue(moduleToDelete)
            Snackbar.make(v, "Delete undone", Snackbar.LENGTH_SHORT).show()
        }.show()
        return true
    }

    private fun getPath(): DatabaseReference? {
        return callback?.getUserData()?.child(selectedInstitutionString!!)?.child(GpaCalcFirebaseUtils.FB_REC_SEMESTER)
                ?.child(selectedSemesterKey!!)?.child(GpaCalcFirebaseUtils.FB_REC_MODULE)
    }

    override fun onStart() {
        super.onStart()
        if (selectedInstitutionString == null) return // Don't do anything, an error had occurred already

        scoreObject = callback?.getScoreMap()!![selectedInstitutionType]
        updateActionBar()
        LogHelper.i(TAG, "Registering Module Firebase DB Listener")
        listener = getPath()?.addValueEventListener(object: FirebaseValueEventListener(TAG, "loadModuleList"){
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (callback?.getCurrentState() != state) return
                LogHelper.i(TAG, "Processing updated modules...")
                modules.clear()
                if (!dataSnapshot.hasChildren()) return
                dataSnapshot.children.forEach {
                    modules.add(it.getValue(GpaModule::class.java)!!)
                }
                LogHelper.i(TAG, "Number of Modules ($selectedInstitutionString:$selectedSemesterKey): ${modules.size}")
                modules.sortBy { it.courseCode }
                modulesProcessAndUpdate()
            }
        })
    }

    private fun modulesProcessAndUpdate() {
        val list: ArrayList<GpaRecycler> = ArrayList()
        modules.forEach {
            val score = when {
                it.gradeTier == -1 -> "-"
                scoreObject == null -> "???"
                it.passFail ->  scoreObject!!.passtier!![it.gradeTier].name
                else -> scoreObject!!.gradetier[it.gradeTier].name
            }
            val finalGrade = if (it.gradeTier == -1) "???" else if (!it.passFail) scoreObject!!.gradetier[it.gradeTier].value.toString() else
                if (scoreObject!!.passtier!![it.gradeTier].value > 0) "P" else "F"
            list.add(GpaRecycler(it.name, if (scoreObject?.type == "gpa") "${it.courseCode} | Credits: ${it.credits} ${selectedInstitution?.creditName}" else it.courseCode,
                    grade=score, gradeColor = GpaCalcFirebaseUtils.getGpaColor(finalGrade, scoreObject, context), color = it.color))
        }
        updateActionBar()
        adapter.update(list)
        adapter.notifyDataSetChanged()
    }

    private fun updateActionBar() {
        LogHelper.d(TAG, "updateActionBar()")
        var subtitle: String? = null
        var title: String? = null
        if (scoreObject != null) {
            subtitle = if (selectedInstitution != null) "${selectedInstitution!!.semester[selectedSemesterKey]?.name} | " else "Unknown Semester | "
            subtitle += if (scoreObject!!.type == "count") "Score" else "GPA"
            subtitle += ": ${if (selectedInstitution != null) selectedInstitution!!.semester[selectedSemesterKey]?.gpa else "Unknown"}"
        }
        if (selectedInstitution != null) title = "${selectedInstitution!!.name} (${selectedInstitution!!.shortName})"
        callback?.updateActionBar(title, subtitle)
    }

    companion object {
        private const val TAG = "GpaCalcModuleList"
    }
}
