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
// limitations under the License.
package com.itachi1706.cheesecakeutilities.mlkit

import android.graphics.Bitmap
import android.media.Image
import android.os.Build
import androidx.annotation.RequiresApi

import com.google.android.gms.tasks.Task
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.itachi1706.cheesecakeutilities.mlkit.camera.FrameMetadata
import com.itachi1706.cheesecakeutilities.mlkit.camera.GraphicOverlay
import com.itachi1706.cheesecakeutilities.mlkit.camera.VisionImageProcessor

import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

/**
 *
 * Abstract base class for ML Kit frame processors. Subclasses need to implement [ ][.onSuccess] to define what they want to with the detection
 * results and [.detectInImage] to specify the detector object.
 *
 * @param <T> The type of the detected feature.
</T> */
abstract class VisionProcessorBase<T> protected constructor() : VisionImageProcessor {

    // Whether we should ignore process(). This is usually caused by feeding input data faster than
    // the model can handle.
    private val shouldThrottle = AtomicBoolean(false)

    override fun process(
            data: ByteBuffer, frameMetadata: FrameMetadata, graphicOverlay: GraphicOverlay) {
        if (shouldThrottle.get()) {
            return
        }
        val metadata = FirebaseVisionImageMetadata.Builder()
                .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                .setWidth(frameMetadata.width)
                .setHeight(frameMetadata.height)
                .setRotation(frameMetadata.rotation)
                .build()

        detectInVisionImage(
                FirebaseVisionImage.fromByteBuffer(data, metadata), frameMetadata, graphicOverlay)
    }

    // Bitmap version
    override fun process(bitmap: Bitmap, graphicOverlay: GraphicOverlay) {
        if (shouldThrottle.get()) {
            return
        }
        detectInVisionImage(FirebaseVisionImage.fromBitmap(bitmap), null, graphicOverlay)
    }

    /**
     * Detects feature from given media.Image
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    override fun process(bitmap: Image, rotation: Int, graphicOverlay: GraphicOverlay) {
        if (shouldThrottle.get()) {
            return
        }
        // This is for overlay display's usage
        val frameMetadata = FrameMetadata.Builder().setWidth(bitmap.width).setHeight(bitmap.height).build()
        val fbVisionImage = FirebaseVisionImage.fromMediaImage(bitmap, rotation)
        detectInVisionImage(fbVisionImage, frameMetadata, graphicOverlay)
    }

    private fun detectInVisionImage(
            image: FirebaseVisionImage,
            metadata: FrameMetadata?,
            graphicOverlay: GraphicOverlay) {
        detectInImage(image)
                .addOnSuccessListener { results ->
                    shouldThrottle.set(false)
                    this@VisionProcessorBase.onSuccess(results, metadata!!,
                            graphicOverlay)
                }
                .addOnFailureListener { e ->
                    shouldThrottle.set(false)
                    this@VisionProcessorBase.onFailure(e)
                }
        // Begin throttling until this frame of input has been processed, either in onSuccess or
        // onFailure.
        shouldThrottle.set(true)
    }

    override fun stop() {}

    protected abstract fun detectInImage(image: FirebaseVisionImage): Task<T>

    protected abstract fun onSuccess(
            results: T,
            frameMetadata: FrameMetadata,
            graphicOverlay: GraphicOverlay)

    protected abstract fun onFailure(e: Exception)
}
