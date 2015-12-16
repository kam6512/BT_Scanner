package com.rainbow.kam.bt_scanner.tools.gatt;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.util.List;

/**
 * Created by kam6512 on 2015-10-29.
 */
public interface GattCustomCallbacks {

    void onDeviceConnected();

    void onDeviceDisconnected();

    void onServicesFound(final List<BluetoothGattService> services);

    void onReadSuccess(
            final BluetoothGattCharacteristic ch);

    void onReadFail();


    void onDataNotify(
            final BluetoothGattCharacteristic ch);

    void onWriteSuccess(
            final String description);

    void onWriteFail(
            final String description);

    void onRssiUpdate(final int rssi);

    /* define Null Adapter class for that interface */
    class Null implements GattCustomCallbacks {
        @Override
        public void onDeviceConnected() {
        }

        @Override
        public void onDeviceDisconnected() {
        }

        @Override
        public void onServicesFound(List<BluetoothGattService> services) {
        }

        @Override
        public void onReadSuccess(BluetoothGattCharacteristic ch) {
        }

        @Override
        public void onReadFail() {
        }

        @Override
        public void onDataNotify(BluetoothGattCharacteristic ch) {
        }

        @Override
        public void onWriteSuccess(
                String description) {
        }

        @Override
        public void onWriteFail(
                String description) {
        }

        @Override
        public void onRssiUpdate(
                int rssi) {
        }
    }
}
