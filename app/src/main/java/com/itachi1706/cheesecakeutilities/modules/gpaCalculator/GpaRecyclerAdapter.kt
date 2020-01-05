package com.itachi1706.cheesecakeutilities.modules.gpaCalculator

import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.itachi1706.helperlib.helpers.PrefHelper
import com.itachi1706.cheesecakeutilities.modules.gpaCalculator.objects.GpaRecycler
import com.itachi1706.cheesecakeutilities.R

/**
 * Created by Kenneth on 16/7/2019.
 * for com.itachi1706.cheesecakeutilities.modules.gpaCalculator in CheesecakeUtilities
 */
class GpaRecyclerAdapter(initialGpa: List<GpaRecycler>, announceState: Boolean = false) : RecyclerView.Adapter<GpaRecyclerAdapter.GpaViewHolder>() {

    private var gpaList: List<GpaRecycler> = ArrayList()
    private var htmlformat = false
    private var announce = false
    private var onClickListener: View.OnClickListener? = null
    private var onLongClickListener: View.OnLongClickListener? = null
    private var createContextMenuListener: View.OnCreateContextMenuListener? = null

    init {
        gpaList = initialGpa
        announce = announceState
    }

    fun setHtmlFormat(htmlFormat: Boolean): GpaRecyclerAdapter {
        this.htmlformat = htmlFormat
        return this
    }

    fun setOnClickListener(listener: View.OnClickListener?) { onClickListener = listener }

    fun setOnLongClickListener(listener: View.OnLongClickListener?) { onLongClickListener = listener }

    fun setOnCreateContextMenuListener(listener: View.OnCreateContextMenuListener?) { createContextMenuListener = listener }

    fun update(updatedGrade: List<GpaRecycler>) { gpaList = updatedGrade }

    override fun getItemCount(): Int {
        return gpaList.size
    }

    override fun onBindViewHolder(holder: GpaViewHolder, position: Int) {
        holder.color.visibility = View.VISIBLE
        val gpa = gpaList[position]
        holder.title.text = gpa.main ?: ""
        holder.subtitle.text = gpa.sub ?: ""
        holder.grade.text = gpa.grade ?: ""
        if (gpa.color != null) holder.color.setImageDrawable(ColorDrawable(gpa.color!!))
        else holder.color.visibility = View.GONE
        if (gpa.gradeColor != -999) holder.grade.setTextColor(gpa.gradeColor)
        else holder.grade.setTextColor(holder.defaultTextColor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GpaViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_gpa_list_item, parent, false)
        return GpaViewHolder(itemView, onClickListener, onLongClickListener)
    }

    inner class GpaViewHolder(v: View, listener: View.OnClickListener? = null, longClickListener: View.OnLongClickListener?) : RecyclerView.ViewHolder(v), View.OnClickListener {
        var title: TextView = v.findViewById(R.id.title)
        var subtitle: TextView = v.findViewById(R.id.subtitle)
        var grade: TextView = v.findViewById(R.id.gradeNumber)
        var color: ImageView = v.findViewById(R.id.colorView)
        val defaultTextColor: Int = ContextCompat.getColor(v.context, if (PrefHelper.isNightModeEnabled(v.context)) R.color.default_text_color_sec_dark else R.color.default_text_color_sec_light)

        init {
            v.setOnClickListener(listener ?: this)
            v.tag = this
            if (longClickListener != null) v.setOnLongClickListener(longClickListener)
            if (createContextMenuListener != null) v.setOnCreateContextMenuListener(createContextMenuListener)
        }

        override fun onClick(v: View?) {
            if (announce) Toast.makeText(v!!.context, title.text, Toast.LENGTH_SHORT).show()
        }
    }
}