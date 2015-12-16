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
import android.widget.Toast;

import com.rainbow.kam.bt_scanner.R;

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

    private final Activity activity;
    private boolean connected;

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;
    private BluetoothGatt bluetoothGatt;
    private List<BluetoothGattService> bluetoothGattServices;

    private final Handler timerHandler = new Handler();
    private boolean timerEnabled = false;

    public GattManager(Activity activity, GattCustomCallbacks gattCustomCallbacks) {
        this.activity = activity;
        this.gattCustomCallbacks = gattCustomCallbacks;
        if (this.gattCustomCallbacks == null) {
            this.gattCustomCallbacks = NULL_CALLBACK;
        }
        if (bluetoothManager == null) {
            Log.e(TAG, "GattManager bluetoothManager Null");
            bluetoothManager = (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager == null) {
                Toast.makeText(activity, R.string.bt_fail, Toast.LENGTH_LONG).show();
                activity.finish();
            }
        }
        if (bluetoothAdapter == null) {
            Log.e(TAG, "GattManager bluetoothAdapter Null");
            bluetoothAdapter = bluetoothManager.getAdapter();
        }
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
            bluetoothGatt = bluetoothDevice.connectGatt(activity, true, bluetoothGattCallback);
        }
    }


    public void disconnect() {
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
        }
    }

    public boolean isBluetoothAvailable() {
        if (bluetoothManager == null) {
            Log.e(TAG, "isBluetoothAvailable bluetoothManager Null");
            return false;
        }
        if (bluetoothAdapter == null) {
            Log.e(TAG, "isBluetoothAvailable bluetoothAdapter Null");
            return false;
        }
        return bluetoothAdapter.isEnabled();
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
        if (bluetoothGatt != null) {
            bluetoothGatt.discoverServices();
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

                stopMonitoringRssiValue();
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
            } else {
                disconnect();
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
            } else {
                disconnect();
            }
        }
    };
}