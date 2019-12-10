@file:Suppress("unused")

package com.itachi1706.cheesecakeutilities.huh.com.thebluealliance.spectrum

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import com.itachi1706.cheesecakeutilities.R
import com.itachi1706.cheesecakeutilities.huh.com.thebluealliance.spectrum.internal.ColorItem
import com.itachi1706.cheesecakeutilities.huh.com.thebluealliance.spectrum.internal.ColorUtil
import com.itachi1706.cheesecakeutilities.huh.com.thebluealliance.spectrum.internal.SelectedColorChangedEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.*
import kotlin.math.min

/**
 * General-purpose class that displays colors in a grid.
 */
open class SpectrumPalette : LinearLayout {

    private var mColorItemDimension: Int = 0
    private var mColorItemMargin: Int = 0
    @ColorInt
    private var mColors: IntArray? = null
    @ColorInt
    private var mSelectedColor: Int = 0
    private var mListener: OnColorSelectedListener? = null
    private var mAutoPadding = false
    private var mHasFixedColumnCount = false
    private var mFixedColumnCount = -1
    private var mOutlineWidth = 0
    private var originalPaddingTop = 0
    private var originalPaddingBottom = 0
    private var mSetPaddingCalledInternally = false

    private var mNumColumns = 2
    private var mOldNumColumns = -1
    private var mViewInitialized = false

    private var mEventBus: EventBus? = null

    private val mItems = ArrayList<ColorItem>()

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {

        val a = getContext().theme.obtainStyledAttributes(attrs, R.styleable.SpectrumPalette, 0, 0)

        val id = a.getResourceId(R.styleable.SpectrumPalette_spectrum_colors, 0)
        if (id != 0) {
            mColors = getContext().resources.getIntArray(id)
        }

        mAutoPadding = a.getBoolean(R.styleable.SpectrumPalette_spectrum_autoPadding, false)
        mOutlineWidth = a.getDimensionPixelSize(R.styleable.SpectrumPalette_spectrum_outlineWidth, 0)
        mFixedColumnCount = a.getInt(R.styleable.SpectrumPalette_spectrum_columnCount, -1)
        if (mFixedColumnCount != -1) {
            mHasFixedColumnCount = true
        }

        a.recycle()

        originalPaddingTop = paddingTop
        originalPaddingBottom = paddingBottom

        init()
    }

    private fun init() {
        mEventBus = EventBus()
        mEventBus!!.register(this)

        mColorItemDimension = resources.getDimensionPixelSize(R.dimen.color_item_small)
        mColorItemMargin = resources.getDimensionPixelSize(R.dimen.color_item_margins_small)

        orientation = VERTICAL
    }

    /**
     * Sets the colors that this palette will display
     *
     * @param colors an array of ARGB colors
     */
    fun setColors(@ColorInt colors: IntArray) {
        mColors = colors
        mViewInitialized = false
        createPaletteView()
    }

    /**
     * Sets the currently selected color. This should be one of the colors specified via
     * [.setColors]; behavior is undefined if `color` is not among those colors.
     *
     * @param color the color to be marked as selected
     */
    fun setSelectedColor(@ColorInt color: Int) {
        mSelectedColor = color
        mEventBus!!.post(SelectedColorChangedEvent(mSelectedColor))
    }

    /**
     * Registers a callback to be invoked when a new color is selected.
     */
    fun setOnColorSelectedListener(listener: OnColorSelectedListener) {
        mListener = listener
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val width: Int
        var height: Int

        if (!mHasFixedColumnCount) {
            when (widthMode) {
                MeasureSpec.EXACTLY -> {
                    width = widthSize
                    mNumColumns = computeColumnCount(widthSize - (paddingLeft + paddingRight))
                }
                MeasureSpec.AT_MOST -> {
                    width = widthSize
                    mNumColumns = computeColumnCount(widthSize - (paddingLeft + paddingRight))
                }
                else -> {
                    width = computeWidthForNumColumns(DEFAULT_COLUMN_COUNT) + paddingLeft + paddingRight
                    mNumColumns = DEFAULT_COLUMN_COUNT
                }
            }
        } else {
            width = computeWidthForNumColumns(mFixedColumnCount) + paddingLeft + paddingRight
            mNumColumns = mFixedColumnCount
        }

        val mComputedVerticalPadding = (width - (computeWidthForNumColumns(mNumColumns) + paddingLeft + paddingRight)) / 2

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize
        } else if (heightMode == MeasureSpec.AT_MOST) {
            var desiredHeight = computeHeight(mNumColumns) + originalPaddingTop + originalPaddingBottom
            if (mAutoPadding) {
                desiredHeight += 2 * mComputedVerticalPadding
            }
            height = min(desiredHeight, heightSize)
        } else {
            height = computeHeight(mNumColumns) + originalPaddingTop + originalPaddingBottom
            if (mAutoPadding) {
                height += 2 * mComputedVerticalPadding
            }
        }

        if (mAutoPadding) {
            setPaddingInternal(paddingLeft, originalPaddingTop + mComputedVerticalPadding, paddingRight, originalPaddingBottom + mComputedVerticalPadding)
        }
        createPaletteView()

        super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY))
    }

    private fun computeColumnCount(maxWidth: Int): Int {
        var numColumns = 0
        while ((numColumns + 1) * mColorItemDimension + (numColumns + 1) * 2 * mColorItemMargin <= maxWidth) {
            numColumns++
        }
        return numColumns
    }

    private fun computeWidthForNumColumns(columnCount: Int): Int {
        return columnCount * (mColorItemDimension + 2 * mColorItemMargin)
    }

    private fun computeHeight(columnCount: Int): Int {
        if (mColors == null) {
            // View does not have any colors to display, so we won't take up any room
            return 0
        }
        var rowCount = mColors!!.size / columnCount
        if (mColors!!.size % columnCount != 0) {
            rowCount++
        }
        return rowCount * (mColorItemDimension + 2 * mColorItemMargin)
    }


    private fun setPaddingInternal(left: Int, top: Int, right: Int, bottom: Int) {
        mSetPaddingCalledInternally = true
        setPadding(left, top, right, bottom)
    }

    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        super.setPadding(left, top, right, bottom)
        if (!mSetPaddingCalledInternally) {
            originalPaddingTop = top
            originalPaddingBottom = bottom
        }
    }

    /**
     * Generates the views to represent this palette's colors. The grid is implemented with
     * [LinearLayout]s. This class itself subclasses [LinearLayout] and is set up in
     * the vertical orientation. Rows consist of horizontal [LinearLayout]s which themselves
     * hold views that display the individual colors.
     */
    private fun createPaletteView() {
        // Only create the view if it hasn't been created yet or if the number of columns has changed
        if (mViewInitialized && mNumColumns == mOldNumColumns) {
            return
        }
        mViewInitialized = true
        mOldNumColumns = mNumColumns

        removeAllViews()

        if (mColors == null) {
            return
        }

        // Add rows
        var numItemsInRow = 0

        var row = createRow()
        for (mColor in mColors!!) {
            val colorItem = createColorItem(mColor, mSelectedColor)
            row.addView(colorItem)
            numItemsInRow++

            if (numItemsInRow == mNumColumns) {
                addView(row)
                row = createRow()
                numItemsInRow = 0
            }
        }

        if (numItemsInRow > 0) {
            while (numItemsInRow < mNumColumns) {
                row.addView(createSpacer())
                numItemsInRow++
            }
            addView(row)
        }
    }

    private fun createRow(): LinearLayout {
        val row = LinearLayout(context)
        row.orientation = HORIZONTAL
        val params = ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        row.layoutParams = params
        row.gravity = Gravity.CENTER_HORIZONTAL
        return row
    }

    private fun createColorItem(color: Int, selectedColor: Int): ColorItem {
        val view = ColorItem(context, color, color == selectedColor, mEventBus!!)
        val params = LayoutParams(mColorItemDimension, mColorItemDimension)
        params.setMargins(mColorItemMargin, mColorItemMargin, mColorItemMargin, mColorItemMargin)
        view.layoutParams = params
        if (mOutlineWidth != 0) {
            view.setOutlineWidth(mOutlineWidth)
        }
        mItems.add(view)
        return view
    }

    private fun createSpacer(): ImageView {
        val view = ImageView(context)
        val params = LayoutParams(mColorItemDimension, mColorItemDimension)
        params.setMargins(mColorItemMargin, mColorItemMargin, mColorItemMargin, mColorItemMargin)
        view.layoutParams = params
        return view
    }

    @Subscribe
    fun onSelectedColorChanged(event: SelectedColorChangedEvent) {
        mSelectedColor = event.selectedColor
        if (mListener != null) {
            mListener!!.onColorSelected(mSelectedColor)
        }
    }

    interface OnColorSelectedListener {
        fun onColorSelected(@ColorInt color: Int)
    }

    /**
     * Returns true if for the given color a dark checkmark is used.
     *
     * @return true if color is "dark"
     */
    fun usesDarkCheckmark(@ColorInt color: Int): Boolean {
        return ColorUtil.isColorDark(color)
    }

    /**
     * Change the size of the outlining
     *
     * @param width in px
     */
    fun setOutlineWidth(width: Int) {
        mOutlineWidth = width
        for (item in mItems) {
            item.setOutlineWidth(width)
        }
    }

    /**
     * Tells the palette to use a fixed number of columns during layout.
     *
     * @param columnCount how many columns to use
     */
    fun setFixedColumnCount(columnCount: Int) {
        if (columnCount > 0) {
            Log.d("spectrum", "set column count to $columnCount")
            mHasFixedColumnCount = true
            mFixedColumnCount = columnCount
            requestLayout()
            invalidate()
        } else {
            mHasFixedColumnCount = false
            mFixedColumnCount = -1
            requestLayout()
            invalidate()
        }
    }

    companion object {

        private const val DEFAULT_COLUMN_COUNT = 4
    }

}
