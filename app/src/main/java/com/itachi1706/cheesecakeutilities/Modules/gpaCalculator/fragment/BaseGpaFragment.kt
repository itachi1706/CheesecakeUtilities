package com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.ValueEventListener
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.GpaCalcFirebaseUtils
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.GpaRecyclerAdapter
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.MainViewActivity
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.interfaces.StateSwitchListener
import com.itachi1706.cheesecakeutilities.R
import com.itachi1706.cheesecakeutilities.recyclerAdapters.SwipeEditDeleteCallback
import com.itachi1706.cheesecakeutilities.util.FirebaseUtils
import com.itachi1706.cheesecakeutilities.util.LogHelper
import java.util.*

/**
 * Created by Kenneth on 16/7/2019.
 * for com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.fragment in CheesecakeUtilities
 */
abstract class BaseGpaFragment : Fragment(), SwipeEditDeleteCallback.ISwipeCallback {
    var callback: StateSwitchListener? = null
    lateinit var adapter: GpaRecyclerAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainViewActivity) {
            callback = context
        }
    }

    var listener: ValueEventListener? = null
    override fun onStart() {
        super.onStart()
        if (listener != null) {
            FirebaseUtils.removeListener(listener!!)
            listener = null
            LogHelper.e(getLogTag(), "Firebase DB Listeners exists when it should not have, terminating it forcibly")
        }
    }

    override fun onStop() {
        super.onStop()
        if (listener != null) {
            FirebaseUtils.removeListener(listener!!)
            Log.i(getLogTag(), "Firebase Listener Unregisted")
            listener = null
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_recycler_view, container, false)
        val recyclerView = v.findViewById<RecyclerView>(R.id.main_menu_recycler_view)

        if (!evaluateToCont(v)) return v

        recyclerView.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        val itemTouchHelper = ItemTouchHelper(SwipeEditDeleteCallback(this, v.context))
        itemTouchHelper.attachToRecyclerView(recyclerView)

        // Update layout
        adapter = GpaRecyclerAdapter(arrayListOf(), false)
        recyclerView.adapter = adapter

        callback?.onStateSwitch(getState())
        return v
    }

    abstract fun getState(): Int
    abstract fun evaluateToCont(v: View): Boolean
    abstract fun getLogTag(): String
    abstract fun initContextSelectMode(position: Int): Boolean

    fun getTimestampString(startTimestamp: Long, endTimestamp: Long): String {
        val calendar = Calendar.getInstance()
        val dateFormat = GpaCalcFirebaseUtils.DATE_FORMAT
        calendar.timeInMillis = startTimestamp
        var timestamp = "${dateFormat.format(calendar.time)} - "
        if (endTimestamp == (-1).toLong()) timestamp += "Present"
        else {
            calendar.timeInMillis = endTimestamp
            timestamp += dateFormat.format(calendar.time)
        }
        return timestamp
    }
}