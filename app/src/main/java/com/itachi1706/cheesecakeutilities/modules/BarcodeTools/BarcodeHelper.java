package com.itachi1706.cheesecakeutilities.modules.BarcodeTools;

import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.zxing.BarcodeFormat;

/**
 * Created by Kenneth on 24/12/2017.
 * for com.itachi1706.cheesecakeutilities.Modules.BarcodeTools in CheesecakeUtilities
 */
public class BarcodeHelper {

    private BarcodeHelper() {
        throw new IllegalStateException("Utility class");
    }

    public static String getFormatName(int format) {
        switch (format) {
            case FirebaseVisionBarcode.FORMAT_CODE_128:
                return "CODE_128";
            case FirebaseVisionBarcode.FORMAT_CODE_39:
                return "CODE_39";
            case FirebaseVisionBarcode.FORMAT_CODE_93:
                return "CODE_93";
            case FirebaseVisionBarcode.FORMAT_CODABAR:
                return "CODABAR";
            case FirebaseVisionBarcode.FORMAT_DATA_MATRIX:
                return "DATA_MATRIX";
            case FirebaseVisionBarcode.FORMAT_EAN_13:
                return "EAN_13";
            case FirebaseVisionBarcode.FORMAT_EAN_8:
                return "EAN_8";
            case FirebaseVisionBarcode.FORMAT_ITF:
                return "ITF";
            case FirebaseVisionBarcode.FORMAT_QR_CODE:
                return "QR_CODE";
            case FirebaseVisionBarcode.FORMAT_UPC_A:
                return "UPC_A";
            case FirebaseVisionBarcode.FORMAT_UPC_E:
                return "UPC_E";
            case FirebaseVisionBarcode.FORMAT_PDF417:
                return "PDF417";
            case FirebaseVisionBarcode.FORMAT_AZTEC:
                return "AZTEC";
            default:
                return "Unknown (" + format + ")";
        }
    }

    private static final int GEN_QR = 0;
    private static final int GEN_UPC_A = 1;
    private static final int GEN_EAN_13 = 2;
    private static final int GEN_CODE_39 = 3;
    private static final int GEN_CODE_93 = 4;
    private static final int GEN_CODE_128 = 5;
    private static final int GEN_CODABAR = 6;
    private static final int GEN_ITF = 7;
    private static final int GEN_DATA_MATRIX = 12;
    private static final int GEN_PDF_417 = 8;
    private static final int GEN_UPC_E = 9;
    private static final int GEN_EAN_8 = 10;
    private static final int GEN_AZTEC = 11;

    public static BarcodeFormat getGenerateType(int generate) {
        switch (generate) {
            case GEN_UPC_A: return BarcodeFormat.UPC_A;
            case GEN_EAN_13: return BarcodeFormat.EAN_13;
            case GEN_CODE_39: return BarcodeFormat.CODE_39;
            case GEN_CODE_93: return BarcodeFormat.CODE_93;
            case GEN_CODE_128: return BarcodeFormat.CODE_128;
            case GEN_CODABAR: return BarcodeFormat.CODABAR;
            case GEN_ITF: return BarcodeFormat.ITF;
            case GEN_DATA_MATRIX: return BarcodeFormat.DATA_MATRIX; // Dont want
            case GEN_PDF_417: return BarcodeFormat.PDF_417;
            case GEN_UPC_E: return BarcodeFormat.UPC_E;
            case GEN_EAN_8: return BarcodeFormat.EAN_8;
            case GEN_AZTEC: return BarcodeFormat.AZTEC;
            case GEN_QR:
            default: return BarcodeFormat.QR_CODE;
        }
    }

    private static final String REGEX_NUM = "^[0-9]*$";
    private static final String REGEX_NUM_CAPS_ALPHA = "^[0-9A-Z-.$/+% ]*$";
    private static final String REGEX_NUM_SYMBOL = "^[0-9$-:/.+]*$";
    private static final String REGEX_ABCD = "^[ABCD]*$";
    private static final String ERROR_NUM_ONLY = "Must only contain numbers";
    /**
     * Check if value is validated
     * @param generatedCode Barcode Type
     * @param value String to validate
     * @return null if validated, else String of error message
     */
    public static String checkValidation(int generatedCode, String value) {
        String result = null;
        switch (generatedCode) {
            case GEN_UPC_A:
                if (value.length() != 11) result = "Must be 11 characters long";
                else if (!value.matches(REGEX_NUM)) result = ERROR_NUM_ONLY;
                break;
            case GEN_EAN_13:
                if (value.length() != 12) result = "Must be 12 characters long";
                else if (!value.matches(REGEX_NUM)) result = ERROR_NUM_ONLY;
                break;
            case GEN_CODE_39:
            case GEN_CODE_93:
                if (value.matches(REGEX_NUM_CAPS_ALPHA)) break;
                result = "Can only contain 0–9, A-Z, -.$/+% and space ONLY";
                break;
            case GEN_CODABAR:
                if (value.matches(REGEX_NUM_SYMBOL)) break;
                if (Character.toString(value.charAt(0)).matches(REGEX_ABCD) && Character.toString(value.charAt(value.length() - 1)).matches(REGEX_ABCD)) break;
                result = "0–9, –$:/.+ only, ABCD can be used at start and end of input";
                break;
            case GEN_ITF:
                if (!value.matches(REGEX_NUM)) result = ERROR_NUM_ONLY;
                else if (value.length() % 2 != 0) result = "Input must be of even length";
                break;
            case GEN_UPC_E:
                if (value.length() != 5) result = "Must be 5 characters long";
                else if (!value.matches(REGEX_NUM)) result = ERROR_NUM_ONLY;
                break;
            case GEN_EAN_8:
                if (value.length() != 7) result = "Must be 7 characters long";
                if (!value.matches(REGEX_NUM)) result = ERROR_NUM_ONLY;
                break;
            // No need for validation
            case GEN_AZTEC:
            case GEN_DATA_MATRIX:
            case GEN_PDF_417:
            case GEN_CODE_128:
            case GEN_QR:
            default: break;
        }
        return result;
    }

    public static String getValueFormat(int valueFormat) {
        String type;
        switch (valueFormat) {
            case FirebaseVisionBarcode.TYPE_CONTACT_INFO: type = "Contact Info"; break;
            case FirebaseVisionBarcode.TYPE_EMAIL: type = "Email"; break;
            case FirebaseVisionBarcode.TYPE_ISBN: type = "ISBN No"; break;
            case FirebaseVisionBarcode.TYPE_PHONE: type = "Phone No"; break;
            case FirebaseVisionBarcode.TYPE_PRODUCT: type = "Product"; break;
            case FirebaseVisionBarcode.TYPE_SMS: type = "SMS"; break;
            case FirebaseVisionBarcode.TYPE_TEXT: type = "Text"; break;
            case FirebaseVisionBarcode.TYPE_URL: type = "URL"; break;
            case FirebaseVisionBarcode.TYPE_WIFI: type = "Wi-Fi Details"; break;
            case FirebaseVisionBarcode.TYPE_GEO: type = "Geo Points"; break;
            case FirebaseVisionBarcode.TYPE_CALENDAR_EVENT: type = "Calendar Event"; break;
            case FirebaseVisionBarcode.TYPE_DRIVER_LICENSE: type = "Driver License"; break;
            case FirebaseVisionBarcode.TYPE_UNKNOWN:
            default: type = "Unknown";
        }
        return type;
    }

    public static String handleSpecialBarcodes(FirebaseVisionBarcode barcode) {
        StringBuilder result = new StringBuilder();
        // Get all special stuff that may be null if its invalid
        FirebaseVisionBarcode.CalendarEvent calendarEvent = barcode.getCalendarEvent();
        FirebaseVisionBarcode.ContactInfo contactInfo = barcode.getContactInfo();
        FirebaseVisionBarcode.DriverLicense driverLicense = barcode.getDriverLicense();
        FirebaseVisionBarcode.Email email = barcode.getEmail();
        FirebaseVisionBarcode.GeoPoint geoPoint = barcode.getGeoPoint();
        FirebaseVisionBarcode.Phone phone = barcode.getPhone();
        FirebaseVisionBarcode.Sms sms = barcode.getSms();
        FirebaseVisionBarcode.UrlBookmark urlBookmark = barcode.getUrl();
        FirebaseVisionBarcode.WiFi wiFi = barcode.getWifi();
        if (calendarEvent != null) {
            result.append("\nCalendar Event\n");
            result.append("Summary: ").append(calendarEvent.getSummary()).append("\n");
            result.append("Description: ").append(calendarEvent.getDescription()).append("\n");
            result.append("Organizer: ").append(calendarEvent.getOrganizer()).append("\n");
            result.append("Location: ").append(calendarEvent.getLocation()).append("\n");
            result.append("Status: ").append(calendarEvent.getStatus()).append("\n");
            if (calendarEvent.getStart() != null) result.append("Start: ").append(getCalString(calendarEvent.getStart())).append("\n");
            if (calendarEvent.getEnd() != null) result.append("End: ").append(getCalString(calendarEvent.getEnd())).append("\n");
        }
        if (contactInfo != null) {
            result.append("\nContact Info\n");
            result.append("From: ").append(contactInfo.getTitle()).append("\n");
            result.append("From: ").append(contactInfo.getName()).append("\n");
            result.append("From: ").append(contactInfo.getOrganization()).append("\n");
            if (contactInfo.getPhones().size() > 0) {
                result.append("Phone Numbers: \n");
                for (FirebaseVisionBarcode.Phone p : contactInfo.getPhones()) {
                    result.append("Number: ").append(p.getNumber()).append(" | Type: ");
                    switch (p.getType()) {
                        case FirebaseVisionBarcode.Phone.TYPE_FAX: result.append("Fax\n"); break;
                        case FirebaseVisionBarcode.Phone.TYPE_HOME: result.append("Home\n"); break;
                        case FirebaseVisionBarcode.Phone.TYPE_WORK: result.append("Work\n"); break;
                        case FirebaseVisionBarcode.Phone.TYPE_MOBILE: result.append("Mobile\n"); break;
                        case FirebaseVisionBarcode.Phone.TYPE_UNKNOWN:
                        default: result.append("Unknown Type\n"); break;
                    }
                }
            }
            if (contactInfo.getAddresses().size() > 0) {
                result.append("Addresses: \n");
                for (FirebaseVisionBarcode.Address a : contactInfo.getAddresses()) {
                    switch (a.getType()) {
                        case FirebaseVisionBarcode.Address.TYPE_WORK: result.append("Work: "); break;
                        case FirebaseVisionBarcode.Address.TYPE_HOME: result.append("Home: "); break;
                        case FirebaseVisionBarcode.Address.TYPE_UNKNOWN:
                        default: result.append("Unknown: "); break;
                    }
                    if (a.getAddressLines().length > 0) {
                        for (String s : a.getAddressLines()) {
                            result.append(s).append(" ");
                        }
                    } else {
                        result.append("Empty");
                    }
                    result.append("\n");
                }
            }
            if (contactInfo.getEmails().size() > 0) {
                result.append("Emails: \n");
                for (FirebaseVisionBarcode.Email e : contactInfo.getEmails()) {
                    result.append("Type: ");
                    switch (e.getType()) {
                        case FirebaseVisionBarcode.Email.TYPE_HOME: result.append("Home\n"); break;
                        case FirebaseVisionBarcode.Email.TYPE_WORK: result.append("Work\n"); break;
                        case FirebaseVisionBarcode.Email.TYPE_UNKNOWN:
                        default: result.append("Unknown\n"); break;
                    }
                    result.append("From: ").append(e.getAddress()).append("\n");
                    result.append("Title: ").append(e.getSubject()).append("\n");
                    result.append("Message: ").append(e.getBody()).append("\n");
                    result.append("\n");
                }
            }
            if (contactInfo.getUrls() != null && contactInfo.getUrls().length > 0) {
                result.append("Websites: \n");
                for (String s : contactInfo.getUrls()) {
                    result.append(s).append("\n");
                }
            }
        }
        if (driverLicense != null) {
            result.append("\nDriver License\n");
            result.append("License No: ").append(driverLicense.getLicenseNumber()).append("\n");
            result.append("First Name: ").append(driverLicense.getFirstName()).append("\n");
            result.append("Middle Name: ").append(driverLicense.getMiddleName()).append("\n");
            result.append("Last Name: ").append(driverLicense.getLastName()).append("\n");
            result.append("Gender: ").append(driverLicense.getGender()).append("\n");
            result.append("Date of Birth: ").append(driverLicense.getBirthDate()).append("\n");
            result.append("Address: ").append(driverLicense.getAddressStreet()).append("\n");
            result.append("City: ").append(driverLicense.getAddressCity()).append("\n");
            result.append("State: ").append(driverLicense.getAddressState()).append("\n");
            result.append("Zip: ").append(driverLicense.getAddressZip()).append("\n");
            result.append("Document Type: ").append(driverLicense.getDocumentType()).append("\n");
            result.append("Date of Issue: ").append(driverLicense.getIssueDate()).append("\n");
            result.append("Issued By: ").append(driverLicense.getIssuingCountry()).append("\n");
            result.append("Expiry: ").append(driverLicense.getExpiryDate()).append("\n");
        }
        if (email != null) {
            result.append("\nEmail Message\n");
            result.append("Type: ");
            switch (email.getType()) {
                case FirebaseVisionBarcode.Email.TYPE_HOME: result.append("Home\n"); break;
                case FirebaseVisionBarcode.Email.TYPE_WORK: result.append("Work\n"); break;
                case FirebaseVisionBarcode.Email.TYPE_UNKNOWN:
                default: result.append("Unknown\n"); break;
            }
            result.append("From: ").append(email.getAddress()).append("\n");
            result.append("Title: ").append(email.getSubject()).append("\n");
            result.append("Message: ").append(email.getBody()).append("\n");
        }
        if (geoPoint != null) {
            result.append("\nGeolocation Point\n");
            result.append("Latitude: ").append(geoPoint.getLat()).append("\n");
            result.append("Longitude: ").append(geoPoint.getLng()).append("\n");
        }
        if (phone != null) {
            result.append("\nPhone Number\n");
            result.append("Phone Number: ").append(phone.getNumber()).append("\n");
            result.append("Type: ");
            switch (phone.getType()) {
                case FirebaseVisionBarcode.Phone.TYPE_FAX: result.append("Fax\n"); break;
                case FirebaseVisionBarcode.Phone.TYPE_HOME: result.append("Home\n"); break;
                case FirebaseVisionBarcode.Phone.TYPE_WORK: result.append("Work\n"); break;
                case FirebaseVisionBarcode.Phone.TYPE_MOBILE: result.append("Mobile\n"); break;
                case FirebaseVisionBarcode.Phone.TYPE_UNKNOWN:
                default: result.append("Unknown Type\n"); break;
            }
        }
        if (sms != null) {
            result.append("\nSMS Message\n");
            result.append("Phone Number: ").append(sms.getPhoneNumber()).append("\n");
            result.append("Message: ").append(sms.getMessage()).append("\n");
        }
        if (urlBookmark != null) {
            result.append("\nURL Bookmarks\n");
            result.append("Title: ").append(urlBookmark.getTitle()).append("\n");
            result.append("URL: ").append(urlBookmark.getUrl()).append("\n");
        }
        if (wiFi != null) {
            result.append("\nWIFI Details\n");
            result.append("SSID: ").append(wiFi.getSsid()).append("\n");
            result.append("Password: ").append(wiFi.getPassword()).append("\n");
            result.append("Encryption Type: ");
            switch (wiFi.getEncryptionType()) {
                case FirebaseVisionBarcode.WiFi.TYPE_OPEN: result.append("Open"); break;
                case FirebaseVisionBarcode.WiFi.TYPE_WEP: result.append("WEP"); break;
                case FirebaseVisionBarcode.WiFi.TYPE_WPA: result.append("WPA2"); break;
                default: result.append("Unknown"); break;
            }
            result.append("\n");
        }

        return result.toString();
    }

    private static String getCalString(FirebaseVisionBarcode.CalendarDateTime dateTime) {
        return dateTime.getDay() + "/" + dateTime.getMonth() + "/" + dateTime.getYear() + " " + dateTime.getHours()
                + ":" + dateTime.getMinutes() + ":" + dateTime.getSeconds();
    }
}
