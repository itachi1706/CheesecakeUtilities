/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.itachi1706.cheesecakeutilities.Modules.BarcodeTools;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.gson.Gson;
import com.itachi1706.cheesecakeutilities.Modules.BarcodeTools.mlkit.BarcodeGraphic;
import com.itachi1706.cheesecakeutilities.Modules.BarcodeTools.mlkit.BarcodeScanningProcessor;
import com.itachi1706.cheesecakeutilities.Modules.BarcodeTools.ui.camera.CameraSource;
import com.itachi1706.cheesecakeutilities.Modules.BarcodeTools.ui.camera.CameraSourcePreview;
import com.itachi1706.cheesecakeutilities.Modules.BarcodeTools.ui.camera.GraphicOverlay;
import com.itachi1706.cheesecakeutilities.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity for the multi-tracker app.  This app detects barcodes and displays the value with the
 * rear facing camera. During detection overlay graphics are drawn to indicate the position,
 * size, and ID of each barcode.
 */
public final class BarcodeCaptureActivity extends AppCompatActivity implements BarcodeGraphicTracker.BarcodeUpdateListener {
    private static final String TAG = "Barcode-reader";

    // permission request codes need to be < 256
    private static final int PERMISSION_REQUESTS = 1;

    // constants used to pass extra data in the intent
    public static final String BARCODE_OBJECT = "Barcode";

    private CameraSource mCameraSource;
    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;

    // helper objects for detecting taps and pinches.
    private GestureDetector gestureDetector;

    /**
     * Initializes the UI and creates the detector pipeline.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.barcode_capture);

        mPreview = findViewById(R.id.preview);
        if (mPreview == null) Log.d(TAG, "Preview is null");
        mGraphicOverlay = findViewById(R.id.graphicOverlay);
        if (mGraphicOverlay == null) Log.d(TAG, "graphicOverlay is null");

        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        if (allPermissionsGranted()) {
            // TODO: Remove arg
            createCameraSource(); // Barcode Detected
        } else {
            getRuntimePermissions();
        }

        // TODO: Might not be needed
        gestureDetector = new GestureDetector(this, new CaptureGestureListener());

        Snackbar.make(mGraphicOverlay, "Tap to capture. Pinch/Stretch to zoom",
                Snackbar.LENGTH_LONG)
                .show();
    }

    @SuppressLint("InlinedApi")
    private void createCameraSource() {
        // If there's no existing cameraSource, create one.
        if (mCameraSource == null) {
            mCameraSource = new CameraSource(this, mGraphicOverlay);
        }

        mCameraSource.setMachineLearningFrameProcessor(new BarcodeScanningProcessor());
    }

    /**
     * Starts or restarts the camera source, if it exists. If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() {
        if (mCameraSource != null) {
            try {
                if (mPreview == null) {
                    Log.d(TAG, "resume: Preview is null");
                }
                if (mGraphicOverlay == null) {
                    Log.d(TAG, "resume: graphOverlay is null");
                }
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        boolean c = gestureDetector.onTouchEvent(e);

        return c || super.onTouchEvent(e);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        startCameraSource();
    }

    /** Stops the camera. */
    @Override
    protected void onPause() {
        super.onPause();
        mPreview.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCameraSource != null) {
            mCameraSource.release();
        }
    }

    private String[] getRequiredPermissions() {
        try {
            PackageInfo info =
                    this.getPackageManager()
                            .getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] ps = info.requestedPermissions;
            if (ps != null && ps.length > 0) {
                return ps;
            } else {
                return new String[0];
            }
        } catch (Exception e) {
            return new String[0];
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                return false;
            }
        }
        return true;
    }

    private void getRuntimePermissions() {
        List<String> allNeededPermissions = new ArrayList<>();
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                allNeededPermissions.add(permission);
            }
        }

        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this, allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.i(TAG, "Permission granted!");
        if (allPermissionsGranted()) {
            createCameraSource();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Barcode Scanner")
                    .setMessage(R.string.no_camera_permission)
                    .setPositiveButton(R.string.ok, (dialog, id) -> finish())
                    .show();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private static boolean isPermissionGranted(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission granted: " + permission);
            return true;
        }
        Log.i(TAG, "Permission NOT granted: " + permission);
        return false;
    }

    /**
     * onTap returns the tapped barcode result to the calling Activity.
     *
     * @param rawX - the raw position of the tap
     * @param rawY - the raw position of the tap.
     * @return true if the activity is ending.
     */
    private boolean onTap(float rawX, float rawY) {
        // Find tap point in preview frame coordinates.
        int[] location = new int[2];
        mGraphicOverlay.getLocationOnScreen(location);
        float x = (rawX - location[0]) / mGraphicOverlay.getWidthScaleFactor();
        float y = (rawY - location[1]) / mGraphicOverlay.getHeightScaleFactor();

        // Find the barcode whose center is closest to the tapped point.
        FirebaseVisionBarcode best = null;
        float bestDistance = Float.MAX_VALUE;
        for (GraphicOverlay.Graphic mGraphic : mGraphicOverlay.getGraphics()) {
            if (!(mGraphic instanceof BarcodeGraphic)) continue;
            BarcodeGraphic graphic = (BarcodeGraphic) mGraphic;
            FirebaseVisionBarcode barcode = graphic.getBarcode();
            if (barcode.getBoundingBox().contains((int) x, (int) y)) {
                // Exact hit, no need to keep looking.
                best = barcode;
                break;
            }
            float dx = x - barcode.getBoundingBox().centerX();
            float dy = y - barcode.getBoundingBox().centerY();
            float distance = (dx * dx) + (dy * dy);  // actually squared distance
            if (distance < bestDistance) {
                best = barcode;
                bestDistance = distance;
            }
        }

        if (best != null) {
            Gson gson = new Gson();
            String json = gson.toJson(best);
            Intent data = new Intent();
            data.putExtra(BARCODE_OBJECT, json);
            setResult(CommonStatusCodes.SUCCESS, data);
            finish();
            return true;
        }
        return false;
    }

    private class CaptureGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return onTap(e.getRawX(), e.getRawY()) || super.onSingleTapConfirmed(e);
        }
    }

    @Override
    public void onBarcodeDetected(Barcode barcode) {
        //do something with barcode data returned
    }
}