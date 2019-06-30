package com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.GpaCalcFirebaseUtils
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.MainViewActivity
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.interfaces.StateSwitchListener
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.objects.GpaInstitution
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.objects.GpaModule
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.objects.GpaScoring
import com.itachi1706.cheesecakeutilities.R
import com.itachi1706.cheesecakeutilities.RecyclerAdapters.DualLineStringRecyclerAdapter
import com.itachi1706.cheesecakeutilities.Util.FirebaseUtils
import com.itachi1706.cheesecakeutilities.Util.LogHelper
import com.itachi1706.cheesecakeutilities.objects.DualLineString

/**
 * Semester List View
 */
class ModuleListFragment : Fragment() {

    private var callback: StateSwitchListener? = null
    private val state = MainViewActivity.STATE_MODULE

    private val modules: ArrayList<GpaModule> = ArrayList()

    private lateinit var adapter: DualLineStringRecyclerAdapter
    private var selectedInstitutionString: String? = null
    private var selectedInstitutionType: String? = null
    private var selectedSemesterKey: String? = null

    private var selectedInstitution: GpaInstitution? = null
    private var scoreObject: GpaScoring? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainViewActivity) {
            callback = context
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_recycler_view, container, false)
        val recyclerView = v.findViewById<RecyclerView>(R.id.main_menu_recycler_view)

        selectedInstitutionString = arguments?.getString("selection")
        selectedSemesterKey = arguments?.getString("semester")
        if (selectedInstitutionString == null || selectedSemesterKey == null) {
            LogHelper.e(TAG, "Institution/Semester not selected!")
            Snackbar.make(v, "An error has occurred. (Institution/Semester not found)", Snackbar.LENGTH_LONG).show()
            callback?.goBack()
            return v
        }
        selectedInstitutionType = arguments?.getString("type")

        recyclerView.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()

        // Update layout
        adapter = DualLineStringRecyclerAdapter(arrayListOf(), false)
        recyclerView.adapter = adapter

        callback?.onStateSwitch(state)

        adapter.setOnClickListener { view ->
            val viewHolder = view.tag as DualLineStringRecyclerAdapter.StringViewHolder
            val pos = viewHolder.adapterPosition
            val moduleSelected = modules[pos]
            // TODO: Switch fragment with the selected module
            Snackbar.make(view, "Unimplemented. Selected: ${moduleSelected.name}", Snackbar.LENGTH_LONG).show()
        }

        adapter.setOnCreateContextMenuListener { menu, view, _ ->
            // Get selected institution
            val viewHolder = view.tag as DualLineStringRecyclerAdapter.StringViewHolder
            moduleContextSel = modules[viewHolder.adapterPosition]
            if (moduleContextSel == null) return@setOnCreateContextMenuListener // Do nothing
            menu.setHeaderTitle("${moduleContextSel!!.name} [${moduleContextSel!!.courseCode}]")
            activity?.menuInflater?.inflate(R.menu.context_menu_editdelete, menu)
        }

        // Get institution name to update title
        selectedInstitution = callback?.getInstitution()
        updateActionBar()
        return v
    }

    private var moduleContextSel: GpaModule? = null

    override fun onContextItemSelected(item: MenuItem): Boolean {
        LogHelper.d(TAG, "Context Item Selected")
        if (moduleContextSel == null) return false
        when (item.itemId) {
            R.id.menu_edit -> TODO("Edit Command")
            R.id.menu_delete -> {
                val moduleToDelete = moduleContextSel!!.copy()
                val data = callback?.getUserData()?.child(selectedInstitutionString!!)?.child(GpaCalcFirebaseUtils.FB_REC_SEMESTER)
                        ?.child(selectedSemesterKey!!)?.child(GpaCalcFirebaseUtils.FB_REC_MODULE) ?: return false
                data.child(moduleToDelete.courseCode).removeValue()
                Snackbar.make(view!!, "Module Deleted", Snackbar.LENGTH_LONG).setAction("Undo") { v ->
                    data.child(moduleToDelete.courseCode).setValue(moduleToDelete)
                    Snackbar.make(v, "Delete undone", Snackbar.LENGTH_SHORT).show()
                }.show()
            }
            else -> return super.onContextItemSelected(item)
        }
        return true
    }

    private var listener: ValueEventListener? = null
    override fun onStart() {
        super.onStart()
        if (selectedInstitutionString == null) return // Don't do anything, an error had occurred already
        if (listener != null) {
            FirebaseUtils.removeListener(listener!!)
            listener = null
            LogHelper.e(TAG, "Firebase DB Listeners exists when it should not have, terminating it forcibly")
        }

        scoreObject = callback?.getScoreMap()!![selectedInstitutionType]
        updateActionBar()
        LogHelper.i(TAG, "Registering Module Firebase DB Listener")
        listener = callback?.getUserData()?.child(selectedInstitutionString!!)?.child(GpaCalcFirebaseUtils.FB_REC_SEMESTER)?.child(selectedSemesterKey!!)?.
                child(GpaCalcFirebaseUtils.FB_REC_MODULE)?.addValueEventListener(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                LogHelper.w(TAG, "loadModuleList:onCancelled", p0.toException())
            }

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

    override fun onStop() {
        super.onStop()
        if (listener != null) {
            FirebaseUtils.removeListener(listener!!)
            Log.i(TAG, "Firebase Listener Unregisted")
            listener = null
        }
    }

    private fun modulesProcessAndUpdate() {
        val list: ArrayList<DualLineString> = ArrayList()
        modules.forEach {
            var sub = if (scoreObject?.type == "gpa") "Credits: ${it.credits} ${selectedInstitution?.creditName} | " else ""
            sub += "Grade: "
            sub += when {
                it.gradeTier == -1 -> "-"
                scoreObject == null -> "Unknown"
                it.passFail ->  scoreObject!!.passtier!![it.gradeTier].name
                else -> scoreObject!!.gradetier[it.gradeTier].name
            }
            list.add(DualLineString("${it.name} [${it.courseCode}]", sub))
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