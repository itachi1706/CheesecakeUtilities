package com.itachi1706.cheesecakeutilities.modules.unicodeKeyboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.itachi1706.cheesecakeutilities.R
import com.itachi1706.cheesecakeutilities.modules.unicodeKeyboard.recyclerAdapters.UnicodeMenuAdapter
import kotlinx.android.synthetic.main.fragment_recycler_view.*

/**
 * Created by Kenneth on 12/1/2020.
 * for com.itachi1706.cheesecakeutilities.modules.unicodeKeyboard in CheesecakeUtilities
 */
class UnicodeFragment : Fragment() {

    private lateinit var stringList: Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stringList = arguments?.getStringArray("list") ?: arrayOf()
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
        // Set up layout
        val adapter = UnicodeMenuAdapter(stringList)
        main_menu_recycler_view.adapter = adapter
    }

    companion object {
        @JvmStatic
        fun newInstance(list: Array<String>): UnicodeFragment { return UnicodeFragment().apply { arguments = Bundle().apply { putStringArray("list", list) } } }
    }
}