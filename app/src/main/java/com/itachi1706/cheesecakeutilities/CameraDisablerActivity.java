package com.itachi1706.cheesecakeutilities;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class CameraDisablerActivity extends AppCompatActivity {

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

    }

    private void updateDeviceAdminStatus() {
        if (isDeviceAdminActive()) {
            devicePolicyManager.removeActiveAdmin(deviceAdmin);
            updateResources();
        } else {
            // Launch device admin request
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, deviceAdmin);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Explaination here");
            startActivityForResult(intent, DEVICE_ADMIN_REQUEST);
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
            deviceAdminStatus.setText("Enabled");
            deviceAdminStatus.setTextColor(Color.GREEN);
            deviceAdminBtn.setText("Disable Device Admin");
            cameraBtn.setEnabled(true);
        } else {
            deviceAdminStatus.setText("Disabled");
            deviceAdminStatus.setTextColor(Color.RED);
            deviceAdminBtn.setText("Enable Device Admin");
            cameraBtn.setEnabled(false);
        }

        if (ca) {
            cameraStatus.setText("Enabled");
            cameraStatus.setTextColor(Color.GREEN);
            cameraBtn.setText("Disable Camera");
        } else {
            cameraStatus.setText("Disabled");
            cameraStatus.setTextColor(Color.RED);
            cameraBtn.setText("Enable Camerea");
        }
    }

    public static class DeviceAdminReceiver extends android.app.admin.DeviceAdminReceiver {

        void showToast(Context context, String msg) {
            String status = context.getString(R.string.admin_receiver_status, msg);
            Toast.makeText(context, status, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onEnabled(Context context, Intent intent) {
            showToast(context, "Enabled");
            sendUpdateBroadcast(context);
        }

        @Override
        public CharSequence onDisableRequested(Context context, Intent intent) {
            return "You are about to disable Device Administrator";
        }

        @Override
        public void onDisabled(Context context, Intent intent) {
            showToast(context, "Disabled");
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
}
