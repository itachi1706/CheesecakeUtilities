package com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.*
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.interfaces.GpaCalcCallback
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.objects.GpaInstitution
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.objects.GpaRecycler
import com.itachi1706.cheesecakeutilities.R
import com.itachi1706.cheesecakeutilities.Util.FirebaseValueEventListener
import com.itachi1706.cheesecakeutilities.Util.LogHelper

/**
 * Institution List View
 */
class InstitutionListFragment : BaseGpaFragment() {

    private val state = MainViewActivity.STATE_INSTITUTION

    private val institutions: ArrayList<GpaInstitution> = ArrayList()
    private lateinit var adapter: GpaRecyclerAdapter

    override fun getLogTag(): String { return TAG }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_recycler_view, container, false)
        val recyclerView = v.findViewById<RecyclerView>(R.id.main_menu_recycler_view)

        recyclerView.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()

        // Update layout
        adapter = GpaRecyclerAdapter(arrayListOf(), false)
        recyclerView.adapter = adapter

        callback?.onStateSwitch(state)

        adapter.setOnClickListener(View.OnClickListener { view ->
            val viewHolder = view.tag as GpaRecyclerAdapter.GpaViewHolder
            val pos = viewHolder.adapterPosition
            val instituteSelected = institutions[pos]
            callback?.selectInstitute(instituteSelected)
        })

        adapter.setOnCreateContextMenuListener(View.OnCreateContextMenuListener{menu, view, _ ->
            // Get selected institution
            val viewHolder = view.tag as GpaRecyclerAdapter.GpaViewHolder
            institutionContextSel = institutions[viewHolder.adapterPosition]
            if (institutionContextSel == null) return@OnCreateContextMenuListener // Do nothing
            menu.setHeaderTitle("${institutionContextSel!!.name} (${institutionContextSel!!.shortName})")
            activity?.menuInflater?.inflate(R.menu.context_menu_editdelete, menu)
        })
        return v
    }

    private var institutionContextSel: GpaInstitution? = null

    override fun onContextItemSelected(item: MenuItem): Boolean {
        LogHelper.d(TAG, "Context Item Selected")
        if (institutionContextSel == null) return false
        when (item.itemId) {
            R.id.menu_edit -> startActivity(Intent(context, AddInstitutionActivity::class.java).apply {
                putExtra("userid", callback?.getUserId())
                putExtra("editmode", institutionContextSel!!.shortName)
            })
            R.id.menu_delete -> {
                val instituteToDelete = institutionContextSel!!.copy()
                val data = callback?.getUserData() ?: return false
                data.child(instituteToDelete.shortName).removeValue()
                Snackbar.make(view!!, "Institute Deleted", Snackbar.LENGTH_LONG).setAction("Undo") { v ->
                    data.child(instituteToDelete.shortName).setValue(instituteToDelete)
                    Snackbar.make(v, "Delete undone", Snackbar.LENGTH_SHORT).show()
                }.show()
            }
            else -> return super.onContextItemSelected(item)
        }
        return true
    }

    override fun onResume() {
        super.onResume()
        callback?.updateActionBar(null, null)
    }

    override fun onStart() {
        super.onStart()
        LogHelper.i(TAG, "Registering Institution Firebase DB Listener")
        listener = callback?.getUserData()?.addValueEventListener(object: FirebaseValueEventListener(TAG, "loadInstitutionsList") {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (callback?.getCurrentState() != state) return
                LogHelper.i(TAG, "Processing updated institutions...")
                institutions.clear()
                if (!dataSnapshot.hasChildren()) return
                dataSnapshot.children.forEach {
                    institutions.add(it.getValue(GpaInstitution::class.java)!!)
                }
                LogHelper.i(TAG, "Institution Size: ${institutions.size}")
                institutions.sortBy { it.order }
                instituteProcessAndUpdate()
            }
        })
    }

    private var retry = false

    private fun instituteProcessAndUpdate() {
        val list: ArrayList<GpaRecycler> = ArrayList()
        val scoring = callback?.getScoreMap()!!
        if (scoring.isEmpty() && !retry) {
            retry = true
            callback?.updateScoreMap(object: GpaCalcCallback { override fun onCallback() { instituteProcessAndUpdate() } })
        }
        institutions.forEach {
            val type = if (scoring.containsKey(it.type)) scoring[it.type]?.name else "Unknown"
            list.add(GpaRecycler("${it.name} (${it.shortName})", "${getTimestampString(it.startTimestamp, it.endTimestamp)}\nScoring: $type",
                    grade = if (it.gpa == "Unknown") "???" else it.gpa, gradeColor = GpaCalcFirebaseUtils.getGpaColor(it.gpa, scoring[it.type], context)))
        }
        adapter.update(list)
        adapter.notifyDataSetChanged()
    }

    companion object {
        private const val TAG = "GpaCalcInstitutionList"
    }
}
