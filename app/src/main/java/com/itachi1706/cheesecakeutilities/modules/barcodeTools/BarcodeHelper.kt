package com.itachi1706.cheesecakeutilities.modules.barcodeTools

import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.zxing.BarcodeFormat
import com.itachi1706.cheesecakeutilities.modules.barcodeTools.objects.BarcodeHistoryScan

/**
 * Created by Kenneth on 24/12/2017.
 * for com.itachi1706.cheesecakeutilities.modules.BarcodeTools in CheesecakeUtilities
 */
object BarcodeHelper {
    const val SP_BARCODE_GENERATED = "barcode_generate_list"
    const val SP_BARCODE_SCANNED = "barcode_scanned_list"
    @JvmStatic
    fun getFormatName(format: Int): String {
        return when (format) {
            FirebaseVisionBarcode.FORMAT_CODE_128 -> "CODE_128"
            FirebaseVisionBarcode.FORMAT_CODE_39 -> "CODE_39"
            FirebaseVisionBarcode.FORMAT_CODE_93 -> "CODE_93"
            FirebaseVisionBarcode.FORMAT_CODABAR -> "CODABAR"
            FirebaseVisionBarcode.FORMAT_DATA_MATRIX -> "DATA_MATRIX"
            FirebaseVisionBarcode.FORMAT_EAN_13 -> "EAN_13"
            FirebaseVisionBarcode.FORMAT_EAN_8 -> "EAN_8"
            FirebaseVisionBarcode.FORMAT_ITF -> "ITF"
            FirebaseVisionBarcode.FORMAT_QR_CODE -> "QR_CODE"
            FirebaseVisionBarcode.FORMAT_UPC_A -> "UPC_A"
            FirebaseVisionBarcode.FORMAT_UPC_E -> "UPC_E"
            FirebaseVisionBarcode.FORMAT_PDF417 -> "PDF417"
            FirebaseVisionBarcode.FORMAT_AZTEC -> "AZTEC"
            else -> "Unknown ($format)"
        }
    }

    private const val GEN_QR = 0
    private const val GEN_UPC_A = 1
    private const val GEN_EAN_13 = 2
    private const val GEN_CODE_39 = 3
    private const val GEN_CODE_93 = 4
    private const val GEN_CODE_128 = 5
    private const val GEN_CODABAR = 6
    private const val GEN_ITF = 7
    private const val GEN_DATA_MATRIX = 12
    private const val GEN_PDF_417 = 8
    private const val GEN_UPC_E = 9
    private const val GEN_EAN_8 = 10
    private const val GEN_AZTEC = 11
    @JvmStatic
    fun getGenerateType(generate: Int): BarcodeFormat {
        return when (generate) {
            GEN_UPC_A -> BarcodeFormat.UPC_A
            GEN_EAN_13 -> BarcodeFormat.EAN_13
            GEN_CODE_39 -> BarcodeFormat.CODE_39
            GEN_CODE_93 -> BarcodeFormat.CODE_93
            GEN_CODE_128 -> BarcodeFormat.CODE_128
            GEN_CODABAR -> BarcodeFormat.CODABAR
            GEN_ITF -> BarcodeFormat.ITF
            GEN_DATA_MATRIX -> BarcodeFormat.DATA_MATRIX // Dont want
            GEN_PDF_417 -> BarcodeFormat.PDF_417
            GEN_UPC_E -> BarcodeFormat.UPC_E
            GEN_EAN_8 -> BarcodeFormat.EAN_8
            GEN_AZTEC -> BarcodeFormat.AZTEC
            GEN_QR -> BarcodeFormat.QR_CODE
            else -> BarcodeFormat.QR_CODE
        }
    }

    private val REGEX_NUM = "^[0-9]*$".toRegex()
    private val REGEX_NUM_CAPS_ALPHA = "^[0-9A-Z-.$/+% ]*$".toRegex()
    private val REGEX_NUM_SYMBOL = "^[0-9$-:/.+]*$".toRegex()
    private val REGEX_ABCD = "^[ABCD]*$".toRegex()
    private const val ERROR_NUM_ONLY: String = "Must only contain numbers"

    /**
     * Check if value is validated
     * @param generatedCode Barcode Type
     * @param value String to validate
     * @return null if validated, else String of error message
     */
    @JvmStatic
    fun checkValidation(generatedCode: Int, value: String): String? {
        return when (generatedCode) {
            GEN_UPC_A -> {
                if (value.length != 11) "Must be 11 characters long" else if (!value.matches(REGEX_NUM)) ERROR_NUM_ONLY else null
            }
            GEN_EAN_13 -> if (value.length != 12) "Must be 12 characters long" else if (!value.matches(REGEX_NUM)) ERROR_NUM_ONLY else null
            GEN_CODE_39, GEN_CODE_93 -> if (value.matches(REGEX_NUM_CAPS_ALPHA)) null else "Can only contain 0–9, A-Z, -.$/+% and space ONLY"
            GEN_CODABAR -> {
                if (value.matches(REGEX_NUM_SYMBOL)) null
                else if (value[0].toString().matches(REGEX_ABCD) && value[value.length - 1].toString().matches(REGEX_ABCD)) null
                else "0–9, –$:/.+ only, ABCD can be used at start and end of input"
            }
            GEN_ITF -> if (!value.matches(REGEX_NUM)) ERROR_NUM_ONLY else if (value.length % 2 != 0) "Input must be of even length" else null
            GEN_UPC_E -> if (value.length != 5) "Must be 5 characters long" else if (!value.matches(REGEX_NUM)) ERROR_NUM_ONLY else null
            GEN_EAN_8 -> {
                if (value.length != 7) "Must be 7 characters long" else if (!value.matches(REGEX_NUM)) ERROR_NUM_ONLY else null
            }
            GEN_AZTEC, GEN_DATA_MATRIX, GEN_PDF_417, GEN_CODE_128, GEN_QR -> null
            else -> null
        }
    }

    @JvmStatic
    fun getValueFormat(valueFormat: Int): String {
        return when (valueFormat) {
            FirebaseVisionBarcode.TYPE_CONTACT_INFO -> "Contact Info"
            FirebaseVisionBarcode.TYPE_EMAIL -> "Email"
            FirebaseVisionBarcode.TYPE_ISBN -> "ISBN No"
            FirebaseVisionBarcode.TYPE_PHONE -> "Phone No"
            FirebaseVisionBarcode.TYPE_PRODUCT -> "Product"
            FirebaseVisionBarcode.TYPE_SMS -> "SMS"
            FirebaseVisionBarcode.TYPE_TEXT -> "Text"
            FirebaseVisionBarcode.TYPE_URL -> "URL"
            FirebaseVisionBarcode.TYPE_WIFI -> "Wi-Fi Details"
            FirebaseVisionBarcode.TYPE_GEO -> "Geo Points"
            FirebaseVisionBarcode.TYPE_CALENDAR_EVENT -> "Calendar Event"
            FirebaseVisionBarcode.TYPE_DRIVER_LICENSE -> "Driver License"
            FirebaseVisionBarcode.TYPE_UNKNOWN -> "Unknown"
            else -> "Unknown"
        }
    }

    @JvmStatic
    fun handleSpecialBarcodes(barcode: BarcodeHistoryScan): String {
        val result = StringBuilder()
        // Get all special stuff that may be null if its invalid
        val calendarEvent = barcode.calendarEvent
        val contactInfo = barcode.contactInfo
        val driverLicense = barcode.driverLicense
        val email = barcode.email
        val geoPoint = barcode.geoPoint
        val phone = barcode.phone
        val sms = barcode.sms
        val urlBookmark = barcode.url
        val wiFi = barcode.wifi
        if (calendarEvent != null) {
            result.append("\nCalendar Event\n")
            result.append("Summary: ").append(calendarEvent.summary).append("\n")
            result.append("Description: ").append(calendarEvent.description).append("\n")
            result.append("Organizer: ").append(calendarEvent.organizer).append("\n")
            result.append("Location: ").append(calendarEvent.location).append("\n")
            result.append("Status: ").append(calendarEvent.status).append("\n")
            if (calendarEvent.start != null) result.append("Start: ").append(getCalString(calendarEvent.start)).append("\n")
            if (calendarEvent.end != null) result.append("End: ").append(getCalString(calendarEvent.end)).append("\n")
        }
        if (contactInfo != null) {
            result.append("\nContact Info\n")
            result.append("From: ").append(contactInfo.title).append("\n")
            result.append("From: ").append(contactInfo.name).append("\n")
            result.append("From: ").append(contactInfo.organization).append("\n")
            if (contactInfo.phones.size > 0) {
                result.append("Phone Numbers: \n")
                for (p in contactInfo.phones) {
                    result.append("Number: ").append(p.number).append(" | Type: ")
                    when (p.type) {
                        FirebaseVisionBarcode.Phone.TYPE_FAX -> result.append("Fax\n")
                        FirebaseVisionBarcode.Phone.TYPE_HOME -> result.append("Home\n")
                        FirebaseVisionBarcode.Phone.TYPE_WORK -> result.append("Work\n")
                        FirebaseVisionBarcode.Phone.TYPE_MOBILE -> result.append("Mobile\n")
                        FirebaseVisionBarcode.Phone.TYPE_UNKNOWN -> result.append("Unknown Type\n")
                        else -> result.append("Unknown Type\n")
                    }
                }
            }
            if (contactInfo.addresses.size > 0) {
                result.append("Addresses: \n")
                for (a in contactInfo.addresses) {
                    when (a.type) {
                        FirebaseVisionBarcode.Address.TYPE_WORK -> result.append("Work: ")
                        FirebaseVisionBarcode.Address.TYPE_HOME -> result.append("Home: ")
                        FirebaseVisionBarcode.Address.TYPE_UNKNOWN -> result.append("Unknown: ")
                        else -> result.append("Unknown: ")
                    }
                    if (a.addressLines.isNotEmpty()) {
                        for (s in a.addressLines) {
                            result.append(s).append(" ")
                        }
                    } else {
                        result.append("Empty")
                    }
                    result.append("\n")
                }
            }
            if (contactInfo.emails.size > 0) {
                result.append("Emails: \n")
                for (e in contactInfo.emails) {
                    result.append("Type: ")
                    when (e.type) {
                        FirebaseVisionBarcode.Email.TYPE_HOME -> result.append("Home\n")
                        FirebaseVisionBarcode.Email.TYPE_WORK -> result.append("Work\n")
                        FirebaseVisionBarcode.Email.TYPE_UNKNOWN -> result.append("Unknown\n")
                        else -> result.append("Unknown\n")
                    }
                    result.append("From: ").append(e.address).append("\n")
                    result.append("Title: ").append(e.subject).append("\n")
                    result.append("Message: ").append(e.body).append("\n")
                    result.append("\n")
                }
            }
            if (contactInfo.urls != null && contactInfo.urls!!.isNotEmpty()) {
                result.append("Websites: \n")
                for (s in contactInfo.urls!!) {
                    result.append(s).append("\n")
                }
            }
        }
        if (driverLicense != null) {
            result.append("\nDriver License\n")
            result.append("License No: ").append(driverLicense.licenseNumber).append("\n")
            result.append("First Name: ").append(driverLicense.firstName).append("\n")
            result.append("Middle Name: ").append(driverLicense.middleName).append("\n")
            result.append("Last Name: ").append(driverLicense.lastName).append("\n")
            result.append("Gender: ").append(driverLicense.gender).append("\n")
            result.append("Date of Birth: ").append(driverLicense.birthDate).append("\n")
            result.append("Address: ").append(driverLicense.addressStreet).append("\n")
            result.append("City: ").append(driverLicense.addressCity).append("\n")
            result.append("State: ").append(driverLicense.addressState).append("\n")
            result.append("Zip: ").append(driverLicense.addressZip).append("\n")
            result.append("Document Type: ").append(driverLicense.documentType).append("\n")
            result.append("Date of Issue: ").append(driverLicense.issueDate).append("\n")
            result.append("Issued By: ").append(driverLicense.issuingCountry).append("\n")
            result.append("Expiry: ").append(driverLicense.expiryDate).append("\n")
        }
        if (email != null) {
            result.append("\nEmail Message\n")
            result.append("Type: ")
            when (email.type) {
                FirebaseVisionBarcode.Email.TYPE_HOME -> result.append("Home\n")
                FirebaseVisionBarcode.Email.TYPE_WORK -> result.append("Work\n")
                FirebaseVisionBarcode.Email.TYPE_UNKNOWN -> result.append("Unknown\n")
                else -> result.append("Unknown\n")
            }
            result.append("From: ").append(email.address).append("\n")
            result.append("Title: ").append(email.subject).append("\n")
            result.append("Message: ").append(email.body).append("\n")
        }
        if (geoPoint != null) {
            result.append("\nGeolocation Point\n")
            result.append("Latitude: ").append(geoPoint.lat).append("\n")
            result.append("Longitude: ").append(geoPoint.lng).append("\n")
        }
        if (phone != null) {
            result.append("\nPhone Number\n")
            result.append("Phone Number: ").append(phone.number).append("\n")
            result.append("Type: ")
            when (phone.type) {
                FirebaseVisionBarcode.Phone.TYPE_FAX -> result.append("Fax\n")
                FirebaseVisionBarcode.Phone.TYPE_HOME -> result.append("Home\n")
                FirebaseVisionBarcode.Phone.TYPE_WORK -> result.append("Work\n")
                FirebaseVisionBarcode.Phone.TYPE_MOBILE -> result.append("Mobile\n")
                FirebaseVisionBarcode.Phone.TYPE_UNKNOWN -> result.append("Unknown Type\n")
                else -> result.append("Unknown Type\n")
            }
        }
        if (sms != null) {
            result.append("\nSMS Message\n")
            result.append("Phone Number: ").append(sms.phoneNumber).append("\n")
            result.append("Message: ").append(sms.message).append("\n")
        }
        if (urlBookmark != null) {
            result.append("\nURL Bookmarks\n")
            result.append("Title: ").append(urlBookmark.title).append("\n")
            result.append("URL: ").append(urlBookmark.url).append("\n")
        }
        if (wiFi != null) {
            result.append("\nWIFI Details\n")
            result.append("SSID: ").append(wiFi.ssid).append("\n")
            result.append("Password: ").append(wiFi.password).append("\n")
            result.append("Encryption Type: ")
            when (wiFi.encryptionType) {
                FirebaseVisionBarcode.WiFi.TYPE_OPEN -> result.append("Open")
                FirebaseVisionBarcode.WiFi.TYPE_WEP -> result.append("WEP")
                FirebaseVisionBarcode.WiFi.TYPE_WPA -> result.append("WPA2")
                else -> result.append("Unknown")
            }
            result.append("\n")
        }
        return result.toString()
    }

    private fun getCalString(dateTime: FirebaseVisionBarcode.CalendarDateTime?): String {
        return (dateTime!!.day.toString() + "/" + dateTime.month + "/" + dateTime.year + " " + dateTime.hours
                + ":" + dateTime.minutes + ":" + dateTime.seconds)
    }
}