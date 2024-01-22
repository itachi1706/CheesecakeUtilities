package com.itachi1706.cheesecakeutilities.modules.unicodekeyboard.recyclerAdapters

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.itachi1706.cheesecakeutilities.R

/**
 * Created by Kenneth on 11/1/2020.
 * for com.itachi1706.cheesecakeutilities.modules.unicodeKeyboard.recyclerAdapters in CheesecakeUtilities
 */
class UnicodeMenuAdapter(strings: List<String>) : RecyclerView.Adapter<UnicodeMenuAdapter.UnicodeMenuHolder>() {

    private var stringList: List<String> = ArrayList()
    init { stringList = strings }

    constructor(string: Array<String>): this(string.toList())

    override fun getItemCount(): Int { return stringList.size }

    override fun onBindViewHolder(holder: UnicodeMenuHolder, position: Int) {
        val s = stringList[position]
        holder.title.text = s
        holder.title.isSelected = true
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UnicodeMenuHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_default_simple_list_item_1, parent, false)
        return UnicodeMenuHolder(itemView)
    }

    inner class UnicodeMenuHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener, View.OnLongClickListener  {
        var title: TextView = v.findViewById(R.id.text1)

        init {
            title.ellipsize = TextUtils.TruncateAt.MARQUEE
            title.marqueeRepeatLimit = -1
            title.setHorizontallyScrolling(true)
            v.setOnClickListener(this)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) v.setOnLongClickListener(this)
        }

        override fun onClick(v: View) {
            val clipboard = v.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = getClip()
            clipboard.setPrimaryClip(clip)
            Toast.makeText(v.context, "${clip.getItemAt(0).text}\ncopied to clipboard", Toast.LENGTH_LONG).show()
        }

        @RequiresApi(Build.VERSION_CODES.N)
        override fun onLongClick(v: View): Boolean {
            val dragShadowBuilder = View.DragShadowBuilder(v)
            v.startDragAndDrop(getClip(), dragShadowBuilder, true, View.DRAG_FLAG_GLOBAL or View.DRAG_FLAG_GLOBAL_URI_READ or View.DRAG_FLAG_GLOBAL_PERSISTABLE_URI_PERMISSION)
            return true
        }

        private fun getClip(): ClipData { return ClipData.newPlainText("unicode", title.text.toString()) }
    }
}