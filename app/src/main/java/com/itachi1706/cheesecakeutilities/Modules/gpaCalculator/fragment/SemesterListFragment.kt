package com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.fragment

import android.content.Context
import android.content.Intent
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
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.AddSemesterActivity
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.GpaCalcFirebaseUtils
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.MainViewActivity
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.interfaces.StateSwitchListener
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.objects.GpaInstitution
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.objects.GpaScoring
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.objects.GpaSemester
import com.itachi1706.cheesecakeutilities.R
import com.itachi1706.cheesecakeutilities.RecyclerAdapters.DualLineStringRecyclerAdapter
import com.itachi1706.cheesecakeutilities.Util.FirebaseUtils
import com.itachi1706.cheesecakeutilities.Util.LogHelper
import com.itachi1706.cheesecakeutilities.objects.DualLineString

/**
 * Semester List View
 */
class SemesterListFragment : Fragment() {

    private var callback: StateSwitchListener? = null
    private val state = MainViewActivity.STATE_SEMESTER

    private val semesters: ArrayList<GpaSemester> = ArrayList()
    private val semesterKeys: ArrayList<String> = ArrayList()

    private lateinit var adapter: DualLineStringRecyclerAdapter
    private var selectedInstitutionString: String? = null
    private var selectedInstitutionType: String? = null

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
        if (selectedInstitutionString == null) {
            LogHelper.e(TAG, "Institution not selected!")
            Snackbar.make(v, "An error has occurred. (Institution not found)", Snackbar.LENGTH_LONG).show()
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
            val semesterSelected = semesters[pos]
            val selectedSemesterKey = semesterKeys[pos]
            callback?.selectSemester(semesterSelected, selectedSemesterKey)
        }

        adapter.setOnCreateContextMenuListener { menu, view, _ ->
            // Get selected institution
            val viewHolder = view.tag as DualLineStringRecyclerAdapter.StringViewHolder
            semesterContextSel = semesters[viewHolder.adapterPosition]
            semesterContextKeySel = semesterKeys[viewHolder.adapterPosition]
            if (semesterContextSel == null) return@setOnCreateContextMenuListener // Do nothing
            menu.setHeaderTitle(semesterContextSel!!.name)
            activity?.menuInflater?.inflate(R.menu.context_menu_editdelete, menu)
        }

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
        when (item.itemId) {
            R.id.menu_edit -> startActivity(Intent(context, AddSemesterActivity::class.java).apply {
                putExtra("userid", callback?.getUserId())
                putExtra("institute", selectedInstitutionString)
                putExtra("editmode", semesterContextKeySel)
            })
            R.id.menu_delete -> {
                val semesterToDelete = semesterContextSel!!.copy()
                val data = callback?.getUserData()?.child(selectedInstitutionString!!)?.child(GpaCalcFirebaseUtils.FB_REC_SEMESTER) ?: return false
                data.child(semesterContextKeySel!!).removeValue()
                Snackbar.make(view!!, "Semester Deleted", Snackbar.LENGTH_LONG).setAction("Undo") { v ->
                    data.child(semesterContextKeySel!!).setValue(semesterToDelete)
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
        LogHelper.i(TAG, "Registering Semester Firebase DB Listener")
        listener = callback?.getUserData()?.child(selectedInstitutionString!!)?.child(GpaCalcFirebaseUtils.FB_REC_SEMESTER)?.addValueEventListener(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                LogHelper.w(TAG, "loadSemestersList:onCancelled", p0.toException())
            }

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

    override fun onStop() {
        super.onStop()
        if (listener != null) {
            FirebaseUtils.removeListener(listener!!)
            Log.i(TAG, "Firebase Listener Unregisted")
            listener = null
        }
    }

    private fun semestersProcessAndUpdate() {
        val list: ArrayList<DualLineString> = ArrayList()
        semesters.forEach {
            list.add(DualLineString(it.name, (if (scoreObject?.type == "count") "Score" else "GPA") + ": ${it.gpa}"))
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
