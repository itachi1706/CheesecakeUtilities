/*
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.itachi1706.cheesecakeutilities.modules.cameraViewer

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.*
import android.hardware.camera2.*
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Size
import android.util.SparseIntArray
import android.view.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.itachi1706.cheesecakeutilities.R
import com.itachi1706.helperlib.helpers.LogHelper
import kotlinx.android.synthetic.main.fragment_camera2_basic.*
import java.util.*
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.math.max
import kotlin.math.round

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class Camera2BasicFragment : Fragment(), View.OnClickListener, ActivityCompat.OnRequestPermissionsResultCallback {

   private val surfaceTextureListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(texture: SurfaceTexture, width: Int, height: Int) { openCamera(width, height) }
        override fun onSurfaceTextureSizeChanged(texture: SurfaceTexture, width: Int, height: Int) { configureTransform(width, height) }
        override fun onSurfaceTextureDestroyed(texture: SurfaceTexture) = true
        override fun onSurfaceTextureUpdated(texture: SurfaceTexture) = Unit
    }

    private lateinit var cameraId: String
    private var captureSession: CameraCaptureSession? = null
    private var cameraDevice: CameraDevice? = null
    private lateinit var previewSize: Size
    private lateinit var largest: Size
    private val stateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(cameraDevice: CameraDevice) {
            cameraOpenCloseLock.release()
            this@Camera2BasicFragment.cameraDevice = cameraDevice
            createCameraPreviewSession()
        }

        override fun onDisconnected(cameraDevice: CameraDevice) {
            cameraOpenCloseLock.release()
            cameraDevice.close()
            this@Camera2BasicFragment.cameraDevice = null
        }

        override fun onError(cameraDevice: CameraDevice, error: Int) { onDisconnected(cameraDevice); this@Camera2BasicFragment.activity?.finish() }
    }

    private var backgroundThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null
    private lateinit var previewRequestBuilder: CaptureRequest.Builder
    private lateinit var previewRequest: CaptureRequest
    private var state = STATE_PREVIEW
    private val cameraOpenCloseLock = Semaphore(1)

    private var flashSupported = false
    private var sensorOrientation = 0

    private val captureCallback = object : CameraCaptureSession.CaptureCallback() {

        private fun process() {
            when (state) { STATE_PREVIEW -> Unit } // Do nothing when the camera preview is working normally.
        }
        override fun onCaptureProgressed(session: CameraCaptureSession, request: CaptureRequest, partialResult: CaptureResult) { process() }
        override fun onCaptureCompleted(session: CameraCaptureSession, request: CaptureRequest, result: TotalCaptureResult) { process() }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_camera2_basic, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        flash.setOnClickListener(this)
        info.setOnClickListener(this)
        switch_cam.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        startBackgroundThread()

        if (texture.isAvailable) openCamera(texture.width, texture.height)
        else texture.surfaceTextureListener = surfaceTextureListener
    }

    override fun onPause() {
        closeCamera()
        stopBackgroundThread()
        super.onPause()
    }

    private fun requestCameraPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) ConfirmationDialog().show(childFragmentManager, FRAGMENT_DIALOG)
        else requestPermissions(arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CAMERA_PERMISSION)
            if (grantResults.size != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) ErrorDialog.newInstance(getString(R.string.request_permission)).show(childFragmentManager, FRAGMENT_DIALOG)
        else super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun setUpCameraOutputs(width: Int, height: Int) {
        val manager = activity!!.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            for (cameraId in manager.cameraIdList) {
                if (!setupCam(manager, cameraId, width, height, true)) continue
                this.cameraId = cameraId
                return // We've found a viable camera and finished setting up member variables so we don't need to iterate through other available cameras.
            }
        } catch (e: CameraAccessException) {
            LogHelper.e(TAG, e.toString())
        } catch (e: NullPointerException) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
            ErrorDialog.newInstance(getString(R.string.camera_error)).show(childFragmentManager, FRAGMENT_DIALOG)
        }

    }

    private fun setupCam(manager: CameraManager, cameraId: String, width: Int, height: Int, init: Boolean = false): Boolean {
        val characteristics = manager.getCameraCharacteristics(cameraId)

        // We don't use a front facing camera in this sample.
        val cameraDirection = characteristics.get(CameraCharacteristics.LENS_FACING)
        if (cameraDirection != null &&
            cameraDirection == CameraCharacteristics.LENS_FACING_FRONT && init) {
            return false
        }

        val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP) ?: return false

        // For still image captures, we use the largest available size.
        largest = Collections.max(listOf(*map.getOutputSizes(ImageFormat.JPEG)), CompareSizesByArea())

        // Find out if we need to swap dimension to get the preview size relative to sensor coordinate.
        val displayRotation = activity!!.windowManager.defaultDisplay.rotation

        sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)!!
        val swappedDimensions = areDimensionsSwapped(displayRotation)

        val displaySize = Point()
        activity!!.windowManager.defaultDisplay.getSize(displaySize)
        val rotatedPreviewWidth = if (swappedDimensions) height else width
        val rotatedPreviewHeight = if (swappedDimensions) width else height
        var maxPreviewWidth = if (swappedDimensions) displaySize.y else displaySize.x
        var maxPreviewHeight = if (swappedDimensions) displaySize.x else displaySize.y

        if (maxPreviewWidth > MAX_PREVIEW_WIDTH) maxPreviewWidth = MAX_PREVIEW_WIDTH
        if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) maxPreviewHeight = MAX_PREVIEW_HEIGHT

        // Danger, W.R.! Attempting to use too large a preview size could exceed the camera
        // bus' bandwidth limitation, resulting in gorgeous previews but the storage of
        // garbage capture data.
        previewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture::class.java), rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth, maxPreviewHeight, largest)

        // We fit the aspect ratio of TextureView to the size of preview we picked.
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            texture.setAspectRatio(previewSize.width, previewSize.height)
        } else {
            texture.setAspectRatio(previewSize.height, previewSize.width)
        }

        // Check if the flash is supported.
        flashSupported = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        return true
    }

    private fun areDimensionsSwapped(displayRotation: Int): Boolean {
        var swappedDimensions = false
        when (displayRotation) {
            Surface.ROTATION_0, Surface.ROTATION_180 -> if (sensorOrientation == 90 || sensorOrientation == 270) swappedDimensions = true
            Surface.ROTATION_90, Surface.ROTATION_270 -> if (sensorOrientation == 0 || sensorOrientation == 180) swappedDimensions = true
            else -> LogHelper.e(TAG, "Display rotation is invalid: $displayRotation")
        }
        return swappedDimensions
    }

    private fun openCamera(width: Int, height: Int, cid: String? = null) {
        val permission = ContextCompat.checkSelfPermission(activity!!, Manifest.permission.CAMERA)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission()
            return
        }
        val manager = activity!!.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        if (cid.isNullOrEmpty()) setUpCameraOutputs(width, height) else {
            setupCam(manager, cid, width, height, false)
            this.cameraId = cid
        }
        flash.isEnabled = flashSupported // If camera has flash
        configureTransform(width, height)
        try {
            // Wait for camera to open - 2.5 seconds is sufficient
            if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) throw RuntimeException("Time out waiting to lock camera opening.")
            manager.openCamera(cameraId, stateCallback, backgroundHandler)
        } catch (e: CameraAccessException) {
            LogHelper.e(TAG, e.toString())
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera opening.", e)
        }

    }

    private fun closeCamera() {
        try {
            cameraOpenCloseLock.acquire()
            captureSession?.close()
            captureSession = null
            cameraDevice?.close()
            cameraDevice = null
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera closing.", e)
        } finally {
            cameraOpenCloseLock.release()
        }
    }

    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("CameraBackground").also { it.start() }
        backgroundHandler = Handler(backgroundThread!!.looper)
    }

    private fun stopBackgroundThread() {
        backgroundThread?.quitSafely()
        try {
            backgroundThread?.join()
            backgroundThread = null
            backgroundHandler = null
        } catch (e: InterruptedException) {
            LogHelper.e(TAG, e.toString())
        }

    }

    private fun createCameraPreviewSession() {
        try {
            val texture = texture.surfaceTexture
            texture?.setDefaultBufferSize(previewSize.width, previewSize.height) // We configure the size of default buffer to be the size of camera preview we want.
            val surface = Surface(texture) // This is the output Surface we need to start preview.

            // We set up a CaptureRequest.Builder with the output Surface.
            previewRequestBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            previewRequestBuilder.addTarget(surface)

            // Here, we create a CameraCaptureSession for camera preview.
            cameraDevice?.createCaptureSession(listOf(surface), object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                        if (cameraDevice == null) return // The camera is already closed
                        captureSession = cameraCaptureSession // When the session is ready, we start displaying the preview.
                        try {
                            // Auto focus should be continuous for camera preview.
                            previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                            setAutoFlash(previewRequestBuilder) // Flash is automatically enabled when necessary.

                            // Finally, we start displaying the camera preview.
                            previewRequest = previewRequestBuilder.build()
                            captureSession?.setRepeatingRequest(previewRequest, captureCallback, backgroundHandler)
                        } catch (e: CameraAccessException) {
                            LogHelper.e(TAG, e.toString())
                        }
                    }
                    override fun onConfigureFailed(session: CameraCaptureSession) { activity!!.runOnUiThread { Toast.makeText(activity, "Failed", Toast.LENGTH_SHORT).show() } } }, null)
        } catch (e: CameraAccessException) { LogHelper.e(TAG, e.toString()) }

    }

    private fun configureTransform(viewWidth: Int, viewHeight: Int) {
        activity ?: return
        val rotation = activity!!.windowManager.defaultDisplay.rotation
        val matrix = Matrix()
        val viewRect = RectF(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat())
        val bufferRect = RectF(0f, 0f, previewSize.height.toFloat(), previewSize.width.toFloat())
        val centerX = viewRect.centerX()
        val centerY = viewRect.centerY()

        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY())
            val scale = max(viewHeight.toFloat() / previewSize.height, viewWidth.toFloat() / previewSize.width)
            with(matrix) {
                setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL)
                postScale(scale, scale, centerX, centerY)
                postRotate((90 * (rotation - 2)).toFloat(), centerX, centerY)
            }
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180f, centerX, centerY)
        }
        texture.setTransform(matrix)
    }

    private fun toggleFlash() {
        if (activity == null) return
        LogHelper.d(TAG, "toggleFlash()")
        val flashState = previewRequestBuilder.get(CaptureRequest.FLASH_MODE) ?: CaptureRequest.FLASH_MODE_OFF
        LogHelper.d(TAG, "Flash State: $flashState")
        if (flashState != CaptureRequest.FLASH_MODE_OFF) {
            previewRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF)
            setAutoFlash(previewRequestBuilder)
        } // On
        else {
            previewRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH)
            previewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)
        } // Off
        previewRequest = previewRequestBuilder.build()
        captureSession?.setRepeatingRequest(previewRequest, captureCallback, backgroundHandler)
    }

    private fun getDebugInformation(): String {
        if (activity == null) return ""
        val manager = activity!!.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val string = StringBuilder()
        string.append("Camera Count: ${manager.cameraIdList.size}\n")
        string.append("Currently Active Camera: ${this.cameraId}\n")
        val c = manager.getCameraCharacteristics(cameraId)
        string.append("Is Front Facing: ${c.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT}\n")
        string.append("Has Flash: $flashSupported\n")
        string.append("Hardware Level Support: ${c.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)} (${getHardwareLevelName(c.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL))})\n")
        string.append("Optical Image Stabilization: ${isStateAvailable(CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION, c, CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_ON)}\n")
        string.append("Electronic Image Stabilization: ${isStateAvailable(CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES, c, CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_ON)}\n")
        val mp = String.format("%.1f", (largest.width * largest.height) / 1024.0 / 1024.0).toDouble()
        val mpRound = round((largest.width * largest.height) / 1024.0 / 1024.0)
        string.append("Resolution: ${largest.width}x${largest.height} (${if (mpRound <=0) mp else mpRound} Megapixels)\n")
        val aperature = c.get(CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES)
        val aperatureString = aperature?.joinToString(",", "f/", " ") ?: "None"
        string.append("Aperature Sizes: $aperatureString")
        return string.toString()
    }

    private fun isStateAvailable(char: CameraCharacteristics.Key<IntArray>, c: CameraCharacteristics, target: Int): Boolean {
        val tmp = c.get(char)
        tmp?.let { it.forEach { o -> if (o == target) return true } }
        return false
    }

    private fun getHardwareLevelName(level: Int?): String {
        return when (level) {
            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY -> "Legacy"
            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_EXTERNAL -> "External"
            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED -> "Limited"
            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL -> "Full"
            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3 -> "L3"
            else -> "Unknown"
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.flash -> toggleFlash()
            R.id.info -> activity?.let { AlertDialog.Builder(it).setTitle("Debug Info").setMessage(getDebugInformation()).setPositiveButton(android.R.string.ok, null).show() }
            R.id.switch_cam -> {
                val manager = activity!!.getSystemService(Context.CAMERA_SERVICE) as CameraManager
                val list = manager.cameraIdList.map { s -> val cameraDirection = manager.getCameraCharacteristics(s.toString()).get(CameraCharacteristics.LENS_FACING)
                    if (cameraDirection != null && cameraDirection == CameraCharacteristics.LENS_FACING_FRONT) "$s (Front Camera)" else "$s (Rear Camera)" }.toTypedArray()
                val selected = manager.cameraIdList.indexOf(cameraId)
                // Get front/rear status
                AlertDialog.Builder(context).setTitle("Change Camera")
                    .setSingleChoiceItems(list, selected, null)
                    .setPositiveButton(android.R.string.ok) { dialog: DialogInterface, _ ->
                        dialog.dismiss()
                        val selPos = (dialog as AlertDialog).listView.checkedItemPosition
                        val selString = manager.cameraIdList[selPos]
                        LogHelper.d(TAG, "Sel new camera: $selPos ($selString)")
                        switchCam(selString.toString())
                    }.setNegativeButton(android.R.string.cancel, null).show()
            }
        }
    }

    private fun switchCam(newCam: String) {
        LogHelper.i(TAG, "Switching Camera View from ${this.cameraId} to $newCam")
        closeCamera()
        openCamera(texture.width, texture.height, newCam)
    }

    private fun setAutoFlash(requestBuilder: CaptureRequest.Builder) {
        if (flashSupported) {
            requestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)
        }
    }

    companion object {
        private val ORIENTATIONS = SparseIntArray()
        private const val FRAGMENT_DIALOG = "dialog"

        init {
            ORIENTATIONS.append(Surface.ROTATION_0, 90)
            ORIENTATIONS.append(Surface.ROTATION_90, 0)
            ORIENTATIONS.append(Surface.ROTATION_180, 270)
            ORIENTATIONS.append(Surface.ROTATION_270, 180)
        }
        private const val TAG = "Camera2BasicFragment"

        private const val STATE_PREVIEW = 0

        private const val MAX_PREVIEW_WIDTH = 1920
        private const val MAX_PREVIEW_HEIGHT = 1080

        @JvmStatic private fun chooseOptimalSize(choices: Array<Size>, textureViewWidth: Int, textureViewHeight: Int, maxWidth: Int, maxHeight: Int, aspectRatio: Size): Size {
            // Collect the supported resolutions that are at least as big as the preview Surface
            val bigEnough = ArrayList<Size>()
            // Collect the supported resolutions that are smaller than the preview Surface
            val notBigEnough = ArrayList<Size>()
            val w = aspectRatio.width
            val h = aspectRatio.height
            for (option in choices) {
                if (option.width <= maxWidth && option.height <= maxHeight &&
                    option.height == option.width * h / w) {
                    if (option.width >= textureViewWidth && option.height >= textureViewHeight) bigEnough.add(option)
                    else notBigEnough.add(option)
                }
            }

            // Pick the smallest of those big enough. If there is no one big enough, pick the
            // largest of those not big enough.
            return when {
                bigEnough.size > 0 -> Collections.min(bigEnough, CompareSizesByArea())
                notBigEnough.size > 0 -> Collections.max(notBigEnough, CompareSizesByArea())
                else -> {
                    LogHelper.e(TAG, "Couldn't find any suitable preview size")
                    choices[0]
                }
            }
        }

        @JvmStatic fun newInstance(): Camera2BasicFragment = Camera2BasicFragment()
    }
}