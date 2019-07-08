package com.itachi1706.cheesecakeutilities.extlibs.com.thebluealliance.spectrum.internal

import androidx.annotation.ColorInt

/**
 * Represents a newly-selected color; used with [org.greenrobot.eventbus.EventBus] for
 * internal communication.
 */
class SelectedColorChangedEvent(@param:ColorInt @field:ColorInt @get:ColorInt val selectedColor: Int)