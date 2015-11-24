package com.rainbow.kam.bt_scanner.Tools.BLE;

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
import android.util.Xml;

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
    private String deviceAddress = "";

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

    public BluetoothManager getBluetoothManager() {
        return bluetoothManager;
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapter;
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

    public boolean isConnected() {
        return connected;
    }

    public boolean checkBleHardwareAvailable() {
        final BluetoothManager bluetoothManager = (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager == null) {
            return false;
        }

        final BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            return false;
        }
        return bluetoothAdapter.isEnabled();
    }

    public void startScanning() {
        bluetoothAdapter.startLeScan(deviceFoundCallback);
    }

    public void stopScanning() {
        bluetoothAdapter.stopLeScan(deviceFoundCallback);
    }

    public boolean initialize() {
        if (bluetoothManager == null) {
            bluetoothManager = (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager == null) {
                return false;
            }
        }

        if (bluetoothAdapter == null) bluetoothAdapter = bluetoothManager.getAdapter();
        return bluetoothAdapter != null;
    }

    public boolean connect(final String deviceAddress) {
        if (bluetoothAdapter == null || deviceAddress == null) {
            return false;
        }
        this.deviceAddress = deviceAddress;
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
        bleUiCallbacks.uiDeviceDisconnected(bluetoothGatt, bluetoothDevice);
    }

    public void close() {
        if (bluetoothGatt != null) {
            bluetoothGatt.close();
        }
        bluetoothGatt = null;
    }

    public void readPeriodicalyRssiValue(final boolean repeat) {
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
                readPeriodicalyRssiValue(timerEnabled);
            }
        }, RSSI_UPDATE_TIME_INTERVAL);
    }

    public void startMonitoringRssiValue() {
        readPeriodicalyRssiValue(true);
    }

    public void stopMonitoringRssiValue() {
        readPeriodicalyRssiValue(false);
    }

    public void startServiceDiscorvery() {
        if (bluetoothGatt != null) bluetoothGatt.discoverServices();
    }

    public void getSupportedServices() {
        if (bluetoothGattServices != null && bluetoothGattServices.size() > 0) {
            bluetoothGattServices.clear();
        }
        if (bluetoothGatt != null) {
            bluetoothGattServices = bluetoothGatt.getServices();
        }

        bleUiCallbacks.uiAvailableServices(bluetoothGatt, bluetoothDevice, bluetoothGattServices);
    }

    public void getCharacteristicsForService(final BluetoothGattService bluetoothGattService) {
        if (bluetoothGattService == null) {
            return;
        }
        List<BluetoothGattCharacteristic> bluetoothGattCharacteristics;

        bluetoothGattCharacteristics = bluetoothGattService.getCharacteristics();
        bleUiCallbacks.uiCharacteristicForService(bluetoothGatt, bluetoothDevice, bluetoothGattService, bluetoothGattCharacteristics);
        this.bluetoothGattService = bluetoothGattService;
    }

    public void requestCharacteristicValue(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        if (bluetoothAdapter == null || bluetoothGatt == null) {
            return;
        }

        bluetoothGatt.readCharacteristic(bluetoothGattCharacteristic);
    }

    public void getCharacteristisValue(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        if (bluetoothAdapter == null || bluetoothGatt == null || bluetoothGattCharacteristic == null) {
            return;
        }
        byte[] rawValue = bluetoothGattCharacteristic.getValue();
        String strValue = null;
        int intValue = 0;

        UUID uuid = bluetoothGattCharacteristic.getUuid();


        if (uuid.equals(UUID.fromString(BLEGattAttributes.Characteristic.HEART_RATE_MEASUREMENT))) {

            //index 1 (and uint8) or index 2 (and uint16)
            int index = ((rawValue[0] & 0x01) == 1) ? 2 : 1;

            int format = (index == 1) ? BluetoothGattCharacteristic.FORMAT_UINT8 : BluetoothGattCharacteristic.FORMAT_UINT16;

            intValue = bluetoothGattCharacteristic.getIntValue(format, index);
            strValue = intValue + "bpm";
        } else if (uuid.equals(UUID.fromString(BLEGattAttributes.Characteristic.HEART_RATE_MEASUREMENT)) ||
                uuid.equals(UUID.fromString(BLEGattAttributes.Characteristic.MODEL)) ||
                uuid.equals(UUID.fromString(BLEGattAttributes.Characteristic.FIRMWARE))) {
            strValue = bluetoothGattCharacteristic.getStringValue(0);

        } else if (uuid.equals(UUID.fromString(BLEGattAttributes.Characteristic.APPEARANCE))) {
            intValue = ((int) rawValue[1]) << 8;
            intValue = rawValue[0];
            strValue = BLEGattAttributes.resolveAppearance(intValue);
        } else if (uuid.equals(UUID.fromString(BLEGattAttributes.Characteristic.BATTRY))) {
            intValue = rawValue[0];
            strValue = "" + intValue + "% battery level";
        } else {
            int Value = 0;
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

            if (rawValue.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(rawValue.length);

                for (byte byteChar : rawValue) {
//                    Log.e("rawValue",String.format("%c", byteChar));
                    try {
                        stringBuilder.append(String.format("%c", byteChar));
                    } catch (IllegalFormatCodePointException e) {

                        stringBuilder.append((char) byteChar);
                    }

                }

                strValue = stringBuilder.toString();
            }
        }
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd HH.mm.ss.SSS").format(new Date());
        bleUiCallbacks.uiNewValueForCharacteristic(bluetoothGatt, bluetoothDevice, bluetoothGattService, bluetoothGattCharacteristic, strValue, intValue, rawValue, timeStamp);
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

    public void writeDataToCharacteristic(final BluetoothGattCharacteristic bluetoothGattCharacteristic, final byte[] dataToWrite) {
        if (bluetoothAdapter == null || bluetoothGatt == null || bluetoothGattCharacteristic == null) {
            Log.e(TAG, "writeDataToCharacteristic null");
            return;
        } else {
            Log.e(TAG, "writeDataToCharacteristic start");
            bluetoothGattCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            bluetoothGattCharacteristic.setValue(dataToWrite);
            bluetoothGatt.writeCharacteristic(bluetoothGattCharacteristic);
        }
    }

    public void setNotificationForCharacteristic(BluetoothGattCharacteristic notificationForCharacteristic, boolean enabled) {
        if (bluetoothAdapter == null || bluetoothGatt == null) {
            Log.e(TAG, "is null");
            return;
        }
        boolean success = bluetoothGatt.setCharacteristicNotification(notificationForCharacteristic, enabled);

        if (!success) {
            Log.e(TAG, "Seting proper notification status for characteristic failed!");
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

    private BluetoothAdapter.LeScanCallback deviceFoundCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            bleUiCallbacks.uiDeviceFound(device, rssi, scanRecord);
        }
    };

    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                connected = true;
                bleUiCallbacks.uiDeviceConnected(bluetoothGatt, bluetoothDevice);

                bluetoothGatt.readRemoteRssi();
                startServiceDiscorvery();
                startMonitoringRssiValue();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                connected = false;
                bleUiCallbacks.uiDeviceDisconnected(bluetoothGatt, bluetoothDevice);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                getSupportedServices();
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                getCharacteristisValue(characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            getCharacteristisValue(characteristic);
            bleUiCallbacks.uiGotNotification(bluetoothGatt, bluetoothDevice, bluetoothGattService, characteristic);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            String deviceName = gatt.getDevice().getName();
            String serviceName = BLEGattAttributes.resolveServiceName(characteristic.getService().getUuid().toString().toLowerCase(Locale.getDefault()));
            String characteristicName = BLEGattAttributes.resolveCharacteristicName(characteristic.getUuid().toString().toLowerCase(Locale.getDefault()));
            String description = "Device: " + deviceName + " Service: " + serviceName + " Characteristic: " + characteristicName;

            if (status == BluetoothGatt.GATT_SUCCESS) {
                bleUiCallbacks.uiSuccessfulWrite(bluetoothGatt, bluetoothDevice, bluetoothGattService, characteristic, description + " STATUS= " + status);
            } else {
                bleUiCallbacks.uiFailedWrite(bluetoothGatt, bluetoothDevice, bluetoothGattService, characteristic, description + " STATUS= " + status);
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                bleUiCallbacks.uiNewRssiAvailable(bluetoothGatt, bluetoothDevice, rssi);
            }
        }
    };

}
