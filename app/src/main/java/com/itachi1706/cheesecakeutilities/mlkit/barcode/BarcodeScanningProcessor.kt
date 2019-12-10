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
package com.itachi1706.cheesecakeutilities.mlkit.barcode

import android.util.Log

import com.google.android.gms.tasks.Task
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.itachi1706.cheesecakeutilities.mlkit.VisionProcessorBase
import com.itachi1706.cheesecakeutilities.mlkit.camera.FrameMetadata
import com.itachi1706.cheesecakeutilities.mlkit.camera.GraphicOverlay

import java.io.IOException

/**
 * Created by Kenneth on 17/9/2018.
 * for com.itachi1706.cheesecakeutilities.modules.BarcodeTools in CheesecakeUtilities
 */
class BarcodeScanningProcessor : VisionProcessorBase<List<FirebaseVisionBarcode>>() {

    private val detector: FirebaseVisionBarcodeDetector

    init {
        // Note that if you know which format of barcode your app is dealing with, detection will be
        // faster to specify the supported barcode formats one by one, e.g.
        // new FirebaseVisionBarcodeDetectorOptions.Builder()
        //     .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_QR_CODE)
        //     .build();
        detector = FirebaseVision.getInstance().visionBarcodeDetector
    }

    override fun stop() {
        try {
            detector.close()
        } catch (e: IOException) {
            Log.e(TAG, "Exception thrown while trying to close Barcode Detector: $e")
        }

    }

    override fun detectInImage(image: FirebaseVisionImage): Task<List<FirebaseVisionBarcode>> {
        return detector.detectInImage(image)
    }

    override fun onSuccess(
            results: List<FirebaseVisionBarcode>,
            frameMetadata: FrameMetadata,
            graphicOverlay: GraphicOverlay) {
        graphicOverlay.clear()
        for (i in results.indices) {
            val barcode = results[i]

            val barcodeGraphic = BarcodeGraphic(graphicOverlay, barcode)
            graphicOverlay.add(barcodeGraphic)
        }
    }

    override fun onFailure(e: Exception) {
        Log.e(TAG, "Barcode detection failed $e")
    }

    companion object {
        private val TAG = "BarcodeScanProc"
    }
}
