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
package com.itachi1706.cheesecakeutilities.modules.barcodetools;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.itachi1706.cheesecakeutilities.R;
import com.itachi1706.cheesecakeutilities.mlkit.barcode.BarcodeGraphic;
import com.itachi1706.cheesecakeutilities.mlkit.barcode.BarcodeScanningProcessor;
import com.itachi1706.cheesecakeutilities.mlkit.camera.CameraSource;
import com.itachi1706.cheesecakeutilities.mlkit.camera.CameraSourcePreview;
import com.itachi1706.cheesecakeutilities.mlkit.camera.GraphicOverlay;
import com.itachi1706.cheesecakeutilities.modules.barcodetools.objects.BarcodeHolder;
import com.itachi1706.helperlib.helpers.LogHelper;

import java.io.IOException;

import static com.itachi1706.cheesecakeutilities.util.CommonMethods.logPermError;

/**
 * Activity for the multi-tracker app.  This app detects barcodes and displays the value with the
 * rear facing camera. During detection overlay graphics are drawn to indicate the position,
 * size, and ID of each barcode.
 */
public final class BarcodeCaptureActivity extends AppCompatActivity {
    private static final String TAG = "Barcode-reader";

    // permission request codes need to be < 256
    private static final int RC_CAMERA_PERMISSION = 1;

    // constants used to pass extra data in the intent
    public static final String USE_FLASH = "UseFlash";

    private CameraSource mCameraSource;
    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;

    // helper objects for detecting taps and pinches.
    private GestureDetector gestureDetector;

    private boolean useFlash;

    /**
     * Initializes the UI and creates the detector pipeline.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.barcode_capture);

        mPreview = findViewById(R.id.preview);
        if (mPreview == null) LogHelper.d(TAG, "Preview is null");
        mGraphicOverlay = findViewById(R.id.graphicOverlay);
        if (mGraphicOverlay == null) LogHelper.d(TAG, "graphicOverlay is null");

        useFlash = getIntent().getBooleanExtra(USE_FLASH, false);

        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            createCameraSource(useFlash); // Barcode Detected
        } else {
            requestCameraPermission();
        }

        gestureDetector = new GestureDetector(this, new CaptureGestureListener());

        Snackbar.make(mGraphicOverlay, "Tap to select a barcode",
                Snackbar.LENGTH_LONG)
                .show();
    }

    @SuppressLint("InlinedApi")
    private void createCameraSource(boolean useFlash) {
        // If there's no existing cameraSource, create one.
        if (mCameraSource == null) {
            mCameraSource = new CameraSource(this, mGraphicOverlay, useFlash);
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
                    LogHelper.d(TAG, "resume: Preview is null");
                    return;
                }
                if (mGraphicOverlay == null) {
                    LogHelper.d(TAG, "resume: graphOverlay is null");
                    return;
                }
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                LogHelper.e(TAG, "Unable to start camera source.", e);
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
        LogHelper.d(TAG, "onResume");
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

    private void requestCameraPermission() {
        LogHelper.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_CAMERA_PERMISSION);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = view -> ActivityCompat.requestPermissions(thisActivity, permissions,
                RC_CAMERA_PERMISSION);

        findViewById(R.id.topLayout).setOnClickListener(listener);
        Snackbar.make(mGraphicOverlay, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != RC_CAMERA_PERMISSION) {
            LogHelper.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            LogHelper.d(TAG, "Camera permission granted - initialize the camera source");
            // we have permission, so create the camerasource
            createCameraSource(useFlash);
            return;
        }

        logPermError(grantResults);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Barcode Scanner")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, (dialog, id) -> finish())
                .show();
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
        for (Object mGraphic : mGraphicOverlay.getGraphics()) {
            if (!(mGraphic instanceof BarcodeGraphic)) continue;
            BarcodeGraphic graphic = (BarcodeGraphic) mGraphic;
            FirebaseVisionBarcode barcode = graphic.getBarcode();
            if (barcode == null || barcode.getBoundingBox() == null) continue; // Ignore if no barcode or bounding box
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
            BarcodeHolder.getInstance().setBarcode(best); // Save barcode
            setResult(CommonStatusCodes.SUCCESS);
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
}