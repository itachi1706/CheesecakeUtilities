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

    public static final int GEN_QR = 0;
    public static final int GEN_UPC_A = 1;
    public static final int GEN_EAN_13 = 2;
    public static final int GEN_CODE_39 = 3;
    public static final int GEN_CODE_93 = 4;
    public static final int GEN_CODE_128 = 5;
    public static final int GEN_CODABAR = 6;
    public static final int GEN_ITF = 7;
    public static final int GEN_DATA_MATRIX = 8;
    public static final int GEN_PDF_417 = 9;
    public static final int GEN_UPC_E = 10;
    public static final int GEN_EAN_8 = 11;
    public static final int GEN_AZTEC = 12;
    public static final int GEN_MAXICODE = 13;
    public static final int GEN_RSS_14 = 14;
    public static final int GEN_RSS_EXPANDED = 15;
    public static final int GEN_UPC_EAN_EXTENSION = 16;

    public static BarcodeFormat getGenerateType(int generate) {
        switch (generate) {
            case GEN_UPC_A: return BarcodeFormat.UPC_A;
            case GEN_EAN_13: return BarcodeFormat.EAN_13;
            case GEN_CODE_39: return BarcodeFormat.CODE_39;
            case GEN_CODE_93: return BarcodeFormat.CODE_93;
            case GEN_CODE_128: return BarcodeFormat.CODE_128;
            case GEN_CODABAR: return BarcodeFormat.CODABAR;
            case GEN_ITF: return BarcodeFormat.ITF;
            case GEN_DATA_MATRIX: return BarcodeFormat.DATA_MATRIX;
            case GEN_PDF_417: return BarcodeFormat.PDF_417;
            case GEN_UPC_E: return BarcodeFormat.UPC_E;
            case GEN_EAN_8: return BarcodeFormat.EAN_8;
            case GEN_AZTEC: return BarcodeFormat.AZTEC;
            case GEN_MAXICODE: return BarcodeFormat.MAXICODE;
            case GEN_RSS_14: return BarcodeFormat.RSS_14;
            case GEN_RSS_EXPANDED: return BarcodeFormat.RSS_EXPANDED;
            case GEN_UPC_EAN_EXTENSION: return BarcodeFormat.UPC_EAN_EXTENSION;
            case GEN_QR:
            default: return BarcodeFormat.QR_CODE;
        }
    }
}
