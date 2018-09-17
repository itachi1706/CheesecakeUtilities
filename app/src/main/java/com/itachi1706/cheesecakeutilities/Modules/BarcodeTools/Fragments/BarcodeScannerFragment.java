package com.itachi1706.cheesecakeutilities.Modules.BarcodeTools.Fragments;

import android.app.admin.DevicePolicyManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.gson.Gson;
import com.itachi1706.cheesecakeutilities.Modules.BarcodeTools.BarcodeCaptureActivity;
import com.itachi1706.cheesecakeutilities.Modules.BarcodeTools.BarcodeHelper;
import com.itachi1706.cheesecakeutilities.R;

import static android.content.Context.DEVICE_POLICY_SERVICE;

/**
 * Created by Kenneth on 24/12/2017.
 * for com.itachi1706.cheesecakeutilities.Modules.BarcodeTools.Fragments in CheesecakeUtilities
 */

public class BarcodeScannerFragment extends Fragment {

    // use a compound button so either checkbox or switch widgets work.
    private CompoundButton useFlash;
    private Button scan;
    private TextView statusMessage;
    private TextView barcodeValue;

    private static final int RC_BARCODE_CAPTURE = 9001;
    private static final String TAG = "BarcodeScanner";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_barcode_scanner, container, false);

        statusMessage = v.findViewById(R.id.status_message);
        barcodeValue = v.findViewById(R.id.barcode_value);
        barcodeValue.setMovementMethod(new ScrollingMovementMethod());
        useFlash = v.findViewById(R.id.use_flash);
        scan = v.findViewById(R.id.read_barcode);
        scan.setOnClickListener(view -> {
            // launch barcode activity.
            Intent intent = new Intent(getActivity(), BarcodeCaptureActivity.class);
            intent.putExtra(BarcodeCaptureActivity.USE_FLASH, useFlash.isChecked());
            startActivityForResult(intent, RC_BARCODE_CAPTURE);
        });
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Check for camera source
        //noinspection ConstantConditions
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getActivity().getSystemService(DEVICE_POLICY_SERVICE);
        if (!this.getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA) ||
                (devicePolicyManager != null && devicePolicyManager.getCameraDisabled(null))) {
            // Disables stuff
            scan.setEnabled(false);
            statusMessage.setText(R.string.no_camera_hardware);
        } else {
            // Enable stuff
            scan.setEnabled(true);
            statusMessage.setText(getString(R.string.barcode_header));
        }
    }

    // Returning barcode activity
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Gson gson = new Gson();
                    String json = data.getStringExtra(BarcodeCaptureActivity.BARCODE_OBJECT);
                    FirebaseVisionBarcode barcode = gson.fromJson(json, FirebaseVisionBarcode.class);
                    statusMessage.setText(R.string.barcode_success);
                    StringBuilder result = new StringBuilder();
                    result.append("Format: ").append(BarcodeHelper.getFormatName(barcode.getFormat())).append("\n");
                    result.append("Type: ").append(BarcodeHelper.getValueFormat(barcode.getValueType())).append("\n\n");
                    result.append("Content: ").append(barcode.getDisplayValue()).append("\n\n");
                    result.append("Raw Value: ").append(barcode.getRawValue()).append("\n");
                    result.append(handleSpecialBarcodes(barcode)).append("\n");
                    barcodeValue.setText(result);
                    barcodeValue.setClickable(true);
                    barcodeValue.setOnClickListener(v -> {
                        ClipboardManager clipboard = (ClipboardManager) v.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                        if (clipboard != null) {
                            ClipData clip = ClipData.newPlainText("barcode", barcode.getDisplayValue());
                            clipboard.setPrimaryClip(clip);
                            Toast.makeText(v.getContext(), "Barcode copied to clipboard", Toast.LENGTH_LONG).show();
                        }
                    });
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        barcodeValue.setOnLongClickListener(v1 -> {
                            ClipData clip = ClipData.newPlainText("barcode", barcode.getDisplayValue());
                            View.DragShadowBuilder dragShadowBuilder = new View.DragShadowBuilder(v1);
                            v1.startDragAndDrop(clip, dragShadowBuilder, true, View.DRAG_FLAG_GLOBAL | View.DRAG_FLAG_GLOBAL_URI_READ |
                                    View.DRAG_FLAG_GLOBAL_PERSISTABLE_URI_PERMISSION);
                            return true;
                        });
                    }
                    Log.d(TAG, "Barcode read: " + barcode.getDisplayValue());
                } else {
                    statusMessage.setText(R.string.barcode_failure);
                    barcodeValue.setClickable(false);
                    Log.d(TAG, "No barcode captured, intent data is null");
                }
            } else {
                statusMessage.setText(String.format(getString(R.string.barcode_error),
                        CommonStatusCodes.getStatusCodeString(resultCode)));
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private String handleSpecialBarcodes(FirebaseVisionBarcode barcode) {
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
                        case FirebaseVisionBarcode.Address.TYPE_WORK: result.append("Work: ");
                        case FirebaseVisionBarcode.Address.TYPE_HOME: result.append("Home: ");
                        case FirebaseVisionBarcode.Address.TYPE_UNKNOWN: result.append("Unknown: ");
                        default:
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

    private String getCalString(FirebaseVisionBarcode.CalendarDateTime dateTime) {
        return dateTime.getDay() + "/" + dateTime.getMonth() + "/" + dateTime.getYear() + " " + dateTime.getHours()
                + ":" + dateTime.getMinutes() + ":" + dateTime.getSeconds();
    }
}
