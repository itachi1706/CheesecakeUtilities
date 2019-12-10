package com.itachi1706.cheesecakeutilities.huh.com.thebluealliance.spectrum.internal

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.*
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.ColorInt
import com.itachi1706.cheesecakeutilities.R
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class ColorItem : FrameLayout, View.OnClickListener {

    /**
     * [EventBus] used internally for inter-component communication
     */
    private lateinit var mEventBus: EventBus

    private var mItemCheckmark: ImageView? = null
    @ColorInt
    private var mColor: Int = 0
    private var mIsSelected = false
    private var mOutlineWidth = 0

    constructor(context: Context, @ColorInt color: Int, isSelected: Boolean, eventBus: EventBus) : super(context) {
        mColor = color
        mIsSelected = isSelected
        mEventBus = eventBus

        init()
        setChecked(mIsSelected)
    }

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    private fun updateDrawables() {
        foreground = createForegroundDrawable()
        background = createBackgroundDrawable()
    }

    private fun init() {
        updateDrawables()

        mEventBus.register(this)
        setOnClickListener(this)

        LayoutInflater.from(context).inflate(R.layout.spectrum_color_item, this, true)
        mItemCheckmark = findViewById(R.id.selected_checkmark)
        mItemCheckmark!!.setColorFilter(if (ColorUtil.isColorDark(mColor)) Color.WHITE else Color.BLACK)
    }

    /**
     * Change the size of the outlining
     *
     * @param width in px
     */
    fun setOutlineWidth(width: Int) {
        mOutlineWidth = width
        updateDrawables()
    }

    fun setChecked(checked: Boolean) {
        val oldChecked = mIsSelected
        mIsSelected = checked

        if (!oldChecked && mIsSelected) {
            // Animate checkmark appearance

            setItemCheckmarkAttributes(0.0f)
            mItemCheckmark!!.visibility = View.VISIBLE

            mItemCheckmark!!.animate()
                    .alpha(1.0f)
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(250)
                    .setListener(object : AnimatorListenerAdapter() {

                        override fun onAnimationEnd(animation: Animator) {
                            setItemCheckmarkAttributes(1.0f)
                        }
                    }).start()
        } else if (oldChecked && !mIsSelected) {
            // Animate checkmark disappearance

            mItemCheckmark!!.visibility = View.VISIBLE
            setItemCheckmarkAttributes(1.0f)

            mItemCheckmark!!.animate()
                    .alpha(0.0f)
                    .scaleX(0.0f)
                    .scaleY(0.0f)
                    .setDuration(250)
                    .setListener(object : AnimatorListenerAdapter() {

                        override fun onAnimationEnd(animation: Animator) {
                            mItemCheckmark!!.visibility = View.INVISIBLE
                            setItemCheckmarkAttributes(0.0f)
                        }
                    }).start()
        } else {
            // Just sync the view's visibility
            updateCheckmarkVisibility()
        }
    }

    private fun updateCheckmarkVisibility() {
        mItemCheckmark!!.visibility = if (mIsSelected) View.VISIBLE else View.INVISIBLE
        setItemCheckmarkAttributes(1.0f)
    }

    /**
     * Convenience method for simultaneously setting the alpha, X scale, and Y scale of a view
     *
     * @param value the value to be set
     */
    private fun setItemCheckmarkAttributes(value: Float) {
        mItemCheckmark!!.alpha = value
        mItemCheckmark!!.scaleX = value
        mItemCheckmark!!.scaleY = value
    }

    @Subscribe
    fun onSelectedColorChanged(event: SelectedColorChangedEvent) {
        setChecked(event.selectedColor == mColor)
    }

    override fun onClick(v: View) {
        mEventBus.post(SelectedColorChangedEvent(mColor))
    }

    private fun createBackgroundDrawable(): Drawable {
        val mask = GradientDrawable()
        mask.shape = GradientDrawable.OVAL
        if (mOutlineWidth != 0) {
            mask.setStroke(mOutlineWidth, if (ColorUtil.isColorDark(mColor)) Color.WHITE else Color.BLACK)
        }
        mask.setColor(mColor)
        return mask
    }

    private fun createForegroundDrawable(): Drawable {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Use a ripple drawable
            val mask = GradientDrawable()
            mask.shape = GradientDrawable.OVAL
            mask.setColor(Color.BLACK)

            return RippleDrawable(ColorStateList.valueOf(ColorUtil.getRippleColor(mColor)), null, mask)
        } else {
            // Use a translucent foreground
            val foreground = StateListDrawable()
            foreground.alpha = 80
            foreground.setEnterFadeDuration(250)
            foreground.setExitFadeDuration(250)

            val mask = GradientDrawable()
            mask.shape = GradientDrawable.OVAL
            mask.setColor(ColorUtil.getRippleColor(mColor))
            foreground.addState(intArrayOf(android.R.attr.state_pressed), mask)

            foreground.addState(intArrayOf(), ColorDrawable(Color.TRANSPARENT))

            return foreground
        }
    }
}