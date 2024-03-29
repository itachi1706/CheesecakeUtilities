package com.itachi1706.cheesecakeutilities.modules.barcodetools.fragments;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.itachi1706.cheesecakeutilities.R;
import com.itachi1706.cheesecakeutilities.modules.barcodetools.BarcodeCaptureActivity;
import com.itachi1706.cheesecakeutilities.modules.barcodetools.BarcodeHelper;
import com.itachi1706.cheesecakeutilities.modules.barcodetools.objects.BarcodeHistoryScan;
import com.itachi1706.cheesecakeutilities.modules.barcodetools.objects.BarcodeHolder;
import com.itachi1706.helperlib.helpers.LogHelper;
import com.itachi1706.helperlib.helpers.PrefHelper;
import com.itachi1706.helperlib.utils.NotifyUserUtil;

import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static android.content.Context.DEVICE_POLICY_SERVICE;

/**
 * Created by Kenneth on 24/12/2017.
 * for com.itachi1706.cheesecakeutilities.modules.BarcodeTools.Fragments in CheesecakeUtilities
 */
public class BarcodeScannerFragment extends Fragment implements BarcodeFragInterface {

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
        if (!this.getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY) ||
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

    private void updateHistory(BarcodeHistoryScan bc) {
        if (getContext() == null) return;
        SharedPreferences sp = PrefHelper.getSharedPreferences(getContext(), "BarcodeHistory");
        String bcString = sp.getString(BarcodeHelper.SP_BARCODE_SCANNED, "");
        ArrayList<BarcodeHistoryScan> array;
        Gson gson = new Gson();
        if (!bcString.isEmpty()) {
            Type listType = new TypeToken<ArrayList<BarcodeHistoryScan>>(){}.getType();
            array = gson.fromJson(bcString, listType);
        } else array = new ArrayList<>();
        array.add(bc);
        String newString = gson.toJson(array);
        sp.edit().putString(BarcodeHelper.SP_BARCODE_SCANNED, newString).apply();
    }

    // Returning barcode activity
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                FirebaseVisionBarcode barcode = BarcodeHolder.getInstance().getBarcode(); // Get barcode
                if (barcode != null) {
                    // Save Barcode
                    BarcodeHistoryScan scanBc = new BarcodeHistoryScan(barcode.getDisplayValue(), barcode.getRawValue(), barcode.getFormat(), barcode.getValueType(),
                            barcode.getEmail(), barcode.getPhone(), barcode.getSms(), barcode.getWifi(), barcode.getUrl(), barcode.getGeoPoint(), barcode.getCalendarEvent(),
                            barcode.getContactInfo(), barcode.getDriverLicense());
                    updateHistory(scanBc);
                    updateBarcode(scanBc);
                    LogHelper.i(TAG, "Saved barcode to history: " + barcode.getDisplayValue());
                } else {
                    statusMessage.setText(R.string.barcode_failure);
                    barcodeValue.setClickable(false);
                    LogHelper.d(TAG, "No barcode captured and retrieved from BarcodeHolder");
                }
            } else {
                statusMessage.setText(String.format(getString(R.string.barcode_error), CommonStatusCodes.getStatusCodeString(resultCode)));
            }
        } else if (requestCode == EXPORT_BARCODES && resultCode == Activity.RESULT_OK && data != null && data.getData() != null && getContext() != null) {
            Uri uri = data.getData();
            try {
                OutputStream os = getContext().getContentResolver().openOutputStream(uri);
                if (os == null) {
                    Toast.makeText(getContext(), "Error obtaining output stream to save to file", Toast.LENGTH_LONG).show();
                    return;
                }
                if (barcodeSaveContext == null) {
                    LogHelper.e(TAG, "Invalid save file, no barcode");
                    Toast.makeText(getContext(), "An error occurred writing barcodes to file (No Barcode Found)", Toast.LENGTH_LONG).show();
                    return;
                }
                PrintWriter pw = new PrintWriter(os);
                pw.print(barcodeSaveContext);
                barcodeSaveContext = null;
                pw.close();
                os.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.e(TAG, "Failed to save to file");
                Toast.makeText(getContext(), "An error occurred saving to file (FileNotFound)", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Failed to close output");
                Toast.makeText(getContext(), "An error occurred saving to file (IOException)", Toast.LENGTH_LONG).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void updateBarcode(BarcodeHistoryScan barcode) {
        statusMessage.setText(R.string.barcode_success);
        StringBuilder result = new StringBuilder();
        result.append("Format: ").append(BarcodeHelper.getFormatName(barcode.getFormat())).append("\n");
        result.append("Type: ").append(BarcodeHelper.getValueFormat(barcode.getValueType())).append("\n\n");
        result.append("Content: ").append(barcode.getBarcodeValue()).append("\n\n");
        result.append("Raw Value: ").append(barcode.getRawBarcodeValue()).append("\n");
        result.append(BarcodeHelper.handleSpecialBarcodes(barcode)).append("\n");
        barcodeValue.setText(result);
        barcodeValue.setClickable(true);
        barcodeValue.setOnClickListener(v -> {
            barcodeContext = barcode;
            registerForContextMenu(barcodeValue);
            barcodeValue.showContextMenu();
            unregisterForContextMenu(barcodeValue);
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            barcodeValue.setOnLongClickListener(v1 -> {
                ClipData clip = ClipData.newPlainText("barcode", barcode.getBarcodeValue());
                View.DragShadowBuilder dragShadowBuilder = new View.DragShadowBuilder(v1);
                v1.startDragAndDrop(clip, dragShadowBuilder, true, View.DRAG_FLAG_GLOBAL | View.DRAG_FLAG_GLOBAL_URI_READ |
                        View.DRAG_FLAG_GLOBAL_PERSISTABLE_URI_PERMISSION);
                return true;
            });
        }
        LogHelper.d(TAG, "Barcode read: " + barcode.getBarcodeValue());
    }

    private BarcodeHistoryScan barcodeContext = null;
    private String barcodeSaveContext = null;

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (getActivity() == null || barcodeContext == null) return;
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.modules_barcode_export_options, menu);
        menu.findItem(R.id.barcode_action_extra).setTitle(BarcodeHelper.specialBarcodeHandlingText(barcodeContext)).setEnabled(true);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        boolean result = true;
        if (barcodeContext == null || getContext() == null) {
            if (barcodeContext == null) LogHelper.e(TAG, "Error processing menu (barcodeContext empty)");
            else LogHelper.e(TAG, "Error processing menu (no Context object)");
            return true; // Error, just close
        }

        int id = item.getItemId();
        if (id == R.id.barcode_action_extra) {
            BarcodeHelper.specialBarcodeHandlingAction(barcodeContext, getContext());
        } else if (id == R.id.barcode_action_clipboard) {
            ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null) {
                ClipData clip = ClipData.newPlainText("barcode", barcodeContext.getBarcodeValue());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getContext(), "Barcode copied to clipboard", Toast.LENGTH_LONG).show();
            }
        } else if (id == R.id.barcode_action_share || id == R.id.barcode_action_share_raw) {
            String textToShare = (id == R.id.barcode_action_share) ? barcodeContext.getBarcodeValue() : barcodeContext.getRawBarcodeValue();
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, textToShare);
            startActivity(Intent.createChooser(shareIntent, "Share barcode"));
        } else if (id == R.id.barcode_action_save_file || id == R.id.barcode_action_save_file_raw) {
            barcodeSaveContext = (id == R.id.barcode_action_save_file) ? barcodeContext.getBarcodeValue() : barcodeContext.getRawBarcodeValue();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss.SSS", Locale.US);
            String defaultFileName = "scanned-barcode-" + sdf.format(new Date()) + ".txt";
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                Intent exportIntent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                exportIntent.addCategory(Intent.CATEGORY_OPENABLE);
                exportIntent.setType("text/plain");
                exportIntent.putExtra(Intent.EXTRA_TITLE, defaultFileName);
                startActivityForResult(exportIntent, EXPORT_BARCODES);
            } else {
                NotifyUserUtil.showShortDismissSnackbar(getView(), "Action not supported for devices before Android KitKat");
            }
        } else result = super.onContextItemSelected(item);

        return result;
    }

    private static final int EXPORT_BARCODES = 2;

    @Override
    public void setHistoryBarcode(@NotNull String barcodeString) {
        Log.i(TAG, "Received Barcode String to process: " + barcodeString);
        Gson gson = new Gson();
        BarcodeHistoryScan bc = gson.fromJson(barcodeString, BarcodeHistoryScan.class);
        if (bc != null) updateBarcode(bc);
    }
}
