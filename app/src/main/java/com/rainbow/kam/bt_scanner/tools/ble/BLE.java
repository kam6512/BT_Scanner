package com.rainbow.kam.bt_scanner.tools.ble;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.IllegalFormatCodePointException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Created by kam6512 on 2015-10-29.
 */
public class BLE {

    private final String TAG = getClass().getSimpleName();

    private static final int RSSI_UPDATE_TIME_INTERVAL = 2000;

    private BleUiCallbacks bleUiCallbacks = null;
    private static final BleUiCallbacks NULL_CALLBACK = new BleUiCallbacks.Null();

    private Activity activity;
    private boolean connected = false;

    private BluetoothManager bluetoothManager = null;
    private BluetoothAdapter bluetoothAdapter = null;
    private BluetoothDevice bluetoothDevice = null;
    private BluetoothGatt bluetoothGatt = null;
    private BluetoothGattService bluetoothGattService = null;
    private List<BluetoothGattService> bluetoothGattServices = null;

    private Handler timerHandler = new Handler();
    private boolean timerEnabled = false;

    public BLE(Activity activity, BleUiCallbacks bleUiCallbacks) {
        this.activity = activity;
        this.bleUiCallbacks = bleUiCallbacks;
        if (this.bleUiCallbacks == null) {
            this.bleUiCallbacks = NULL_CALLBACK;
        }
    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public BluetoothGatt getBluetoothGatt() {
        return bluetoothGatt;
    }

    public BluetoothGattService getBluetoothGattService() {
        return bluetoothGattService;
    }

    public List<BluetoothGattService> getBluetoothGattServices() {
        return bluetoothGattServices;
    }

    public boolean initialize() {
        if (bluetoothManager == null) {
            bluetoothManager = (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager == null) {
                return false;
            }
        }
        if (bluetoothAdapter == null) {
            bluetoothAdapter = bluetoothManager.getAdapter();
        }
        return bluetoothAdapter != null;
    }

    public boolean connect(final String deviceAddress) {
        if (bluetoothAdapter == null || deviceAddress == null) {
            return false;
        }
        if (bluetoothGatt != null && bluetoothGatt.getDevice().getAddress().equals(deviceAddress)) {
            return bluetoothGatt.connect();
        } else {
            bluetoothDevice = bluetoothAdapter.getRemoteDevice(deviceAddress);
            if (bluetoothDevice == null) {
                return false;
            }
            bluetoothGatt = bluetoothDevice.connectGatt(activity, false, bluetoothGattCallback);
        }
        return true;
    }

    public void disconnect() {
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
        }
        bleUiCallbacks.onDeviceDisconnected(bluetoothGatt, bluetoothDevice);
    }

    public boolean isConnected() {
        return connected;
    }

    public void close() {
        if (bluetoothGatt != null) {
            bluetoothGatt.close();
        }
        bluetoothGatt = null;
    }

    public void readRssiValue(final boolean repeat) {
        timerEnabled = repeat;

        if (!connected || bluetoothGatt == null || !timerEnabled) {
            timerEnabled = false;
            return;
        }
        timerHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (bluetoothGatt == null || bluetoothAdapter == null || !connected) {
                    timerEnabled = false;
                    return;
                }

                bluetoothGatt.readRemoteRssi();
                readRssiValue(timerEnabled);
            }
        }, RSSI_UPDATE_TIME_INTERVAL);
    }

    public void startMonitoringRssiValue() {
        readRssiValue(true);
    }

    public void stopMonitoringRssiValue() {
        readRssiValue(false);
    }

    public void startServiceDiscovery() {
        if (bluetoothGatt != null) bluetoothGatt.discoverServices();
    }

    public void getServices() {
        if (bluetoothGattServices != null && bluetoothGattServices.size() > 0) {
            bluetoothGattServices.clear();
        }
        if (bluetoothGatt != null) {
            bluetoothGattServices = bluetoothGatt.getServices();
        }

        bleUiCallbacks.onServicesFound(bluetoothGatt, bluetoothDevice, bluetoothGattServices);
    }

    public void getCharacteristics(final BluetoothGattService bluetoothGattService) {
        if (bluetoothGattService == null) {
            return;
        }
        List<BluetoothGattCharacteristic> bluetoothGattCharacteristics;

        bluetoothGattCharacteristics = bluetoothGattService.getCharacteristics();
        bleUiCallbacks.onCharacteristicFound(bluetoothGatt, bluetoothDevice, bluetoothGattService, bluetoothGattCharacteristics);
        this.bluetoothGattService = bluetoothGattService;
    }

    public void setNotification(BluetoothGattCharacteristic notificationForCharacteristic, boolean enabled) {
        if (bluetoothAdapter == null || bluetoothGatt == null) {
            Log.e(TAG, "is null");
            return;
        }
        boolean success = bluetoothGatt.setCharacteristicNotification(notificationForCharacteristic, enabled);

        if (!success) {
            Log.e(TAG, "Setting proper notification status for characteristic failed!");
        }

        BluetoothGattDescriptor bluetoothGattDescriptor = notificationForCharacteristic.getDescriptor(UUID.fromString(BLEGattAttributes.Descriptor.CLIENT_CHARACTERISTIC_CONFIG));
        if (bluetoothGattDescriptor != null) {
            Log.e(TAG, "start Notify");
            byte[] value = enabled ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
            bluetoothGattDescriptor.setValue(value);
            boolean enable = bluetoothGatt.writeDescriptor(bluetoothGattDescriptor);
            Log.e(TAG, value[0] + value[1] + "writeDescriptor : " + enable);
        }
    }

    public void readValue(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        if (bluetoothAdapter == null || bluetoothGatt == null) {
            return;
        }
        bluetoothGatt.readCharacteristic(bluetoothGattCharacteristic);
    }

    public void writeValue(final BluetoothGattCharacteristic bluetoothGattCharacteristic, final byte[] dataToWrite) {
        if (bluetoothAdapter == null || bluetoothGatt == null || bluetoothGattCharacteristic == null) {
            Log.e(TAG, "writeValue null");
        } else {
            Log.e(TAG, "writeValue start");
            bluetoothGattCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            bluetoothGattCharacteristic.setValue(dataToWrite);
            bluetoothGatt.writeCharacteristic(bluetoothGattCharacteristic);
        }
    }

    public void onValueFound(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        if (bluetoothAdapter == null || bluetoothGatt == null || bluetoothGattCharacteristic == null) {
            return;
        }
        byte[] rawValue = bluetoothGattCharacteristic.getValue();
        String strValue = null;
        int intValue = 0;
/*
        UUID uuid = bluetoothGattCharacteristic.getUuid();

        if (uuid.equals(UUID.fromString(BLEGattAttributes.Characteristic.HEART_RATE_MEASUREMENT))) {

            int index = ((rawValue[0] & 0x01) == 1) ? 2 : 1;

            int format = (index == 1) ? BluetoothGattCharacteristic.FORMAT_UINT8 : BluetoothGattCharacteristic.FORMAT_UINT16;

            intValue = bluetoothGattCharacteristic.getIntValue(format, index);
            strValue = intValue + "bpm";
        } else if (uuid.equals(UUID.fromString(BLEGattAttributes.Characteristic.HEART_RATE_MEASUREMENT)) ||
                uuid.equals(UUID.fromString(BLEGattAttributes.Characteristic.MODEL)) ||
                uuid.equals(UUID.fromString(BLEGattAttributes.Characteristic.FIRMWARE))) {
            strValue = bluetoothGattCharacteristic.getStringValue(0);

        } else if (uuid.equals(UUID.fromString(BLEGattAttributes.Characteristic.APPEARANCE))) {

            intValue = rawValue[0];
            strValue = BLEGattAttributes.resolveAppearance(intValue);
        } else if (uuid.equals(UUID.fromString(BLEGattAttributes.Characteristic.BATTRY))) {
            intValue = rawValue[0];
            strValue = "" + intValue + "% battery level";
        } else {
            if (rawValue.length > 0) {
                intValue = (int) rawValue[0];
            }
            if (rawValue.length > 1) {
                intValue = intValue + ((int) rawValue[1] << 8);
            }
            if (rawValue.length > 2) {
                intValue = intValue + ((int) rawValue[2] << 8);
            }
            if (rawValue.length > 3) {
                intValue = intValue + ((int) rawValue[3] << 8);
            }
        }
        */
        if (rawValue.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(rawValue.length);
            for (byte byteChar : rawValue) {
                try {
                    stringBuilder.append(String.format("%c", byteChar));
                } catch (IllegalFormatCodePointException e) {
                    stringBuilder.append((char) byteChar);
                }
            }
            strValue = stringBuilder.toString();
        }

        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyy.MM.dd HH.mm.ss.SSS").format(new Date());
        bleUiCallbacks.onNewDataFound(bluetoothGatt, bluetoothDevice, bluetoothGattService, bluetoothGattCharacteristic, strValue, intValue, rawValue, timeStamp);
    }

    public int getValueFormat(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        int properties = bluetoothGattCharacteristic.getProperties();
        if ((BluetoothGattCharacteristic.FORMAT_FLOAT & properties) != 0) {
            return BluetoothGattCharacteristic.FORMAT_FLOAT;
        }
        if ((BluetoothGattCharacteristic.FORMAT_SFLOAT & properties) != 0) {
            return BluetoothGattCharacteristic.FORMAT_SFLOAT;
        }
        if ((BluetoothGattCharacteristic.FORMAT_SINT16 & properties) != 0) {
            return BluetoothGattCharacteristic.FORMAT_SINT16;
        }
        if ((BluetoothGattCharacteristic.FORMAT_SINT32 & properties) != 0) {
            return BluetoothGattCharacteristic.FORMAT_SINT32;
        }
        if ((BluetoothGattCharacteristic.FORMAT_SINT8 & properties) != 0) {
            return BluetoothGattCharacteristic.FORMAT_SINT8;
        }
        if ((BluetoothGattCharacteristic.FORMAT_UINT16 & properties) != 0) {
            return BluetoothGattCharacteristic.FORMAT_UINT16;
        }
        if ((BluetoothGattCharacteristic.FORMAT_UINT32 & properties) != 0) {
            return BluetoothGattCharacteristic.FORMAT_UINT32;
        }
        if ((BluetoothGattCharacteristic.FORMAT_UINT8 & properties) != 0) {
            return BluetoothGattCharacteristic.FORMAT_UINT8;
        }
        return 0;
    }
    /*
        private BluetoothAdapter.LeScanCallback deviceFoundCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                bleUiCallbacks.onDeviceFound(device, rssi, scanRecord);
            }
        };
    */

    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                connected = true;
                bleUiCallbacks.onDeviceConnected(bluetoothGatt, bluetoothDevice);

                bluetoothGatt.readRemoteRssi();
                startServiceDiscovery();
                startMonitoringRssiValue();

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                connected = false;
                bleUiCallbacks.onDeviceDisconnected(bluetoothGatt, bluetoothDevice);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                getServices();
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                onValueFound(characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            onValueFound(characteristic);
            bleUiCallbacks.onDataNotify(bluetoothGatt, bluetoothDevice, bluetoothGattService, characteristic);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            String deviceName = gatt.getDevice().getName();
            String serviceName = BLEGattAttributes.resolveServiceName(characteristic.getService().getUuid().toString().toLowerCase(Locale.getDefault()));
            String characteristicName = BLEGattAttributes.resolveCharacteristicName(characteristic.getUuid().toString().toLowerCase(Locale.getDefault()));
            String description = "Device: " + deviceName + " Service: " + serviceName + " Characteristic: " + characteristicName;

            if (status == BluetoothGatt.GATT_SUCCESS) {
                bleUiCallbacks.onWriteSuccess(bluetoothGatt, bluetoothDevice, bluetoothGattService, characteristic, description + " STATUS = " + status);
            } else {
                bleUiCallbacks.onWriteFail(bluetoothGatt, bluetoothDevice, bluetoothGattService, characteristic, description + " STATUS = " + status);
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                bleUiCallbacks.onRssiUpdate(bluetoothGatt, bluetoothDevice, rssi);
            }
        }
    };

}
