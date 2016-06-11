package com.itachi1706.cheesecakeutilities;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.itachi1706.cheesecakeutilities.RecyclerAdapters.StringRecyclerAdapter;
import com.itachi1706.cheesecakeutilities.Util.CommonMethods;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BluetoothBatteryActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    BluetoothAdapter bluetoothAdapter;
    List<String> bluetoothlist = new ArrayList<>();
    StringRecyclerAdapter recyclerAdapter;

    private static final String TAG = "BluetoothAct";
    private static final int REQUEST_ENABLE_BLUETOOTH = 4;

    private static final UUID BATTERY_SERVICE_UUID = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb");
    private static final UUID BATTERY_LEVEL_UUID = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_battery);

        CommonMethods.betaInfo(this, "Bluetooth Battery Activity");

        recyclerView = (RecyclerView) findViewById(R.id.bt_recycler_view);
        if (recyclerView == null) {
            Log.e(TAG, "Cannot instantiate recyclerview");
            Toast.makeText(this, "Cannot launch utility", Toast.LENGTH_SHORT).show();
            finish();
        }
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Log.e(TAG, "Device API Level too low!");
            new AlertDialog.Builder(this).setTitle("Incompatible Device")
                    .setMessage("The Android Version on your device is too low to use this utility\n\n" +
                            "Requred API Level: " + Build.VERSION_CODES.JELLY_BEAN_MR2 + "\n" +
                            "Current API Level: " + Build.VERSION.SDK_INT + "\n\n" +
                            "This utility will now exit").setCancelable(false)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).show();
        } else {
            processScanning();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
            if (resultCode == RESULT_OK) {
                continueOn();
            } else if (resultCode == RESULT_CANCELED) {
                    new AlertDialog.Builder(this)
                            .setTitle("Bluetooth not enabled")
                            .setMessage("This utility will not work as bluetooth is disabled and will now exit.")
                            .setCancelable(false).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).show();
            }
        }
    }

    public void processScanning() {
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null) {
            new AlertDialog.Builder(this).setTitle("Unsupported")
                    .setMessage("Your device does not support Bluetooth. This utility will now exit")
                    .setCancelable(false).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            }).show();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH);
            return;
        }

        continueOn();
    }

    private void continueOn() {
        // Get paired devices
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        Set<BluetoothDevice> bleDevices = new HashSet<>();
        if (pairedDevices.size() <= 0) {
            Toast.makeText(this, "No devices found", Toast.LENGTH_SHORT).show();
            return;
        }
        for (BluetoothDevice device : pairedDevices) {
            String message = device.getName() + " | " +  device.getAddress();
            if (!supportLe(device)) {
                continue;
            }
            bluetoothlist.add(message);
            bleDevices.add(device);
        }
        recyclerAdapter = new StringRecyclerAdapter(bluetoothlist);
        recyclerView.setAdapter(recyclerAdapter);
        getGattServices(bleDevices);
    }

    private static final long SCAN_PERIOD = 10000;
    private boolean isScanning = false;
    private Handler handler;
    private BluetoothAdapter.LeScanCallback callback;

    private void scanDevice(BluetoothDevice device) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                isScanning = false;
                    bluetoothAdapter.stopLeScan(callback);
            }
        }, SCAN_PERIOD);

        isScanning = true;
        bluetoothAdapter.startLeScan(callback);
    }

    private void getGattServices(Set<BluetoothDevice> pairedDevices) {
        for (BluetoothDevice device : pairedDevices) {
            device.connectGatt(this, false, btgattcallback);
        }
    }

    private void readBattery(BluetoothGatt gattService) {

    }

    BluetoothGattCallback btgattcallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "Connected");
                Log.i(TAG, "Attempting service discovery " + gatt.discoverServices());
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "Disconnected");
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
        }
    };

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private boolean supportLe(BluetoothDevice device) {
        return device.getType() == BluetoothDevice.DEVICE_TYPE_DUAL || device.getType() == BluetoothDevice.DEVICE_TYPE_LE;
    }

}
