package com.itachi1706.cheesecakeutilities.modules.barcodeTools.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.itachi1706.cheesecakeutilities.R
import com.itachi1706.cheesecakeutilities.modules.barcodeTools.BarcodeHelper
import com.itachi1706.cheesecakeutilities.modules.barcodeTools.objects.BarcodeHistory
import com.itachi1706.cheesecakeutilities.modules.barcodeTools.objects.BarcodeHistoryGen
import com.itachi1706.cheesecakeutilities.modules.barcodeTools.objects.BarcodeHistoryScan
import com.itachi1706.cheesecakeutilities.recyclerAdapters.StringRecyclerAdapter
import com.itachi1706.helperlib.helpers.LogHelper
import kotlinx.android.synthetic.main.fragment_recycler_view.*

/**
 * Created by Kenneth on 15/1/2020.
 * for com.itachi1706.cheesecakeutilities.modules.barcodeTools.fragments in CheesecakeUtilities
 */
class BarcodeHistoryFragment : Fragment() {

    private var historyString: String = ""
    private var type: String = BarcodeHelper.SP_BARCODE_SCANNED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        historyString = arguments?.getString("history") ?: ""
        type = arguments?.getString("type") ?: BarcodeHelper.SP_BARCODE_SCANNED
        LogHelper.d(TAG, "History Str: $historyString")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_recycler_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        main_menu_recycler_view.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(context)
        main_menu_recycler_view.layoutManager = linearLayoutManager
        main_menu_recycler_view.itemAnimator = DefaultItemAnimator()

        val gson = Gson()
        val listType = if (type == BarcodeHelper.SP_BARCODE_SCANNED) object : TypeToken<java.util.ArrayList<BarcodeHistoryScan>>() {}.type
            else object : TypeToken<java.util.ArrayList<BarcodeHistoryGen>>() {}.type
        val list: ArrayList<BarcodeHistory> = gson.fromJson(historyString, listType)
        // TODO: Setup proper adapter
        val strListTmp = ArrayList<String>()
        list.forEach {
            if (it is BarcodeHistoryGen) strListTmp.add(it.text)
            else if (it is BarcodeHistoryScan) strListTmp.add(it.barcodeValue)
        }
        val adapter = StringRecyclerAdapter(strListTmp)
        main_menu_recycler_view.adapter = adapter
    }

    companion object {
        private const val TAG = "BarcodeHistoryFrag"
        @JvmStatic
        fun newInstance(historyString: String, type: String): BarcodeHistoryFragment { return BarcodeHistoryFragment().apply { arguments = Bundle().apply {
            putString("history", historyString)
            putString("type", type)
        } } }
    }
}