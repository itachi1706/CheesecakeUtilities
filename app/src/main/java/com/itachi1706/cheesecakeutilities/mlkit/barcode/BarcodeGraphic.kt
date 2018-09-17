// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.mitations under the License.
package com.itachi1706.cheesecakeutilities.mlkit.barcode

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.itachi1706.cheesecakeutilities.mlkit.camera.GraphicOverlay
import java.util.*

/**
 * Graphic instance for rendering barcode position, size, and ID within an associated graphic
 * overlay view.
 */
class BarcodeGraphic internal constructor(overlay: GraphicOverlay, // EXTRA METHODS
                                          val barcode: FirebaseVisionBarcode?) : GraphicOverlay.Graphic(overlay) {

    private val rectPaint: Paint
    private val barcodePaint: Paint

    init {

        rectPaint = Paint()
        rectPaint.color = TEXT_COLOR
        rectPaint.style = Paint.Style.STROKE
        rectPaint.strokeWidth = STROKE_WIDTH

        barcodePaint = Paint()
        barcodePaint.color = TEXT_COLOR
        barcodePaint.textSize = TEXT_SIZE
        // Redraw the overlay, as this graphic has been added.
        postInvalidate()
    }

    /**
     * Draws the barcode block annotations for position, size, and raw value on the supplied canvas.
     */
    override fun draw(canvas: Canvas) {
        if (barcode == null) {
            throw IllegalStateException("Attempting to draw a null barcode.")
        }

        // Draws the bounding box around the BarcodeBlock.
        val rect = RectF(barcode.boundingBox)
        rect.left = translateX(rect.left)
        rect.top = translateY(rect.top)
        rect.right = translateX(rect.right)
        rect.bottom = translateY(rect.bottom)
        canvas.drawRect(rect, rectPaint)

        // Renders the barcode at the bottom of the box.
        canvas.drawText(Objects.requireNonNull<String>(barcode.rawValue), rect.left, rect.bottom, barcodePaint)
    }

    companion object {

        private val TEXT_COLOR = Color.WHITE
        private val TEXT_SIZE = 54.0f
        private val STROKE_WIDTH = 4.0f
    }
}