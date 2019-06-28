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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.GpaCalculatorMainActivity
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.`interface`.StateSwitchListener
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.objects.GpaInstitution
import com.itachi1706.cheesecakeutilities.R
import com.itachi1706.cheesecakeutilities.RecyclerAdapters.DualLineStringRecyclerAdapter
import com.itachi1706.cheesecakeutilities.Util.FirebaseUtils
import com.itachi1706.cheesecakeutilities.Util.LogHelper
import com.itachi1706.cheesecakeutilities.objects.DualLineString

/**
 * Institution List View
 */
class GpaCalcInstitutionListFragment : Fragment() {

    private var callback: StateSwitchListener? = null
    private val state = GpaCalculatorMainActivity.STATE_INSTITUTION

    private val institutions: ArrayList<GpaInstitution> = ArrayList()
    private lateinit var adapter: DualLineStringRecyclerAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (!toBoot()) return // Check if boot else dont boot
        if (context is GpaCalculatorMainActivity) {
            callback = context
        }
    }

    private fun toBoot(): Boolean {
        return arguments?.getBoolean("boot") ?: false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_recycler_view, container, false)
        if (!toBoot()) return v // Check if boot else dont boot
        val recyclerView = v.findViewById<RecyclerView>(R.id.main_menu_recycler_view)

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
            val instituteSelected = institutions[pos]
            callback?.selectInstitute(instituteSelected.shortName, instituteSelected.type)
        }
        return v
    }

    override fun onResume() {
        super.onResume()
        if (!toBoot()) return
        callback?.updateActionBar(null, null)
    }

    private var listener: ValueEventListener? = null
    override fun onStart() {
        super.onStart()
        if (!toBoot()) return
        if (listener != null) {
            FirebaseUtils.removeListener(listener!!)
            listener = null
            LogHelper.e(TAG, "Firebase DB Listeners exists when it should not have, terminating it forcibly")
        }

        LogHelper.i(TAG, "Registering Institution Firebase DB Listeners")
        listener = callback?.getUserData()?.addValueEventListener(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                LogHelper.w(TAG, "loadInstitutionsList:onCancelled", p0.toException())
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
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

    override fun onStop() {
        super.onStop()
        if (listener != null) {
            FirebaseUtils.removeListener(listener!!)
            Log.i(TAG, "Firebase Listener Unregisted")
            listener = null
        }
    }

    private fun instituteProcessAndUpdate() {
        val list: ArrayList<DualLineString> = ArrayList()
        val scoring = callback?.getScoreMap()!!
        institutions.forEach {
            val type = if (scoring.containsKey(it.type)) scoring[it.type]?.name else "Unknown"
            list.add(DualLineString("${it.name} (${it.shortName})", "Scoring Mode: $type"))
        }
        adapter.update(list)
        adapter.notifyDataSetChanged()
    }

    companion object {
        private const val TAG = "GpaCalcInstitutionList"
    }
}
