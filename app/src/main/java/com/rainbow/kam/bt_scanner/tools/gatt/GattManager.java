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
import android.os.Handler;
import android.text.TextUtils;

import java.util.List;
import java.util.UUID;

import hugo.weaving.DebugLog;

/**
 * Created by kam6512 on 2015-10-29.
 */
public class GattManager {

    private static final long RSSI_UPDATE_TIME_INTERVAL = 3000;
    private static final UUID uuid = UUID.fromString(GattAttributes.CLIENT_CHARACTERISTIC_CONFIG);

    private final GattCustomCallbacks gattCustomCallbacks;

    private final Context context;

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattDescriptor notificationDescriptor;

    private boolean timerEnabled = false;

    private final Handler handler = new Handler();
    private final Runnable readRssiThread = new Runnable() {
        @Override
        public void run() {
            if (bluetoothGatt != null) {
                bluetoothGatt.readRemoteRssi();
                readRssiValue(timerEnabled);
            }
        }
    };


    public GattManager(Context context, GattCustomCallbacks gattCustomCallbacks) {
        this.context = context;
        this.gattCustomCallbacks = gattCustomCallbacks;

        if (bluetoothManager == null || bluetoothAdapter == null) {
            bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothAdapter = bluetoothManager.getAdapter();
        }
    }


    @DebugLog
    public void connect(final String deviceAddress) throws NullPointerException {
        if (TextUtils.isEmpty(deviceAddress)) {
            throw new NullPointerException("Address is not available");
        }
        if (bluetoothGatt != null && bluetoothGatt.getDevice().getAddress().equals(deviceAddress)) {
            bluetoothGatt.connect();
        } else {
            bluetoothDevice = bluetoothAdapter.getRemoteDevice(deviceAddress);
            if (bluetoothDevice == null) {
                throw new NullPointerException("RemoteDevice is not available");
            }
            bluetoothGatt = bluetoothDevice.connectGatt(context, true, bluetoothGattCallback);
        }
    }


    @DebugLog
    public void disconnect() {
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
        }
    }


    public boolean isBluetoothAvailable() {
        return bluetoothAdapter.isEnabled();
    }


    public boolean isConnected() {
        List<BluetoothDevice> bluetoothDevices = bluetoothManager.getConnectedDevices(BluetoothProfile.GATT);
        for (BluetoothDevice bluetoothDeviceItem : bluetoothDevices) {
            if (this.bluetoothDevice != null && this.bluetoothDevice.getAddress().equals(bluetoothDeviceItem.getAddress())) {
                return true;
            }
        }
        return false;
    }


    private void readRssiValue(final boolean repeat) {
        timerEnabled = repeat;
        if (!isConnected()) {
            timerEnabled = false;
            handler.removeCallbacks(readRssiThread);
            return;
        }
        handler.postDelayed(readRssiThread, RSSI_UPDATE_TIME_INTERVAL);
        bluetoothGatt.readRemoteRssi();
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


    public void setNotification(BluetoothGattCharacteristic notificationForCharacteristic, boolean enabled) {
        bluetoothGatt.setCharacteristicNotification(notificationForCharacteristic, enabled);

        notificationDescriptor = notificationForCharacteristic.getDescriptor(uuid);
        if (notificationDescriptor != null) {
            byte[] value = enabled ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
            notificationDescriptor.setValue(value);
            bluetoothGatt.writeDescriptor(notificationDescriptor);
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
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                startServiceDiscovery();
                startMonitoringRssiValue();

                gattCustomCallbacks.onDeviceConnected();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                stopMonitoringRssiValue();

                gattCustomCallbacks.onDeviceDisconnected();
                bluetoothGatt.close();
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
            gattCustomCallbacks.onDeviceNotify(characteristic);
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
            } else {
                gattCustomCallbacks.onRSSIMiss();
            }
        }


        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            if (descriptor.equals(notificationDescriptor)) {
                gattCustomCallbacks.onDeviceReady();
            }
        }
    };
}