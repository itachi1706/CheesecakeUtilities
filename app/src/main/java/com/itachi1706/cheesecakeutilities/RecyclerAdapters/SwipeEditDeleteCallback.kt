package com.itachi1706.cheesecakeutilities.RecyclerAdapters

import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.itachi1706.appupdater.Util.BitmapUtil
import com.itachi1706.cheesecakeutilities.R
import com.itachi1706.cheesecakeutilities.Util.ColorUtils

/**
 * Created by Kenneth on 17/7/2019.
 * for com.itachi1706.cheesecakeutilities.RecyclerAdapters in CheesecakeUtilities
 */
class SwipeEditDeleteCallback(var callback: ISwipeCallback, var context: Context, var editSwipeDirection: Int = ItemTouchHelper.LEFT) :
        ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

    private val editBackground: ColorDrawable = ColorDrawable(ColorUtils.getColorFromVariable(context, ColorUtils.DARK_YELLOW))
    private val deleteBackground: ColorDrawable = ColorDrawable(Color.RED)
    private val iconColor: Paint = Paint().apply {
        val res = ResourcesCompat.getColor(context.resources, R.color.white, null)
        color = res
        colorFilter = PorterDuffColorFilter(res, PorterDuff.Mode.SRC_ATOP)
    }
    private val editBitmap: Bitmap = BitmapUtil.getBitmap(context, R.drawable.ic_edit) //BitmapFactory.decodeResource(context.resources, R.drawable.ic_edit)
    private val deleteBitmap: Bitmap = BitmapUtil.getBitmap(context, R.drawable.ic_delete_24dp) //BitmapFactory.decodeResource(context.resources, R.drawable.ic_delete_24dp)

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val pos = viewHolder.adapterPosition
        if (direction == editSwipeDirection) callback.edit(pos)
        else callback.delete(pos)
    }

    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            val itemView = viewHolder.itemView

            val swipeDirection = if (dX > 0) ItemTouchHelper.RIGHT else if (dX < 0) ItemTouchHelper.LEFT else ItemTouchHelper.UP
            val selectedBackground = if (swipeDirection == editSwipeDirection) editBackground else deleteBackground
            val selectedBitmap = if (swipeDirection == editSwipeDirection) editBitmap else deleteBitmap

            val height = itemView.bottom.toFloat() - itemView.top.toFloat()
            val width = height / 3
            val p = Paint().apply { color = selectedBackground.color }

            when (swipeDirection) {
                ItemTouchHelper.RIGHT -> {
                    val bg = RectF(itemView.left.toFloat(), itemView.top.toFloat(), dX, itemView.bottom.toFloat())
                    c.drawRect(bg, p)
                    val iconDest = RectF(itemView.left.toFloat() + width, itemView.top.toFloat() + width, itemView.left.toFloat() + 2 * width, itemView.bottom.toFloat() - width)
                    c.drawBitmap(selectedBitmap, null, iconDest, iconColor)
                }
                ItemTouchHelper.LEFT -> {
                    val bg = RectF(itemView.right.toFloat() + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat())
                    c.drawRect(bg, p)
                    val iconDest = RectF(itemView.right.toFloat() - 2 * width, itemView.top.toFloat() + width, itemView.right.toFloat() - width, itemView.bottom.toFloat() - width)
                    c.drawBitmap(selectedBitmap, null, iconDest, iconColor)
                }
            }
        }
    }

    interface ISwipeCallback {
        fun edit(position: Int?): Boolean
        fun delete(position: Int?): Boolean
    }
}