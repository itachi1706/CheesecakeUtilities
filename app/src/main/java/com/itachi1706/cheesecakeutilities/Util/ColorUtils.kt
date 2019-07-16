package com.itachi1706.cheesecakeutilities.Util

import android.content.Context
import androidx.core.content.ContextCompat

import com.itachi1706.cheesecakeutilities.R

/**
 * Created by Kenneth on 5/7/2018.
 * for com.itachi1706.cheesecakeutilities.Util in CheesecakeUtilities
 */
class ColorUtils {

    companion object {

        fun getColorFromVariable(context: Context, color: Int): Int {
            return when (color) {
                RED -> ContextCompat.getColor(context, R.color.red)
                ORANGE -> ContextCompat.getColor(context, R.color.orange)
                YELLOW -> ContextCompat.getColor(context, R.color.yellow)
                GREEN -> ContextCompat.getColor(context, R.color.green)
                BLUE -> ContextCompat.getColor(context, R.color.blue)
                INDIGO -> ContextCompat.getColor(context, R.color.indigo)
                VIOLET -> ContextCompat.getColor(context, R.color.violet)
                DARK_GREEN -> ContextCompat.getColor(context, R.color.dark_green)
                DARK_YELLOW -> ContextCompat.getColor(context, R.color.dark_yellow)
                else -> ContextCompat.getColor(context, R.color.black)
            }
        }

        val RED = 0
        val ORANGE = 1
        val YELLOW = 2
        val GREEN = 3
        val BLUE = 4
        val INDIGO = 5
        val VIOLET = 6
        val DARK_GREEN = 7
        val DARK_YELLOW = 8
    }
}
