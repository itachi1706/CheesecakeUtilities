package com.itachi1706.cheesecakeutilities.Modules.BarcodeTools;

import com.google.android.gms.vision.barcode.Barcode;
import com.google.zxing.BarcodeFormat;

/**
 * Created by Kenneth on 24/12/2017.
 * for com.itachi1706.cheesecakeutilities.Modules.BarcodeTools in CheesecakeUtilities
 */
public class BarcodeHelper {
    public static String getFormatName(int format) {
        switch (format) {
            case Barcode.CODE_128:
                return "CODE_128";
            case Barcode.CODE_39:
                return "CODE_39";
            case Barcode.CODE_93:
                return "CODE_93";
            case Barcode.CODABAR:
                return "CODABAR";
            case Barcode.DATA_MATRIX:
                return "DATA_MATRIX";
            case Barcode.EAN_13:
                return "EAN_13";
            case Barcode.EAN_8:
                return "EAN_8";
            case Barcode.ITF:
                return "ITF";
            case Barcode.QR_CODE:
                return "QR_CODE";
            case Barcode.UPC_A:
                return "UPC_A";
            case Barcode.UPC_E:
                return "UPC_E";
            case Barcode.PDF417:
                return "PDF417";
            case Barcode.AZTEC:
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
    /**
     * Check if value is validated
     * @param generatedCode Barcode Type
     * @param value String to validate
     * @return null if validated, else String of error message
     */
    public static String checkValidation(int generatedCode, String value) {
        switch (generatedCode) {
            case GEN_UPC_A:
                if (value.length() != 11) return "Must be 11 characters long";
                if (value.matches(REGEX_NUM)) return null;
                return "Must only contain numbers";
            case GEN_EAN_13:
                if (value.length() != 12) return "Must be 12 characters long";
                if (value.matches(REGEX_NUM)) return null;
                return "Must only contain numbers";
            case GEN_CODE_39:
            case GEN_CODE_93:
                if (value.matches(REGEX_NUM_CAPS_ALPHA)) return null;
                return "Can only contain 0–9, A-Z, -.$/+% and space ONLY";
            case GEN_CODABAR:
                if (value.matches(REGEX_NUM_SYMBOL)) return null;
                if (Character.toString(value.charAt(0)).matches(REGEX_ABCD) && Character.toString(value.charAt(value.length() - 1)).matches(REGEX_ABCD)) return null;
                return "0–9, –$:/.+ only, ABCD can be used at start and end of input";
            case GEN_ITF:
                if (!value.matches(REGEX_NUM)) return "Must only contain numbers";
                if (value.length() % 2 != 0) return "Input must be of even length";
                return null;
            case GEN_UPC_E:
                if (value.length() != 5) return "Must be 5 characters long";
                if (value.matches(REGEX_NUM)) return null;
                else return "Must only contain numbers";
            case GEN_EAN_8:
                if (value.length() != 7) return "Must be 7 characters long";
                if (value.matches(REGEX_NUM)) return null;
                else return "Must only contain numbers";
            // No need for validation
            case GEN_AZTEC:
            case GEN_DATA_MATRIX:
            case GEN_PDF_417:
            case GEN_CODE_128:
            case GEN_QR:
            default: return null;
        }
    }
}
