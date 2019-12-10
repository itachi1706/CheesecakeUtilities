package com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.AddSemesterActivity
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.GpaCalcFirebaseUtils
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.GpaRecyclerAdapter
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.MainViewActivity
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.objects.GpaInstitution
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.objects.GpaRecycler
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.objects.GpaScoring
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.objects.GpaSemester
import com.itachi1706.cheesecakeutilities.R
import com.itachi1706.cheesecakeutilities.util.FirebaseValueEventListener
import com.itachi1706.cheesecakeutilities.util.LogHelper

/**
 * Semester List View
 */
class SemesterListFragment : BaseGpaFragment() {

    private val state = MainViewActivity.STATE_SEMESTER

    private val semesters: ArrayList<GpaSemester> = ArrayList()
    private val semesterKeys: ArrayList<String> = ArrayList()

    private var selectedInstitutionString: String? = null
    private var selectedInstitutionType: String? = null

    private var selectedInstitution: GpaInstitution? = null
    private var scoreObject: GpaScoring? = null

    override fun getLogTag(): String { return TAG }

    override fun getState(): Int { return state }

    override fun evaluateToCont(v: View): Boolean {
        selectedInstitutionString = arguments?.getString("selection")
        if (selectedInstitutionString == null) {
            LogHelper.e(TAG, "Institution not selected!")
            Snackbar.make(v, "An error has occurred. (Institution not found)", Snackbar.LENGTH_LONG).show()
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
            val semesterSelected = semesters[pos]
            val selectedSemesterKey = semesterKeys[pos]
            callback?.selectSemester(semesterSelected, selectedSemesterKey)
        })

        adapter.setOnCreateContextMenuListener(View.OnCreateContextMenuListener { menu, view, _ ->
            // Get selected institution
            val viewHolder = view.tag as GpaRecyclerAdapter.GpaViewHolder
            if (!initContextSelectMode(viewHolder.adapterPosition)) return@OnCreateContextMenuListener // Do nothing
            menu.setHeaderTitle(semesterContextSel!!.name)
            activity?.menuInflater?.inflate(R.menu.context_menu_editdelete, menu)
        })

        // Get institution name to update title
        selectedInstitution = callback?.getInstitution()
        updateActionBar()
        return v
    }

    private var semesterContextKeySel: String? = null
    private var semesterContextSel: GpaSemester? = null

    override fun onContextItemSelected(item: MenuItem): Boolean {
        LogHelper.d(TAG, "Context Item Selected")
        if (semesterContextKeySel == null || semesterContextSel == null) return false
        return when (item.itemId) {
            R.id.menu_edit -> edit(null)
            R.id.menu_delete -> delete(null)
            else -> super.onContextItemSelected(item)
        }
    }

    override fun initContextSelectMode(position: Int): Boolean {
        semesterContextKeySel = semesterKeys[position]
        semesterContextSel = semesters[position]
        if (semesterContextSel == null) return false
        return true
    }

    override fun edit(position: Int?): Boolean {
        if (position != null) if (!initContextSelectMode(position)) return false

        startActivity(Intent(context, AddSemesterActivity::class.java).apply {
            putExtra("userid", callback?.getUserId())
            putExtra("institute", selectedInstitutionString)
            putExtra("editmode", semesterContextKeySel)
        })
        return true
    }

    override fun delete(position: Int?): Boolean {
        if (position != null) if (!initContextSelectMode(position)) return false

        val semesterToDelete = semesterContextSel!!.copy()
        val data = callback?.getUserData()?.child(selectedInstitutionString!!)?.child(GpaCalcFirebaseUtils.FB_REC_SEMESTER) ?: return false
        data.child(semesterContextKeySel!!).removeValue()
        Snackbar.make(view!!, "Semester Deleted", Snackbar.LENGTH_LONG).setAction("Undo") { v ->
            data.child(semesterContextKeySel!!).setValue(semesterToDelete)
            Snackbar.make(v, "Delete undone", Snackbar.LENGTH_SHORT).show()
        }.show()
        return true
    }

    override fun onStart() {
        super.onStart()
        if (selectedInstitutionString == null) return // Don't do anything, an error had occurred already

        scoreObject = callback?.getScoreMap()!![selectedInstitutionType]
        updateActionBar()
        LogHelper.i(TAG, "Registering Semester Firebase DB Listener")
        listener = callback?.getUserData()?.child(selectedInstitutionString!!)?.child(GpaCalcFirebaseUtils.FB_REC_SEMESTER)?.addValueEventListener(object: FirebaseValueEventListener(TAG, "loadSemestersList") {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (callback?.getCurrentState() != state) return
                LogHelper.i(TAG, "Processing updated semesters...")
                val tmp: HashMap<String, GpaSemester> = HashMap()
                if (!dataSnapshot.hasChildren()) return
                dataSnapshot.children.forEach { tmp[it.key!!] = it.getValue(GpaSemester::class.java)!! }
                val sortedTmp = tmp.toList().sortedBy { it.second.order }.toMap()
                LogHelper.i(TAG, "Number of Semesters in Institution: ${sortedTmp.size}")
                semesterKeys.clear()
                semesters.clear()
                semesters.addAll(sortedTmp.values.toList())
                semesterKeys.addAll(sortedTmp.keys.toList())
                semestersProcessAndUpdate()
            }
        })
    }

    private fun semestersProcessAndUpdate() {
        val list: ArrayList<GpaRecycler> = ArrayList()
        semesters.forEach {
            list.add(GpaRecycler(it.name, getTimestampString(it.startTimestamp, it.endTimestamp), grade = if (it.gpa == "Unknown") "???" else it.gpa,
                    gradeColor = GpaCalcFirebaseUtils.getGpaColor(it.gpa, scoreObject, context)))
        }
        updateActionBar()
        adapter.update(list)
        adapter.notifyDataSetChanged()
        callback?.updateSelectedInstitution()

    }

    private fun updateActionBar() {
        LogHelper.d(TAG, "updateActionBar()")
        var subtitle: String? = null
        var title: String? = null
        if (scoreObject != null) {
            subtitle = if (scoreObject!!.type == "count") "Score" else "GPA"
            subtitle += ": ${if (selectedInstitution != null) selectedInstitution!!.gpa else "Unknown"}"
        }
        if (selectedInstitution != null) title = "${selectedInstitution!!.name} (${selectedInstitution!!.shortName})"
        callback?.updateActionBar(title, subtitle)
    }

    companion object {
        private const val TAG = "GpaCalcSemesterList"
    }
}
