package com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.database.DatabaseReference
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.GpaCalculatorMainActivity
import com.itachi1706.cheesecakeutilities.R

/**
 * A placeholder fragment containing a simple view.
 */
class GpaCalculatorListFragment : Fragment() {

    private var callback: StateSwitchListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is GpaCalculatorMainActivity) {
            callback = context
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_recycler_view, container, false)

        // By default we are loading institution
        callback?.onStateSwitch(GpaCalculatorMainActivity.STATE_INSTITUTION)
        return v
    }

    private fun loadData() {
        val curState = callback?.getState() ?: GpaCalculatorMainActivity.STATE_INSTITUTION

    }

    interface StateSwitchListener {
        fun onStateSwitch(newState: Int)
        fun getState(): Int
        fun getUserData(): DatabaseReference
    }
}
