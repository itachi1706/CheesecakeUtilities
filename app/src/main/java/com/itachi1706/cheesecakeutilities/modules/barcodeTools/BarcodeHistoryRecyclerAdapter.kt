package com.itachi1706.cheesecakeutilities.modules.barcodeTools

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.*
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.recyclerview.widget.RecyclerView
import com.itachi1706.cheesecakeutilities.R
import com.itachi1706.cheesecakeutilities.modules.barcodeTools.objects.BarcodeHistory
import com.itachi1706.cheesecakeutilities.modules.barcodeTools.objects.BarcodeHistoryGen
import com.itachi1706.cheesecakeutilities.modules.barcodeTools.objects.BarcodeHistoryScan
import com.itachi1706.helperlib.helpers.LogHelper

/**
 * Created by Kenneth on 23/1/2020.
 * for com.itachi1706.cheesecakeutilities.modules.barcodeTools in CheesecakeUtilities
 */
class BarcodeHistoryRecyclerAdapter(barcodes: ArrayList<BarcodeHistory>, val callback: Callbacks) : RecyclerView.Adapter<BarcodeHistoryRecyclerAdapter.BarcodeHistoryViewHolder>() {

    private var barcodeList: ArrayList<BarcodeHistory> = ArrayList()

    init {
        barcodeList = barcodes
    }

    fun update(updatedBarcodes: ArrayList<BarcodeHistory>) { barcodeList = updatedBarcodes }

    override fun getItemCount(): Int { return barcodeList.size }

    override fun onBindViewHolder(holder: BarcodeHistoryViewHolder, position: Int) {
        val barcode = barcodeList[position]
        holder.barcode = barcode
        holder.layout.background = holder.origBackground
        if (multiSelection.contains(barcode)) holder.layout.setBackgroundColor(Color.LTGRAY)
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

    // Multiselect handling
    private var multiMode = false
    private var multiActionMode: ActionMode? = null
    private var multiSelection = ArrayList<BarcodeHistory>()

    inner class BarcodeHistoryViewHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener, View.OnLongClickListener {
        var title: TextView = v.findViewById(android.R.id.text1)
        var subtitle: TextView = v.findViewById(android.R.id.text2)
        var layout: RelativeLayout = v.findViewById(R.id.layout_two_line)
        var origBackground: Drawable = layout.background
        lateinit var barcode: BarcodeHistory

        init {
            v.setOnClickListener(this)
            v.setOnLongClickListener(this)
            v.tag = this
        }

        override fun onClick(v: View) {
            if (multiMode) {
                selectItem(barcode, v.context)
            } else {
                callback.itemSelected(barcode)
            }
        }

        override fun onLongClick(v: View): Boolean {
            if (!::barcode.isInitialized) return false
            if (multiMode) return true // Do not reinit
            multiActionMode = (v.context as AppCompatActivity).startSupportActionMode(actionModeCallback)
            selectItem(barcode, v.context)
            return true
        }

        private fun selectItem(item: BarcodeHistory, context: Context) {
            if (!multiMode) return
            if (multiSelection.contains(item)) {
                // Remove
                multiSelection.remove(item)
                layout.background = origBackground
            }
            else {
                multiSelection.add(item)
                layout.setBackgroundColor(Color.LTGRAY)
            }
            multiActionMode?.title = multiSelection.size.toString()
        }

        private val actionModeCallback: ActionMode.Callback = object: ActionMode.Callback {
            override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                val dbgSelection = multiSelection.joinToString (",") { s ->
                    when (s) {
                        is BarcodeHistoryGen -> s.text
                        is BarcodeHistoryScan -> s.barcodeValue
                        else -> "Unknown Barcode"
                    }
                }
                when (item.itemId) {
                    R.id.menu_select_all -> {
                        LogHelper.d(TAG, "SELECT ALL: $dbgSelection")
                        multiSelection.clear()
                        multiSelection.addAll(barcodeList)
                        notifyDataSetChanged()
                        mode.title = multiSelection.size.toString()
                    }
                    R.id.menu_select_invert -> {
                        LogHelper.d(TAG, "INVERSE: $dbgSelection")
                        val tmp = ArrayList<BarcodeHistory>()
                        barcodeList.forEach { if (!multiSelection.contains(it)) tmp.add(it) }
                        multiSelection.clear()
                        multiSelection.addAll(tmp)
                        notifyDataSetChanged()
                        mode.title = multiSelection.size.toString()
                    }
                    R.id.menu_delete -> {
                        LogHelper.d(TAG, "DELETE: $dbgSelection")
                        multiSelection.forEach {
                            barcodeList.remove(it)
                        }
                        mode.finish()
                    }
                    else -> mode.finish()
                }
                return true
            }

            override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                multiMode = true
                mode.menuInflater.inflate(R.menu.context_menu_barcode_hist, menu)
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return false
            }

            override fun onDestroyActionMode(mode: ActionMode?) {
                LogHelper.d(TAG, "Clearing CAB state. Updating")
                multiMode = false
                multiSelection.clear()
                multiActionMode = null
                notifyDataSetChanged()
                callback.updateHistory(barcodeList)
            }
        }
    }

    interface Callbacks {
        fun updateHistory(list: List<BarcodeHistory>)
        fun itemSelected(item: BarcodeHistory)
    }

    companion object {
        private const val TAG = "BarcodeHistAdapter"
    }
}