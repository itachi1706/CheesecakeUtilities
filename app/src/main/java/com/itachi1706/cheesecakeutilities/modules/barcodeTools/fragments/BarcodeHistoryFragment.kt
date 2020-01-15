package com.itachi1706.cheesecakeutilities.modules.barcodeTools.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.itachi1706.cheesecakeutilities.R
import com.itachi1706.cheesecakeutilities.recyclerAdapters.StringRecyclerAdapter
import kotlinx.android.synthetic.main.fragment_recycler_view.*

/**
 * Created by Kenneth on 15/1/2020.
 * for com.itachi1706.cheesecakeutilities.modules.barcodeTools.fragments in CheesecakeUtilities
 */
class BarcodeHistoryFragment : Fragment() {

    private var historyString: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        historyString = arguments?.getString("history") ?: ""
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_recycler_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        main_menu_recycler_view.setHasFixedSize(true)
        val gridLayoutManager = GridLayoutManager(context, 2)
        main_menu_recycler_view.layoutManager = gridLayoutManager
        main_menu_recycler_view.itemAnimator = DefaultItemAnimator()
        // TODO: Setup adapter
        val adapter = StringRecyclerAdapter(arrayOf("History Coming Soon (WIP)"))
        main_menu_recycler_view.adapter = adapter
    }

    companion object {
        @JvmStatic
        fun newInstance(historyString: String): BarcodeHistoryFragment { return BarcodeHistoryFragment().apply { arguments = Bundle().apply { putString("history", historyString) } } }
    }
}