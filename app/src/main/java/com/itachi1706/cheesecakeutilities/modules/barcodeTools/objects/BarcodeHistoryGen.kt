package com.itachi1706.cheesecakeutilities.modules.barcodeTools.objects

import com.google.zxing.BarcodeFormat

/**
 * Created by Kenneth on 15/1/2020.
 * for com.itachi1706.cheesecakeutilities.modules.barcodeTools.objects in CheesecakeUtilities
 */
data class BarcodeHistoryGen(val text:String = "", val format: BarcodeFormat = BarcodeFormat.QR_CODE) : BarcodeHistory()