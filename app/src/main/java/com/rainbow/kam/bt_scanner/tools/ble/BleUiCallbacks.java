package com.rainbow.kam.bt_scanner.tools.ble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.util.List;

/**
 * Created by kam6512 on 2015-10-29.
 */
public interface BleUiCallbacks {

//    void onDeviceFound(final BluetoothDevice device, int rssi, byte[] record);

    void onDeviceConnected(final BluetoothGatt gatt,
                           final BluetoothDevice device);

    void onDeviceDisconnected(final BluetoothGatt gatt,
                              final BluetoothDevice device);

    void onServicesFound(final BluetoothGatt gatt,
                         final BluetoothDevice device,
                         final List<BluetoothGattService> services);

    void onCharacteristicFound(final BluetoothGatt gatt,
                               final BluetoothDevice device,
                               final BluetoothGattService service,
                               final List<BluetoothGattCharacteristic> chars);

//    void startDeviceWrite(final BluetoothGatt gatt,
//                                  final BluetoothDevice device,
//                                  final BluetoothGattService service,
//                                  final BluetoothGattCharacteristic characteristic);

    void onNewDataFound(final BluetoothGatt gatt,
                        final BluetoothDevice device,
                        final BluetoothGattService service,
                        final BluetoothGattCharacteristic ch,
                        final String strValue,
                        final int intValue,
                        final byte[] rawValue,
                        final String timestamp);

    void onDataNotify(final BluetoothGatt gatt,
                      final BluetoothDevice device,
                      final BluetoothGattService service,
                      final BluetoothGattCharacteristic characteristic);

    void onWriteSuccess(final BluetoothGatt gatt,
                        final BluetoothDevice device,
                        final BluetoothGattService service,
                        final BluetoothGattCharacteristic ch,
                        final String description);

    void onWriteFail(final BluetoothGatt gatt,
                     final BluetoothDevice device,
                     final BluetoothGattService service,
                     final BluetoothGattCharacteristic ch,
                     final String description);

    void onRssiUpdate(final BluetoothGatt gatt, final BluetoothDevice device, final int rssi);

    /* define Null Adapter class for that interface */
    class Null implements BleUiCallbacks {
        @Override
        public void onDeviceConnected(BluetoothGatt gatt, BluetoothDevice device) {
        }

        @Override
        public void onDeviceDisconnected(BluetoothGatt gatt, BluetoothDevice device) {
        }

        @Override
        public void onServicesFound(BluetoothGatt gatt, BluetoothDevice device,
                                    List<BluetoothGattService> services) {
        }

        @Override
        public void onCharacteristicFound(BluetoothGatt gatt,
                                          BluetoothDevice device, BluetoothGattService service,
                                          List<BluetoothGattCharacteristic> chars) {
        }

//        @Override
//        public void startDeviceWrite(BluetoothGatt gatt,
//                                             BluetoothDevice device, BluetoothGattService service,
//                                             BluetoothGattCharacteristic characteristic) {
//        }

        @Override
        public void onNewDataFound(BluetoothGatt gatt,
                                   BluetoothDevice device, BluetoothGattService service,
                                   BluetoothGattCharacteristic ch, String strValue, int intValue,
                                   byte[] rawValue, String timestamp) {
        }

        @Override
        public void onDataNotify(BluetoothGatt gatt, BluetoothDevice device,
                                 BluetoothGattService service,
                                 BluetoothGattCharacteristic characteristic) {
        }

        @Override
        public void onWriteSuccess(BluetoothGatt gatt, BluetoothDevice device,
                                   BluetoothGattService service, BluetoothGattCharacteristic ch,
                                   String description) {
        }

        @Override
        public void onWriteFail(BluetoothGatt gatt, BluetoothDevice device,
                                BluetoothGattService service, BluetoothGattCharacteristic ch,
                                String description) {
        }

        @Override
        public void onRssiUpdate(BluetoothGatt gatt, BluetoothDevice device,
                                 int rssi) {
        }

//        @Override
//        public void onDeviceFound(BluetoothDevice device, int rssi, byte[] record) {
//        }
    }

}
