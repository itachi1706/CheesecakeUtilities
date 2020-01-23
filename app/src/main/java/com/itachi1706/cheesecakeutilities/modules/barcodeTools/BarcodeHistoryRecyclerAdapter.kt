package com.itachi1706.cheesecakeutilities.modules.barcodeTools

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.itachi1706.cheesecakeutilities.R
import com.itachi1706.cheesecakeutilities.modules.barcodeTools.objects.BarcodeHistory
import com.itachi1706.cheesecakeutilities.modules.barcodeTools.objects.BarcodeHistoryGen
import com.itachi1706.cheesecakeutilities.modules.barcodeTools.objects.BarcodeHistoryScan
import com.itachi1706.helperlib.utils.NotifyUserUtil

/**
 * Created by Kenneth on 23/1/2020.
 * for com.itachi1706.cheesecakeutilities.modules.barcodeTools in CheesecakeUtilities
 */
class BarcodeHistoryRecyclerAdapter(barcodes: List<BarcodeHistory>) : RecyclerView.Adapter<BarcodeHistoryRecyclerAdapter.BarcodeHistoryViewHolder>() {

    private var barcodeList: List<BarcodeHistory> = ArrayList()

    init {
        barcodeList = barcodes
    }

    fun update(updatedBarcodes: List<BarcodeHistory>) { barcodeList = updatedBarcodes }

    override fun getItemCount(): Int { return barcodeList.size }

    override fun onBindViewHolder(holder: BarcodeHistoryViewHolder, position: Int) {
        val barcode = barcodeList[position]
        holder.barcode = barcode
        if (barcode is BarcodeHistoryGen) {
            // Generated Barcode
            holder.title.text = barcode.text
            holder.subtitle.text = barcode.format.name
        } else if (barcode is BarcodeHistoryScan) {
            // Scanned Barcode
            holder.title.text = barcode.barcodeValue
            holder.subtitle.text = "${BarcodeHelper.getFormatName(barcode.format)} | ${BarcodeHelper.getValueFormat(barcode.valueType)}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BarcodeHistoryViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_default_simple_list_item_2, parent, false)
        return BarcodeHistoryViewHolder(itemView)
    }

    inner class BarcodeHistoryViewHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener, View.OnLongClickListener {
        var title: TextView = v.findViewById(android.R.id.text1)
        var subtitle: TextView = v.findViewById(android.R.id.text2)
        var barcode: BarcodeHistory? = null

        init {
            v.setOnClickListener(this)
            v.setOnLongClickListener(this)
            v.tag = this
        }

        override fun onClick(v: View) {
            NotifyUserUtil.createShortToast(v.context, title.text)
        }

        override fun onLongClick(v: View): Boolean {
            NotifyUserUtil.createShortToast(v.context, subtitle.text)
            return true
        }
    }
}