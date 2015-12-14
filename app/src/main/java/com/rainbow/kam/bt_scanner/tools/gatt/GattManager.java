package com.rainbow.kam.bt_scanner.tools.gatt;

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
public class GattManager {

    private final String TAG = getClass().getSimpleName();

    private static final int RSSI_UPDATE_TIME_INTERVAL = 2000;

    private GattCustomCallbacks gattCustomCallbacks = null;
    private static final GattCustomCallbacks NULL_CALLBACK = new GattCustomCallbacks.Null();

    private Activity activity;
    private boolean connected = false;

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;
    private BluetoothGatt bluetoothGatt;
    private List<BluetoothGattService> bluetoothGattServices;

    private Handler timerHandler = new Handler();
    private boolean timerEnabled = false;

    public GattManager(Activity activity, GattCustomCallbacks gattCustomCallbacks) {
        this.activity = activity;
        this.gattCustomCallbacks = gattCustomCallbacks;
        if (this.gattCustomCallbacks == null) {
            this.gattCustomCallbacks = NULL_CALLBACK;
        }
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

    public void connect(final String deviceAddress) throws Exception {
        if (bluetoothAdapter == null) {
            throw new Exception("Adapter is Null");
        } else if (deviceAddress == null) {
            throw new Exception("Address is not available");
        }
        if (bluetoothGatt != null && bluetoothGatt.getDevice().getAddress().equals(deviceAddress)) {
            bluetoothGatt.connect();
        } else {
            bluetoothDevice = bluetoothAdapter.getRemoteDevice(deviceAddress);
            if (bluetoothDevice == null) {
                throw new Exception("RemoteDevice is not available");
            }
            bluetoothGatt = bluetoothDevice.connectGatt(activity, false, bluetoothGattCallback);
        }
    }

    public void disconnect() {
        if (bluetoothGatt != null) {
            stopMonitoringRssiValue();
            bluetoothGatt.disconnect();
        }
    }

    public boolean isConnected() {
        return connected;
    }

    private void readRssiValue(final boolean repeat) {
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

    private void startMonitoringRssiValue() {
        readRssiValue(true);
    }

    private void stopMonitoringRssiValue() {
        readRssiValue(false);
    }

    private void startServiceDiscovery() {
        if (bluetoothGatt != null) bluetoothGatt.discoverServices();
    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public void setNotification(BluetoothGattCharacteristic notificationForCharacteristic, boolean enabled) {
        bluetoothGatt.setCharacteristicNotification(notificationForCharacteristic, enabled);

        BluetoothGattDescriptor bluetoothGattDescriptor = notificationForCharacteristic.getDescriptor(UUID.fromString(GattAttributes.Descriptor.CLIENT_CHARACTERISTIC_CONFIG));
        if (bluetoothGattDescriptor != null) {
            byte[] value = enabled ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
            bluetoothGattDescriptor.setValue(value);
           bluetoothGatt.writeDescriptor(bluetoothGattDescriptor);
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

    private void onValueFound(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        if (bluetoothAdapter == null || bluetoothGatt == null || bluetoothGattCharacteristic == null) {
            return;
        }
        byte[] rawValue = bluetoothGattCharacteristic.getValue();
        String strValue = null;
        int intValue = 0;

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

        gattCustomCallbacks.onNewDataFound(bluetoothGattCharacteristic, strValue, intValue, rawValue, timeStamp);
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

    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                connected = true;
                gattCustomCallbacks.onDeviceConnected();

                bluetoothGatt.readRemoteRssi();
                startServiceDiscovery();
                startMonitoringRssiValue();

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                connected = false;
                gattCustomCallbacks.onDeviceDisconnected();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (bluetoothGattServices != null && bluetoothGattServices.size() > 0) {
                    bluetoothGattServices.clear();
                }
                if (bluetoothGatt != null) {
                    bluetoothGattServices = bluetoothGatt.getServices();
                }

                gattCustomCallbacks.onServicesFound(bluetoothGattServices);
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
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            String deviceName = gatt.getDevice().getName();
            String serviceName = GattAttributes.resolveServiceName(characteristic.getService().getUuid().toString().toLowerCase(Locale.getDefault()));
            String characteristicName = GattAttributes.resolveCharacteristicName(characteristic.getUuid().toString().toLowerCase(Locale.getDefault()));
            String description = "Device: " + deviceName + " Service: " + serviceName + " Characteristic: " + characteristicName;

            if (status == BluetoothGatt.GATT_SUCCESS) {
                gattCustomCallbacks.onWriteSuccess(description + " STATUS = " + status);
            } else {
                gattCustomCallbacks.onWriteFail(description + " STATUS = " + status);
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                gattCustomCallbacks.onRssiUpdate(rssi);
            }
        }
    };
}