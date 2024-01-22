package com.itachi1706.cheesecakeutilities.modules.barcodetools.objects

import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode

/**
 * A singleton class whose main goals is to store the barcode caputred by the barcodecaptureactivity
 * @property barcode FirebaseVisionBarcode? Barcode Object. Can be nullable
 *
 * Created by Kenneth on 5/1/2020.
 * for com.itachi1706.cheesecakeutilities.modules.barcodeTools in CheesecakeUtilities
 */
class BarcodeHolder {
    var barcode: FirebaseVisionBarcode? = null
    fun clearBarcode() { barcode = null }
    fun hasBarcode(): Boolean { return barcode != null }

    companion object {
        private lateinit var instance: BarcodeHolder

        @JvmStatic
        fun getInstance(): BarcodeHolder {
            if (!Companion::instance.isInitialized) instance = BarcodeHolder()
            return instance
        }
    }
}