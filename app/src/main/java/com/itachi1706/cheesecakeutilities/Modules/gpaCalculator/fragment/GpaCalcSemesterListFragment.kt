package com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
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
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.GpaCalculatorFirebaseUtils
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.GpaCalculatorMainActivity
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.`interface`.StateSwitchListener
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
class GpaCalcSemesterListFragment : Fragment() {

    private var callback: StateSwitchListener? = null
    private val state = GpaCalculatorMainActivity.STATE_SEMESTER

    private val semesters: ArrayList<GpaSemester> = ArrayList()

    private lateinit var adapter: DualLineStringRecyclerAdapter
    private var selectedInstitutionString: String? = null
    private var selectedInstitutionType: String? = null

    private var selectedInstitution: GpaInstitution? = null
    private var scoreObject: GpaScoring? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is GpaCalculatorMainActivity) {
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
            // TODO: Switch fragment with the selected semester
            Snackbar.make(view, "Unimplemented. Selected: ${semesterSelected.name}", Snackbar.LENGTH_LONG).show()
        }

        // Get institution name to update title
        callback?.getUserData()?.child(selectedInstitutionString!!)?.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                LogHelper.w(TAG, "getInstitution:onCancelled", p0.toException())
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                selectedInstitution = dataSnapshot.getValue(GpaInstitution::class.java)
                updateActionBar()
            }
        })
        return v
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
        LogHelper.i(TAG, "Registering Semester Firebase DB Listeners")
        listener = callback?.getUserData()?.child(selectedInstitutionString!!)?.child(GpaCalculatorFirebaseUtils.FB_REC_SEMESTER)?.addValueEventListener(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                LogHelper.w(TAG, "loadSemestersList:onCancelled", p0.toException())
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                LogHelper.i(TAG, "Processing updated semesters...")
                semesters.clear()
                if (!dataSnapshot.hasChildren()) return
                dataSnapshot.children.forEach {
                    semesters.add(it.getValue(GpaSemester::class.java)!!)
                }
                LogHelper.i(TAG, "Number of Semesters in Institution: ${semesters.size}")
                semesters.sortBy { it.order }
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
            list.add(DualLineString(it.name, (if (scoreObject?.type == "count") "Score" else "GPA") + ": TODO"))
        }
        updateActionBar()
        adapter.update(list)
        adapter.notifyDataSetChanged()
    }

    private fun updateActionBar() {
        var subtitle: String? = null
        var title: String? = null
        if (scoreObject != null) {
            subtitle = if (scoreObject!!.type == "count") "Score" else "GPA"
            subtitle += ": TODO"
        }
        if (selectedInstitution != null) title = "${selectedInstitution!!.name} (${selectedInstitution!!.shortName})"
        callback?.updateActionBar(title, subtitle)
    }

    companion object {
        private const val TAG = "GpaCalcSemesterList"
    }
}
