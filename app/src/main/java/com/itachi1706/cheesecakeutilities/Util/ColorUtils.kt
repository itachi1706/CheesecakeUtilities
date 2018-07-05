package com.itachi1706.cheesecakeutilities.Util

import android.content.Context
import android.support.v4.content.ContextCompat

import com.itachi1706.cheesecakeutilities.R

/**
 * Created by Kenneth on 5/7/2018.
 * for com.itachi1706.cheesecakeutilities.Util in CheesecakeUtilities
 */
class ColorUtils {

    companion object {

        fun getColorFromVariable(context: Context, color: Int): Int {
            when (color) {
                RED -> return ContextCompat.getColor(context, R.color.red)
                ORANGE -> return ContextCompat.getColor(context, R.color.orange)
                YELLOW -> return ContextCompat.getColor(context, R.color.yellow)
                GREEN -> return ContextCompat.getColor(context, R.color.green)
                BLUE -> return ContextCompat.getColor(context, R.color.blue)
                INDIGO -> return ContextCompat.getColor(context, R.color.indigo)
                VIOLET -> return ContextCompat.getColor(context, R.color.violet)
            }
            return ContextCompat.getColor(context, R.color.black)
        }

        val RED = 0
        val ORANGE = 1
        val YELLOW = 1
        val GREEN = 1
        val BLUE = 1
        val INDIGO = 1
        val VIOLET = 1
    }
}
