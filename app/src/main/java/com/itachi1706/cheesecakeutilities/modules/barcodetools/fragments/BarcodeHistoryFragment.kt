package com.itachi1706.cheesecakeutilities.modules.barcodetools.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.itachi1706.cheesecakeutilities.databinding.FragmentRecyclerViewBinding
import com.itachi1706.cheesecakeutilities.modules.barcodetools.BarcodeHelper
import com.itachi1706.cheesecakeutilities.modules.barcodetools.BarcodeHistoryRecyclerAdapter
import com.itachi1706.cheesecakeutilities.modules.barcodetools.objects.BarcodeHistory
import com.itachi1706.cheesecakeutilities.modules.barcodetools.objects.BarcodeHistoryGen
import com.itachi1706.cheesecakeutilities.modules.barcodetools.objects.BarcodeHistoryScan
import com.itachi1706.cheesecakeutilities.recycleradapters.StringRecyclerAdapter
import com.itachi1706.helperlib.helpers.LogHelper
import com.itachi1706.helperlib.helpers.PrefHelper

/**
 * Created by Kenneth on 15/1/2020.
 * for com.itachi1706.cheesecakeutilities.modules.barcodeTools.fragments in CheesecakeUtilities
 */
class BarcodeHistoryFragment : Fragment() {

    private var historyString: String = ""
    private var bcType: String = BarcodeHelper.SP_BARCODE_SCANNED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        historyString = arguments?.getString("history") ?: ""
        bcType = arguments?.getString("type") ?: BarcodeHelper.SP_BARCODE_SCANNED
        LogHelper.d(TAG, "History Str: $historyString")
    }

    private var _binding: FragmentRecyclerViewBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentRecyclerViewBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mainMenuRecyclerView.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(context)
        binding.mainMenuRecyclerView.layoutManager = linearLayoutManager
        binding.mainMenuRecyclerView.itemAnimator = DefaultItemAnimator()

        val gson = Gson()
        val listType = if (bcType == BarcodeHelper.SP_BARCODE_SCANNED) object : TypeToken<java.util.ArrayList<BarcodeHistoryScan>>() {}.type
            else object : TypeToken<java.util.ArrayList<BarcodeHistoryGen>>() {}.type
        val list: ArrayList<BarcodeHistory> = if (historyString.isNotEmpty()) gson.fromJson(historyString, listType) else ArrayList()
        val adapter = if (list.isNotEmpty()) BarcodeHistoryRecyclerAdapter(list, rvCallback) else StringRecyclerAdapter(arrayOf("No barcodes in history"), false)
        binding.mainMenuRecyclerView.adapter = adapter
    }

    private val rvCallback = object:BarcodeHistoryRecyclerAdapter.Callbacks {
        override fun updateHistory(list: List<BarcodeHistory>) {
            if (context == null) return
            // Convert based on type of barcode
            val updateHistScan = ArrayList<BarcodeHistoryScan>()
            val updateHistGen = ArrayList<BarcodeHistoryGen>()
            list.forEach {
                if (it is BarcodeHistoryScan) updateHistScan.add(it)
                else if (it is BarcodeHistoryGen) updateHistGen.add(it)
            }
            val sp = PrefHelper.getSharedPreferences(context!!, "BarcodeHistory")
            val gson = Gson()
            val string = if (bcType == BarcodeHelper.SP_BARCODE_SCANNED) gson.toJson(updateHistScan) else gson.toJson(updateHistGen)
            sp.edit().putString(bcType, string).apply()
            LogHelper.i(TAG, "Updated History")
        }

        override fun itemSelected(item: BarcodeHistory) {
            val gson = Gson()
            val string = if (bcType == BarcodeHelper.SP_BARCODE_SCANNED) gson.toJson(item as BarcodeHistoryScan) else gson.toJson(item as BarcodeHistoryGen)
            LogHelper.i(TAG, "Selected Item: $string")
            val resultIntent = Intent().apply { putExtra("selection", string); putExtra("barcodeType", bcType) }
            activity?.setResult(Activity.RESULT_OK, resultIntent)
            activity?.finish()
        }

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