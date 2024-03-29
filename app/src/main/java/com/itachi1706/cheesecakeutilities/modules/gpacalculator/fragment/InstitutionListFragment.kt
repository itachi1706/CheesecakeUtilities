package com.itachi1706.cheesecakeutilities.modules.gpacalculator.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.itachi1706.cheesecakeutilities.modules.gpacalculator.AddInstitutionActivity
import com.itachi1706.cheesecakeutilities.modules.gpacalculator.GpaCalcFirebaseUtils
import com.itachi1706.cheesecakeutilities.modules.gpacalculator.GpaRecyclerAdapter
import com.itachi1706.cheesecakeutilities.modules.gpacalculator.MainViewActivity
import com.itachi1706.cheesecakeutilities.modules.gpacalculator.interfaces.GpaCalcCallback
import com.itachi1706.cheesecakeutilities.modules.gpacalculator.objects.GpaInstitution
import com.itachi1706.cheesecakeutilities.modules.gpacalculator.objects.GpaRecycler
import com.itachi1706.cheesecakeutilities.R
import com.itachi1706.cheesecakeutilities.util.FirebaseValueEventListener
import com.itachi1706.helperlib.helpers.LogHelper

/**
 * Institution List View
 */
class InstitutionListFragment : BaseGpaFragment() {

    private val state = MainViewActivity.STATE_INSTITUTION

    private val institutions: ArrayList<GpaInstitution> = ArrayList()

    override fun getLogTag(): String { return TAG }

    override fun getState(): Int { return state }

    override fun evaluateToCont(v: View): Boolean { return true }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = super.onCreateView(inflater, container, savedInstanceState)

        adapter.setOnClickListener(View.OnClickListener { view ->
            val viewHolder = view.tag as GpaRecyclerAdapter.GpaViewHolder
            val pos = viewHolder.adapterPosition
            val instituteSelected = institutions[pos]
            callback?.selectInstitute(instituteSelected)
        })

        adapter.setOnCreateContextMenuListener(View.OnCreateContextMenuListener{menu, view, _ ->
            // Get selected institution
            val viewHolder = view.tag as GpaRecyclerAdapter.GpaViewHolder
            if (!initContextSelectMode(viewHolder.adapterPosition)) return@OnCreateContextMenuListener // Do nothing
            menu.setHeaderTitle("${institutionContextSel!!.name} (${institutionContextSel!!.shortName})")
            activity?.menuInflater?.inflate(R.menu.context_menu_editdelete, menu)
        })
        return v
    }

    private var institutionContextSel: GpaInstitution? = null

    override fun onContextItemSelected(item: MenuItem): Boolean {
        LogHelper.d(TAG, "Context Item Selected")
        if (institutionContextSel == null) return false
        return when (item.itemId) {
            R.id.menu_edit -> edit(null)
            R.id.menu_delete -> delete(null)
            else -> super.onContextItemSelected(item)
        }
    }

    override fun initContextSelectMode(position: Int): Boolean {
        institutionContextSel = institutions[position]
        if (institutionContextSel == null) return false
        return true
    }

    override fun edit(position: Int?): Boolean {
        if (position != null) if (!initContextSelectMode(position)) return false

        startActivity(Intent(context, AddInstitutionActivity::class.java).apply {
            putExtra("userid", callback?.getUserId())
            putExtra("editmode", institutionContextSel!!.shortName)
        })
        return true
    }

    override fun delete(position: Int?): Boolean {
        if (position != null) if (!initContextSelectMode(position)) return false

        val instituteToDelete = institutionContextSel!!.copy()
        val data = callback?.getUserData() ?: return false
        data.child(instituteToDelete.shortName).removeValue()
        Snackbar.make(view!!, "Institute Deleted", Snackbar.LENGTH_LONG).setAction("Undo") { v ->
            data.child(instituteToDelete.shortName).setValue(instituteToDelete)
            Snackbar.make(v, "Delete undone", Snackbar.LENGTH_SHORT).show()
        }.show()
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
