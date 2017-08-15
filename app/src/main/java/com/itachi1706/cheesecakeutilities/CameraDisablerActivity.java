package com.itachi1706.cheesecakeutilities;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class CameraDisablerActivity extends BaseActivity {

    public static final String DEVICE_ADMIN_BROADCAST = "com.itachi1706.cheesecakeutilities.DEVICE_ADMIN_BROADCAST";
    private static final int DEVICE_ADMIN_REQUEST = 4;
    ResponseReceiver receiver;

    TextView deviceAdminStatus, cameraStatus;
    Button deviceAdminBtn, cameraBtn;

    DevicePolicyManager devicePolicyManager;
    ComponentName deviceAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_disabler);

        deviceAdminBtn = (Button) findViewById(R.id.btnDeviceAdmin);
        cameraBtn = (Button) findViewById(R.id.btnCameraAdmin);
        deviceAdminStatus = (TextView) findViewById(R.id.statusDeviceAdmin);
        cameraStatus = (TextView) findViewById(R.id.statusCamera);

        devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        deviceAdmin = new ComponentName(this, DeviceAdminReceiver.class);

        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateCameraStatus();
            }
        });

        deviceAdminBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateDeviceAdminStatus();
            }
        });

        receiver = new ResponseReceiver();
        IntentFilter filter = new IntentFilter(DEVICE_ADMIN_BROADCAST);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
        updateResources();
    }

    private void updateCameraStatus() {
        if (isCameraActive()) {
            // Disable
            devicePolicyManager.setCameraDisabled(deviceAdmin, true);
            Toast.makeText(this, getString(R.string.camera_disable_camera_status_notification, getString(R.string.camera_disable_disabled)), Toast.LENGTH_SHORT).show();
        } else {
            devicePolicyManager.setCameraDisabled(deviceAdmin, false);
            Toast.makeText(this, getString(R.string.camera_disable_camera_status_notification, getString(R.string.camera_disable_enabled)), Toast.LENGTH_SHORT).show();
        }
        updateResources();
    }

    private void updateDeviceAdminStatus() {
        if (isDeviceAdminActive()) {
            devicePolicyManager.removeActiveAdmin(deviceAdmin);
            updateResources();
            Toast.makeText(this, R.string.camera_disable_disabled_toast, Toast.LENGTH_SHORT).show();
        } else {
            // As per Google Play requirements, a disclosure of why it is needed
            new AlertDialog.Builder(this).setTitle(R.string.camera_disable_disclosure_title).setMessage(R.string.camera_disable_explaination_disclosure)
                    .setNeutralButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Cancel and exit utility
                            finish();
                        }
                    }).setPositiveButton(R.string.camera_disable_disclosure_grant, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Launch device admin request
                    Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                    intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, deviceAdmin);
                    intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, getString(R.string.camera_disable_explaination));
                    startActivityForResult(intent, DEVICE_ADMIN_REQUEST);
                }
            }).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DEVICE_ADMIN_REQUEST && resultCode == RESULT_OK) {
            new AlertDialog.Builder(this).setTitle(R.string.camera_disable_enabled_toast)
                    .setMessage(R.string.camera_disable_enabled_message).setPositiveButton(R.string.dialog_action_positive_close, null).show();
        }
    }

    private boolean isDeviceAdminActive() {
        return devicePolicyManager.isAdminActive(deviceAdmin);
    }

    private boolean isCameraActive() {
        return !devicePolicyManager.getCameraDisabled(deviceAdmin);
    }

    private void updateResources() {
        boolean da = isDeviceAdminActive();
        boolean ca = isCameraActive();

        if (da) {
            deviceAdminStatus.setText(R.string.camera_disable_enabled);
            deviceAdminStatus.setTextColor(Color.GREEN);
            deviceAdminBtn.setText(getString(R.string.camera_disable_device_admin_status, getString(R.string.camera_disable_disable)));
            cameraBtn.setEnabled(true);
        } else {
            deviceAdminStatus.setText(R.string.camera_disable_disabled);
            deviceAdminStatus.setTextColor(Color.RED);
            deviceAdminBtn.setText(getString(R.string.camera_disable_device_admin_status, getString(R.string.camera_disable_enable)));
            cameraBtn.setEnabled(false);
        }

        if (ca) {
            cameraStatus.setText(R.string.camera_disable_enabled);
            cameraStatus.setTextColor(Color.GREEN);
            cameraBtn.setText(getString(R.string.camera_disable_camera_status, getString(R.string.camera_disable_disable)));
        } else {
            cameraStatus.setText(R.string.camera_disable_disabled);
            cameraStatus.setTextColor(Color.RED);
            cameraBtn.setText(getString(R.string.camera_disable_camera_status, getString(R.string.camera_disable_enable)));
        }
    }

    public static class DeviceAdminReceiver extends android.app.admin.DeviceAdminReceiver {

        void showToast(Context context, String msg) {
            String status = context.getString(R.string.camera_disable_admin_receiver_status, msg);
            Toast.makeText(context, status, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onEnabled(Context context, Intent intent) {
            showToast(context, context.getString(R.string.camera_disable_enabled));
            sendUpdateBroadcast(context);
        }

        @Override
        public CharSequence onDisableRequested(Context context, Intent intent) {
            return context.getString(R.string.camera_disable_device_admin_disable_warning);
        }

        @Override
        public void onDisabled(Context context, Intent intent) {
            showToast(context, context.getString(R.string.camera_disable_disabled));
            sendUpdateBroadcast(context);
        }

        private void sendUpdateBroadcast(Context context) {
            Intent completeIntent = new Intent(CameraDisablerActivity.DEVICE_ADMIN_BROADCAST);
            LocalBroadcastManager.getInstance(context).sendBroadcast(completeIntent);
        }
    }

    private class ResponseReceiver extends BroadcastReceiver {
        private ResponseReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            Log.i("CameraDisabler", "Received Update Broadcast");
            updateResources();
        }
    }

    @Override
    public String getHelpDescription() {
        return "Allows you to control whether or not to enable/disable the camera on the device.\n" +
                "Note: Device Admin Permissions are required";
    }
}
