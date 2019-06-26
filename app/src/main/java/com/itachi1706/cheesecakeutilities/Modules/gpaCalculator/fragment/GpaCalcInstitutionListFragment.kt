package com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.GpaCalculatorMainActivity
import com.itachi1706.cheesecakeutilities.Modules.gpaCalculator.`interface`.StateSwitchListener
import com.itachi1706.cheesecakeutilities.R

/**
 * Institution List View
 */
class GpaCalcInstitutionListFragment : Fragment() {

    private var callback: StateSwitchListener? = null
    private val state = GpaCalculatorMainActivity.STATE_INSTITUTION

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is GpaCalculatorMainActivity) {
            callback = context
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_recycler_view, container, false)

        callback?.onStateSwitch(state)
        return v
    }

    private fun loadData() {
        val curState = callback?.getState() ?: GpaCalculatorMainActivity.STATE_INSTITUTION

    }


}
