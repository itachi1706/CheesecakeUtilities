package com.itachi1706.cheesecakeutilities.modules.cameraViewer

import android.os.Build
import android.util.Size
import androidx.annotation.RequiresApi
import java.lang.Long.signum
import java.util.*

/**
 * Compares two `Size`s based on their areas.
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
internal class CompareSizesByArea : Comparator<Size> {

    // We cast here to ensure the multiplications won't overflow
    override fun compare(lhs: Size, rhs: Size) = signum(lhs.width.toLong() * lhs.height - rhs.width.toLong() * rhs.height)

}
