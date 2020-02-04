package com.itachi1706.cheesecakeutilities.modules.barcodeTools.objects

import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode

/**
 * Created by Kenneth on 15/1/2020.
 * for com.itachi1706.cheesecakeutilities.modules.barcodeTools.objects in CheesecakeUtilities
 */
data class BarcodeHistoryScan(val barcodeValue:String = "", val rawBarcodeValue: String = "", val format: Int = FirebaseVisionBarcode.FORMAT_ALL_FORMATS,
                              val valueType: Int = FirebaseVisionBarcode.TYPE_UNKNOWN, val email: FirebaseVisionBarcode.Email? = null,
                              val phone: FirebaseVisionBarcode.Phone? = null, val sms: FirebaseVisionBarcode.Sms? = null, val wifi: FirebaseVisionBarcode.WiFi? = null,
                              val url: FirebaseVisionBarcode.UrlBookmark? = null, val geoPoint: FirebaseVisionBarcode.GeoPoint? = null, val calendarEvent: FirebaseVisionBarcode.CalendarEvent? = null,
                              val contactInfo: FirebaseVisionBarcode.ContactInfo? = null, val driverLicense: FirebaseVisionBarcode.DriverLicense? = null) : BarcodeHistory()
