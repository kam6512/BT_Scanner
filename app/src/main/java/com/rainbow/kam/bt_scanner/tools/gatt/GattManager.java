package com.rainbow.kam.bt_scanner.tools.gatt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.rainbow.kam.bt_scanner.R;

import java.util.List;
import java.util.UUID;

/**
 * Created by kam6512 on 2015-10-29.
 */
public class GattManager {

    private final String TAG = getClass().getSimpleName();

//    private String deviceAddress;

    private static final int RSSI_UPDATE_TIME_INTERVAL = 5000;

    private GattCustomCallbacks gattCustomCallbacks;
//    private static final GattCustomCallbacks NULL_CALLBACK = new GattCustomCallbacks.Null();

    private final Context context;

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;
    private BluetoothGatt bluetoothGatt;

    private final Handler timerHandler = new Handler();
    private boolean timerEnabled = false;

    public GattManager(Context context, GattCustomCallbacks gattCustomCallbacks) {
        this.context = context;
        this.gattCustomCallbacks = gattCustomCallbacks;
//        if (this.gattCustomCallbacks == null) {
//            this.gattCustomCallbacks = NULL_CALLBACK;
//        }
        if (bluetoothManager == null || bluetoothAdapter == null) {
            bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothAdapter = bluetoothManager.getAdapter();
            if (bluetoothManager == null) {
                Toast.makeText(context, R.string.bt_fail, Toast.LENGTH_LONG).show();
            }
        }
    }


    public void connect(final String deviceAddress) throws Exception {
        if (TextUtils.isEmpty(deviceAddress)) {
            throw new Exception("Address is not available");
        }
        if (bluetoothGatt != null && bluetoothGatt.getDevice().getAddress().equals(deviceAddress)) {
            bluetoothGatt.connect();
        } else {
            bluetoothDevice = bluetoothAdapter.getRemoteDevice(deviceAddress);
            if (bluetoothDevice == null) {
                throw new Exception("RemoteDevice is not available");
            }
            bluetoothGatt = bluetoothDevice.connectGatt(context, true, bluetoothGattCallback);
        }
//        if ( bluetoothDevice.createBond()){
//            Log.e(TAG, "Success");
//        }else {
//            Log.e(TAG,"Fail");
//        }
    }


    public void disconnect() {

        bluetoothGatt.disconnect();
    }


    public boolean isBluetoothAvailable() {
        return bluetoothAdapter.isEnabled();
    }


    public boolean isConnected() {
        List<BluetoothDevice> bluetoothDevicesTemp = bluetoothManager.getConnectedDevices(BluetoothProfile.GATT);
        for (BluetoothDevice bluetoothDeviceTemp : bluetoothDevicesTemp) {
            if (bluetoothDevice.getAddress().equals(bluetoothDeviceTemp.getAddress())) {
                return true;
            }
        }
        return false;
    }


    private void readRssiValue(final boolean repeat) {
        timerEnabled = repeat;
        if (!isConnected()) {
            timerEnabled = false;
            return;
        }
        timerHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                bluetoothGatt.readRemoteRssi();
                readRssiValue(timerEnabled);
            }
        }, RSSI_UPDATE_TIME_INTERVAL);
    }


    private void startMonitoringRssiValue() {
        readRssiValue(true);
    }


    private void stopMonitoringRssiValue() {
        readRssiValue(false);
    }


    private void startServiceDiscovery() {
        boolean status = bluetoothGatt.discoverServices();
        if (!status) {
            gattCustomCallbacks.onServicesNotFound();
        }
    }


    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }


    public void setNotification(BluetoothGattCharacteristic notificationForCharacteristic, boolean enabled) {
        bluetoothGatt.setCharacteristicNotification(notificationForCharacteristic, enabled);

        // notification 을 enable 한뒤에 Descriptor 를 write 해주어야 응답함
        BluetoothGattDescriptor bluetoothGattDescriptor = notificationForCharacteristic.getDescriptor(UUID.fromString(GattAttributes.Descriptor.CLIENT_CHARACTERISTIC_CONFIG));
        if (bluetoothGattDescriptor != null) {
            byte[] value = enabled ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
            bluetoothGattDescriptor.setValue(value);
            bluetoothGatt.writeDescriptor(bluetoothGattDescriptor);
        }

    }


    public void readValue(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        boolean status = bluetoothGatt.readCharacteristic(bluetoothGattCharacteristic);
        if (!status) {
            gattCustomCallbacks.onReadFail();
        }
    }


    public void writeValue(final BluetoothGattCharacteristic bluetoothGattCharacteristic, final byte[] dataToWrite) {
        if (dataToWrite.length != 0) {
            bluetoothGattCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            bluetoothGattCharacteristic.setValue(dataToWrite);

            boolean status = bluetoothGatt.writeCharacteristic(bluetoothGattCharacteristic);
            if (!status) {
                gattCustomCallbacks.onWriteFail();
            }
        } else {
            gattCustomCallbacks.onWriteFail();
        }

    }


    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.e(TAG, "status = " + status + "   newState = " + newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {

                startServiceDiscovery();
                startMonitoringRssiValue();

                gattCustomCallbacks.onDeviceConnected();

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {

                stopMonitoringRssiValue();

                gattCustomCallbacks.onDeviceDisconnected();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                gattCustomCallbacks.onServicesFound(bluetoothGatt.getServices());
            } else {
                gattCustomCallbacks.onServicesNotFound();
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                gattCustomCallbacks.onReadSuccess(characteristic);
            } else {
                gattCustomCallbacks.onReadFail();
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            gattCustomCallbacks.onDataNotify(characteristic);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

            if (status == BluetoothGatt.GATT_SUCCESS) {
                gattCustomCallbacks.onWriteSuccess();
            } else {
                gattCustomCallbacks.onWriteFail();
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                gattCustomCallbacks.onRSSIUpdate(rssi);
                Log.e(TAG, "RSSI : " + rssi);
            } else {
                gattCustomCallbacks.onRSSIMiss();
            }
        }
    };
}